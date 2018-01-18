package co.blustor.identity.gatekeeper

import android.bluetooth.*
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.content.Context
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
                if (connectCallback != null) {
                    BluetoothLog.d("onConnectionStateChange: connectCallback")
                    connectCallback?.onConnectionStateChange(status, newState)
                    stopWaitingForCallback()
                }
            } else if (newState == STATE_DISCONNECTED) {
                if (disconnectCallback != null) {
                    BluetoothLog.d("onConnectionStateChange: disconnectCallback")
                    disconnectCallback?.onDisconnected()
                    stopWaitingForCallback()
                }
            } else {
                BluetoothLog.d(
                    String.format(
                        Locale.getDefault(), "onConnectionStateChange: Unknown status %d, newState = %d", status, newState
                    )
                )
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            BluetoothLog.d("onServicesDiscovered")
            if (discoverServicesCallback != null) {
                BluetoothLog.d("onServicesDiscovered: discoverServicesCallback")
                discoverServicesCallback?.onServicesDiscovered(status)
                stopWaitingForCallback()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            BluetoothLog.d("onCharacteristicRead")
            if (characteristicReadCallback != null) {
                BluetoothLog.d("onCharacteristicRead: characteristicReadCallback")
                characteristicReadCallback?.onCharacteristicRead(status, characteristic.value)
                stopWaitingForCallback()
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            BluetoothLog.d("onCharacteristicWrite")
            if (characteristicWriteCallback != null) {
                BluetoothLog.d("onCharacteristicWrite: characteristicWriteCallback")
                characteristicWriteCallback?.onCharacteristicWrite(status)
                stopWaitingForCallback()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic
        ) {
            val hexString = BaseEncoding.base16().encode(characteristic.value)
            BluetoothLog.d(
                String.format(
                    "onCharacteristicChanged: %s to %s", characteristic.uuid, hexString
                )
            )
            if (notifyCallback != null) {
                BluetoothLog.d("onCharacteristicChanged: notifyCallback")
                val service = characteristic.service
                notifyCallback?.onNotify(service.uuid, characteristic.uuid, characteristic.value)
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            BluetoothLog.d("onDescriptorRead")
            if (descriptorReadCallback != null) {
                BluetoothLog.d("onDescriptorRead: descriptorReadCallback")
                descriptorReadCallback?.onDescriptorRead(status, descriptor.value)
                try {
                    cyclicBarrier.await()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: BrokenBarrierException) {
                    e.printStackTrace()
                }

            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            BluetoothLog.d("onDescriptorWrite")
            if (descriptorWriteCallback != null) {
                BluetoothLog.d("onDescriptorWrite: descriptorWriteCallback")
                descriptorWriteCallback?.onDescriptorWrite(status)
                stopWaitingForCallback()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            BluetoothLog.d("onMtuChanged")
            if (requestMtuCallback != null) {
                BluetoothLog.d("onMtuChanged: requestMtuCallback")
                requestMtuCallback?.onMtuChanged(mtu, status)
                stopWaitingForCallback()
            }
        }
    }

    private fun stopWaitingForCallback() {
        try {
            cyclicBarrier.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: BrokenBarrierException) {
            e.printStackTrace()
        }
    }

    // Event processing

    private fun getCharacteristic(
        gatt: BluetoothGatt?, serviceUUID: UUID, characteristicUUID: UUID
    ): BluetoothGattCharacteristic? {
        return if (gatt == null) {
            null
        } else {
            val service = gatt.getService(serviceUUID)
            service?.getCharacteristic(characteristicUUID)
        }
    }

    private fun getDescriptor(
        gatt: BluetoothGatt?, serviceUUID: UUID, characteristicUUID: UUID, descriptorUUID: UUID
    ): BluetoothGattDescriptor? {
        return if (gatt == null) {
            null
        } else {
            val service = gatt.getService(serviceUUID)
            if (service == null) {
                null
            } else {
                val characteristic = service.getCharacteristic(characteristicUUID)
                characteristic?.getDescriptor(descriptorUUID)
            }
        }
    }

    private fun queueEvent(event: Event) {
        executorService.execute {

            cyclicBarrier.reset()

            val eventName = event.name
            BluetoothLog.d("Processing " + eventName.name)
            when (eventName) {
                BluetoothConstants.EventName.CHARACTERISTIC_READ -> {
                    val characteristicReadEvent = event as CharacteristicReadEvent
                    characteristicReadCallback = characteristicReadEvent.callback

                    if (gatt == null) {
                        BluetoothLog.d("Not connected.")
                        if (characteristicReadCallback != null) {
                            characteristicReadCallback?.onDisconnected()
                        }
                    } else {
                        val characteristic = getCharacteristic(
                            gatt, characteristicReadEvent.serviceUUID, characteristicReadEvent.characteristicUUID
                        )
                        if (characteristic == null) {
                            if (characteristicReadCallback != null) {
                                characteristicReadCallback?.onCharacteristicNotFound()
                            }
                        } else {
                            gatt?.readCharacteristic(characteristic)
                            try {
                                cyclicBarrier.await(1000, TimeUnit.MILLISECONDS)
                            } catch (e: InterruptedException) {
                                if (characteristicReadCallback != null) {
                                    characteristicReadCallback?.onTimeout()
                                }
                            } catch (e: BrokenBarrierException) {
                                if (characteristicReadCallback != null) {
                                    characteristicReadCallback?.onTimeout()
                                }
                            } catch (e: TimeoutException) {
                                if (characteristicReadCallback != null) {
                                    characteristicReadCallback?.onTimeout()
                                }
                            }

                        }
                    }
                }
                BluetoothConstants.EventName.CHARACTERISTIC_WRITE -> {
                    val characteristicWriteEvent = event as CharacteristicWriteEvent
                    characteristicWriteCallback = characteristicWriteEvent.callback

                    if (gatt == null) {
                        if (characteristicWriteCallback != null) {
                            characteristicWriteCallback?.onNotConnected()
                        }
                    } else {
                        val characteristic = getCharacteristic(
                            gatt, characteristicWriteEvent.serviceUUID, characteristicWriteEvent.characteristicUUID
                        )
                        if (characteristic == null) {
                            if (characteristicWriteCallback != null) {
                                characteristicWriteCallback?.onCharacteristicNotFound()
                            }
                        } else {
                            val value = characteristicWriteEvent.value
                            val hexString = BaseEncoding.base16().encode(value)

                            BluetoothLog.d(
                                String.format(
                                    "writeDescriptor: %s to %s", hexString, characteristic.uuid
                                )
                            )
                            characteristic.value = characteristicWriteEvent.value

                            gatt?.writeCharacteristic(characteristic)
                            try {
                                cyclicBarrier.await(1000, TimeUnit.MILLISECONDS)
                            } catch (e: InterruptedException) {
                                if (characteristicWriteCallback != null) {
                                    characteristicWriteCallback?.onTimeout()
                                }
                            } catch (e: BrokenBarrierException) {
                                if (characteristicWriteCallback != null) {
                                    characteristicWriteCallback?.onTimeout()
                                }
                            } catch (e: TimeoutException) {
                                if (characteristicWriteCallback != null) {
                                    characteristicWriteCallback?.onTimeout()
                                }
                            }

                        }
                    }
                }
                BluetoothConstants.EventName.CONNECT -> {
                    val connectEvent = event as ConnectEvent
                    connectCallback = connectEvent.callback

                    val address = connectEvent.address
                    device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)

                    if (device == null) {
                        if (connectCallback != null) {
                            connectCallback?.onBluetoothNotSupported()
                        }
                    } else {
                        gatt = if (gatt == null) {
                            device?.connectGatt(connectEvent.context, false, gattCallback)
                        } else {
                            gatt?.close()
                            device?.connectGatt(connectEvent.context, false, gattCallback)
                        }

                        try {
                            cyclicBarrier.await(10000, TimeUnit.MILLISECONDS)
                        } catch (e: InterruptedException) {
                            if (connectCallback != null) {
                                connectCallback?.onTimeout()
                            }
                        } catch (e: BrokenBarrierException) {
                            if (connectCallback != null) {
                                connectCallback?.onTimeout()
                            }
                        } catch (e: TimeoutException) {
                            if (connectCallback != null) {
                                connectCallback?.onTimeout()
                            }
                        }

                    }
                }
                BluetoothConstants.EventName.DESCRIPTOR_READ -> {
                    val descriptorReadEvent = event as DescriptorReadEvent
                    descriptorReadCallback = descriptorReadEvent.callback

                    if (gatt == null) {
                        if (descriptorReadCallback != null) {
                            descriptorReadCallback?.onNotConnected()
                        }
                    } else {
                        val descriptor = getDescriptor(
                            gatt, descriptorReadEvent.serviceUUID, descriptorReadEvent.characteristicUUID, descriptorReadEvent.descriptorUUID
                        )
                        if (descriptor == null) {
                            if (descriptorReadCallback != null) {
                                descriptorReadCallback?.onDescriptorNotFound()
                            }
                        } else {
                            gatt?.readDescriptor(descriptor)
                            try {
                                cyclicBarrier.await(1000, TimeUnit.MILLISECONDS)
                            } catch (e: InterruptedException) {
                                if (descriptorReadCallback != null) {
                                    descriptorReadCallback?.onTimeout()
                                }
                            } catch (e: BrokenBarrierException) {
                                if (descriptorReadCallback != null) {
                                    descriptorReadCallback?.onTimeout()
                                }
                            } catch (e: TimeoutException) {
                                if (descriptorReadCallback != null) {
                                    descriptorReadCallback?.onTimeout()
                                }
                            }

                        }
                    }
                }
                BluetoothConstants.EventName.DESCRIPTOR_WRITE -> {
                    val descriptorWriteEvent = event as DescriptorWriteEvent
                    descriptorWriteCallback = descriptorWriteEvent.callback

                    if (gatt == null) {
                        if (descriptorWriteCallback != null) {
                            descriptorWriteCallback?.onNotConnected()
                        }
                    } else {
                        val descriptor = getDescriptor(
                            gatt, descriptorWriteEvent.serviceUUID, descriptorWriteEvent.characteristicUUID, descriptorWriteEvent.descriptorUUID
                        )
                        if (descriptor == null) {
                            if (descriptorWriteCallback != null) {
                                descriptorWriteCallback?.onDescriptorNotFound()
                            }
                        } else {
                            val value = descriptorWriteEvent.value
                            val hexString = BaseEncoding.base16().encode(value)
                            BluetoothLog.d(
                                String.format(
                                    "writeDescriptor: %s to %s", hexString, descriptor.uuid
                                )
                            )

                            descriptor.value = value

                            gatt?.writeDescriptor(descriptor)
                            try {
                                cyclicBarrier.await(1000, TimeUnit.MILLISECONDS)
                            } catch (e: InterruptedException) {
                                if (descriptorWriteCallback != null) {
                                    descriptorWriteCallback?.onTimeout()
                                }
                            } catch (e: BrokenBarrierException) {
                                if (descriptorWriteCallback != null) {
                                    descriptorWriteCallback?.onTimeout()
                                }
                            } catch (e: TimeoutException) {
                                if (descriptorWriteCallback != null) {
                                    descriptorWriteCallback?.onTimeout()
                                }
                            }

                        }
                    }
                }
                BluetoothConstants.EventName.DISCONNECT -> {
                    val disconnectEvent = event as DisconnectEvent
                    disconnectCallback = disconnectEvent.callback

                    if (gatt == null) {
                        disconnectCallback?.onNotConnected()
                    } else {
                        gatt?.disconnect()
                        try {
                            cyclicBarrier.await(1000, TimeUnit.MILLISECONDS)
                        } catch (e: InterruptedException) {
                            if (disconnectCallback != null) {
                                disconnectCallback?.onTimeout()
                            }
                        } catch (e: BrokenBarrierException) {
                            if (disconnectCallback != null) {
                                disconnectCallback?.onTimeout()
                            }
                        } catch (e: TimeoutException) {
                            if (disconnectCallback != null) {
                                disconnectCallback?.onTimeout()
                            }
                        }

                    }
                }
                BluetoothConstants.EventName.DISCOVER_SERVICES -> {
                    val discoverServicesEvent = event as DiscoverServicesEvent
                    discoverServicesCallback = discoverServicesEvent.callback

                    if (gatt == null) {
                        if (discoverServicesCallback != null) {
                            discoverServicesCallback?.onNotConnected()
                        }
                    } else {
                        gatt?.discoverServices()
                        try {
                            cyclicBarrier.await(1000, TimeUnit.MILLISECONDS)
                        } catch (e: InterruptedException) {
                            if (discoverServicesCallback != null) {
                                discoverServicesCallback?.onTimeout()
                            }
                        } catch (e: BrokenBarrierException) {
                            if (discoverServicesCallback != null) {
                                discoverServicesCallback?.onTimeout()
                            }
                        } catch (e: TimeoutException) {
                            if (discoverServicesCallback != null) {
                                discoverServicesCallback?.onTimeout()
                            }
                        }

                    }
                }
                BluetoothConstants.EventName.NOTIFY -> {
                }
                BluetoothConstants.EventName.REQUEST_MTU -> {
                    val requestMtuEvent = event as RequestMtuEvent
                    requestMtuCallback = requestMtuEvent.callback

                    if (gatt == null) {
                        if (requestMtuCallback != null) {
                            requestMtuCallback?.onNotConnected()
                        }
                    } else {
                        gatt?.requestMtu(requestMtuEvent.mtu)
                        try {
                            cyclicBarrier.await(1000, TimeUnit.MILLISECONDS)
                        } catch (e: InterruptedException) {
                            if (requestMtuCallback != null) {
                                requestMtuCallback?.onTimeout()
                            }
                        } catch (e: BrokenBarrierException) {
                            if (requestMtuCallback != null) {
                                requestMtuCallback?.onTimeout()
                            }
                        } catch (e: TimeoutException) {
                            if (requestMtuCallback != null) {
                                requestMtuCallback?.onTimeout()
                            }
                        }

                    }
                }
            }
        }
    }

    // Events

    fun connect(context: Context, address: String, callback: ConnectCallback) {
        queueEvent(ConnectEvent(context, address, callback))
    }

    fun disconnect(callback: DisconnectCallback) {
        queueEvent(DisconnectEvent(callback))
    }

    fun discoverServices(callback: DiscoverServicesCallback) {
        queueEvent(DiscoverServicesEvent(callback))
    }

    fun characteristicRead(
        serviceUUID: UUID, characteristicUUID: UUID, callback: CharacteristicReadCallback
    ) {
        queueEvent(CharacteristicReadEvent(serviceUUID, characteristicUUID, callback))
    }

    fun readDescriptor(
        serviceUUID: UUID, characteristicUUID: UUID, descriptorUUID: UUID, callback: DescriptorReadCallback
    ) {
        queueEvent(DescriptorReadEvent(serviceUUID, characteristicUUID, descriptorUUID, callback))
    }

    fun requestMtu(mtu: Int, callback: RequestMtuCallback) {
        queueEvent(RequestMtuEvent(mtu, callback))
    }

    fun characteristicWrite(
        serviceUUID: UUID, characteristicUUID: UUID, value: ByteArray, callback: CharacteristicWriteCallback
    ) {
        queueEvent(CharacteristicWriteEvent(serviceUUID, characteristicUUID, value, callback))
    }

    fun descriptorWrite(
        serviceUUID: UUID, characteristicUUID: UUID, descriptorUUID: UUID, value: ByteArray, callback: DescriptorWriteCallback
    ) {
        queueEvent(
            DescriptorWriteEvent(
                serviceUUID, characteristicUUID, descriptorUUID, value, callback
            )
        )
    }

    // Device

    fun getBondState(address: String): Int {
        return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address).bondState
    }

    // Helpers

    fun notify(callback: NotifyCallback) {
        notifyCallback = callback
        BluetoothLog.d("Notify gattCallback set to " + callback.toString())
    }

    fun enableNotify(serviceUUID: UUID, characteristicUUID: UUID): Boolean {
        return if (gatt == null) {
            false
        } else {
            val characteristic = getCharacteristic(gatt, serviceUUID, characteristicUUID)
            if (characteristic == null) {
                false
            } else {
                BluetoothLog.d("enableNotify: " + characteristic.uuid)
                gatt?.setCharacteristicNotification(characteristic, true) ?: false
            }
        }
    }

    fun setCharacteristicWriteType(
        serviceUUID: UUID, characteristicUUID: UUID, writeType: Int
    ): Boolean {
        return if (gatt == null) {
            false
        } else {
            val characteristic = getCharacteristic(gatt, serviceUUID, characteristicUUID)
            if (characteristic == null) {
                false
            } else {
                characteristic.writeType = writeType
                true
            }
        }
    }
}
