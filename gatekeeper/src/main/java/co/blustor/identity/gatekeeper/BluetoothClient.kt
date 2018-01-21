package co.blustor.identity.gatekeeper

import android.bluetooth.*
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import co.blustor.identity.gatekeeper.callbacks.*
import co.blustor.identity.gatekeeper.events.*
import com.google.common.io.BaseEncoding
import java.util.*
import java.util.concurrent.*

object BluetoothClient {

    private val executorService = Executors.newSingleThreadExecutor()
    private val cyclicBarrier = CyclicBarrier(2)
    private var device: BluetoothDevice? = null
    private var gatt: BluetoothGatt? = null
    private var notifyCallback: NotifyCallback? = null
    private var characteristicReadCallback: CharacteristicReadCallback? = null
    private var characteristicWriteCallback: CharacteristicWriteCallback? = null
    private var connectCallback: ConnectCallback? = null
    private var descriptorReadCallback: DescriptorReadCallback? = null
    private var descriptorWriteCallback: DescriptorWriteCallback? = null
    private var disconnectCallback: DisconnectCallback? = null
    private var discoverServicesCallback: DiscoverServicesCallback? = null
    private var requestMtuCallback: RequestMtuCallback? = null
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            BluetoothLog.d("onConnectionStateChange")
            if (newState == STATE_CONNECTED || newState == 133) {
                BluetoothLog.d("onConnectionStateChange: connectCallback")
                connectCallback?.onConnectionStateChange(status, newState)
                cancelTimeout()
            } else if (newState == STATE_DISCONNECTED) {
                BluetoothLog.d("onConnectionStateChange: disconnectCallback")
                disconnectCallback?.onDisconnected()
                cancelTimeout()
            } else {
                BluetoothLog.d("onConnectionStateChange: Unknown status $status, newState = $newState")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            BluetoothLog.d("onServicesDiscovered")
            discoverServicesCallback?.onServicesDiscovered(status)
            cancelTimeout()
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            BluetoothLog.d("onCharacteristicRead")
            characteristicReadCallback?.onCharacteristicRead(status, characteristic.value)
            cancelTimeout()
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            BluetoothLog.d("onCharacteristicWrite: characteristicWriteCallback")
            characteristicWriteCallback?.onCharacteristicWrite(status)
            cancelTimeout()
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val hexString = BaseEncoding.base16().encode(characteristic.value)
            BluetoothLog.d("onCharacteristicChanged: ${characteristic.uuid} to $hexString")

            notifyCallback?.onNotify(characteristic.service.uuid, characteristic.uuid, characteristic.value)
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            BluetoothLog.d("onDescriptorRead")
            descriptorReadCallback?.onDescriptorRead(status, descriptor.value)
            cancelTimeout()
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            BluetoothLog.d("onDescriptorWrite")
            descriptorWriteCallback?.onDescriptorWrite(status)
            cancelTimeout()
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            BluetoothLog.d("onMtuChanged")
            requestMtuCallback?.onMtuChanged(mtu, status)
            cancelTimeout()
        }
    }

    private fun cancelTimeout() {
        if (!cyclicBarrier.isBroken) {
            try {
                cyclicBarrier.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: BrokenBarrierException) {
                e.printStackTrace()
            }
        }
    }

    // Event processing

    private fun getCharacteristic(gatt: BluetoothGatt?, serviceUUID: UUID, characteristicUUID: UUID): BluetoothGattCharacteristic? {
        return gatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
    }

    private fun getDescriptor(gatt: BluetoothGatt?, serviceUUID: UUID, characteristicUUID: UUID, descriptorUUID: UUID): BluetoothGattDescriptor? {
        return gatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)?.getDescriptor(descriptorUUID)
    }

    private fun timeout(value: Long, timeUnit: TimeUnit) {
        try {
            cyclicBarrier.await(value, timeUnit)
        } catch (e: InterruptedException) {
            requestMtuCallback?.onTimeout()
        } catch (e: BrokenBarrierException) {
            requestMtuCallback?.onTimeout()
        } catch (e: TimeoutException) {
            requestMtuCallback?.onTimeout()
        }
    }

    fun queue(event: Event) {
        executorService.execute {
            cyclicBarrier.reset()

            BluetoothLog.d("Processing ${event.name}")
            when (event.name) {
                BluetoothConstants.EventName.CHARACTERISTIC_READ -> {
                    val characteristicReadEvent = event as CharacteristicReadEvent
                    characteristicReadCallback = characteristicReadEvent.callback

                    gatt?.let {
                        val characteristic = getCharacteristic(gatt, characteristicReadEvent.serviceUUID, characteristicReadEvent.characteristicUUID)
                        if (characteristic == null) {
                            characteristicReadCallback?.onCharacteristicNotFound()
                        } else {
                            it.readCharacteristic(characteristic)
                            timeout(5, TimeUnit.SECONDS)
                        }
                    } ?: characteristicReadCallback?.onDisconnected()
                }
                BluetoothConstants.EventName.CHARACTERISTIC_WRITE -> {
                    val characteristicWriteEvent = event as CharacteristicWriteEvent
                    characteristicWriteCallback = characteristicWriteEvent.callback

                    gatt?.let {
                        val characteristic = getCharacteristic(it, characteristicWriteEvent.serviceUUID, characteristicWriteEvent.characteristicUUID)
                        if (characteristic == null) {
                            characteristicWriteCallback?.onCharacteristicNotFound()
                        } else {
                            val hexString = BaseEncoding.base16().encode(characteristicWriteEvent.value)

                            BluetoothLog.d("writeDescriptor: $hexString to ${characteristic.uuid}")
                            characteristic.value = characteristicWriteEvent.value

                            it.writeCharacteristic(characteristic)
                            timeout(5, TimeUnit.SECONDS)
                        }
                    } ?: characteristicWriteCallback?.onNotConnected()
                }
                BluetoothConstants.EventName.CONNECT -> {
                    val connectEvent = event as ConnectEvent
                    connectCallback = connectEvent.callback

                    val address = connectEvent.address
                    device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)

                    device?.let { device ->
                        gatt = gatt?.let {
                            it.close()
                            device.connectGatt(connectEvent.context, false, gattCallback)
                        } ?: run {
                            device.connectGatt(connectEvent.context, false, gattCallback)
                        }
                        timeout(5, TimeUnit.SECONDS)
                    } ?: connectCallback?.onBluetoothNotSupported()
                }
                BluetoothConstants.EventName.DESCRIPTOR_READ -> {
                    val descriptorReadEvent = event as DescriptorReadEvent
                    descriptorReadCallback = descriptorReadEvent.callback

                    gatt?.let {
                        val descriptor = getDescriptor(gatt, descriptorReadEvent.serviceUUID, descriptorReadEvent.characteristicUUID, descriptorReadEvent.descriptorUUID)
                        if (descriptor == null) {
                            descriptorReadCallback?.onDescriptorNotFound()
                        } else {
                            it.readDescriptor(descriptor)
                            timeout(5, TimeUnit.SECONDS)
                        }
                    } ?: descriptorReadCallback?.onNotConnected()
                }
                BluetoothConstants.EventName.DESCRIPTOR_WRITE -> {
                    val descriptorWriteEvent = event as DescriptorWriteEvent
                    descriptorWriteCallback = descriptorWriteEvent.callback

                    gatt?.let {
                        val descriptor = getDescriptor(it, descriptorWriteEvent.serviceUUID, descriptorWriteEvent.characteristicUUID, descriptorWriteEvent.descriptorUUID)
                        if (descriptor == null) {
                            descriptorWriteCallback?.onDescriptorNotFound()
                        } else {
                            val hexString = BaseEncoding.base16().encode(descriptorWriteEvent.value)

                            BluetoothLog.d("writeDescriptor: $hexString to ${descriptor.uuid}")

                            descriptor.value = descriptorWriteEvent.value
                            it.writeDescriptor(descriptor)
                            timeout(5, TimeUnit.SECONDS)
                        }
                    } ?: descriptorWriteCallback?.onNotConnected()
                }
                BluetoothConstants.EventName.DISCONNECT -> {
                    val disconnectEvent = event as DisconnectEvent
                    disconnectCallback = disconnectEvent.callback

                    gatt?.let {
                        it.disconnect()
                        timeout(5, TimeUnit.SECONDS)
                    } ?: disconnectCallback?.onNotConnected()
                }
                BluetoothConstants.EventName.DISCOVER_SERVICES -> {
                    val discoverServicesEvent = event as DiscoverServicesEvent
                    discoverServicesCallback = discoverServicesEvent.callback

                    gatt?.let {
                        it.discoverServices()
                        timeout(5, TimeUnit.SECONDS)
                    } ?: discoverServicesCallback?.onNotConnected()
                }
                BluetoothConstants.EventName.NOTIFY -> {

                }
                BluetoothConstants.EventName.REQUEST_MTU -> {
                    val requestMtuEvent = event as RequestMtuEvent
                    requestMtuCallback = requestMtuEvent.callback

                    gatt?.let {
                        it.requestMtu(requestMtuEvent.mtu)
                        timeout(5, TimeUnit.SECONDS)
                    } ?: requestMtuCallback?.onNotConnected()
                }
            }
        }
    }

    // Device

    fun getBondState(address: String): Int {
        return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address).bondState
    }

    // Helpers

    fun notify(callback: NotifyCallback) {
        BluetoothLog.d("notify: $callback")
        notifyCallback = callback
    }

    fun enableNotify(serviceUUID: UUID, characteristicUUID: UUID): Boolean {
        BluetoothLog.d("enableNotify: ($serviceUUID, $characteristicUUID)")

        gatt?.let {
            val characteristic = getCharacteristic(it, serviceUUID, characteristicUUID)
            if (characteristic != null) {
                return it.setCharacteristicNotification(characteristic, true)
            }
        }

        return false
    }

    fun setCharacteristicWriteType(serviceUUID: UUID, characteristicUUID: UUID, writeType: Int): Boolean {
        BluetoothLog.d("setCharacteristicWriteType: ($serviceUUID, $characteristicUUID)")
        gatt?.let {
            val characteristic = getCharacteristic(it, serviceUUID, characteristicUUID)
            if (characteristic != null) {
                characteristic.writeType = writeType
                return true
            }
        }

        return false
    }
}
