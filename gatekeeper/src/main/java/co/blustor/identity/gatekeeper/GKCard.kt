package co.blustor.identity.gatekeeper

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.SystemClock
import android.util.Log
import co.blustor.identity.gatekeeper.callbacks.*
import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import com.google.common.primitives.Bytes
import org.jdeferred.DonePipe
import org.jdeferred.Promise
import org.jdeferred.impl.DeferredObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Phaser
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class GKCard(private val mAddress: String) {
    private val mControlPointBuffer = ArrayList<Byte>()

    private var mMtu = 20

    private fun makeCommandData(
        command: Byte, string: String?
    ): Promise<ByteArray, CardException, Void> {
        Log.d(tag, "makeCommandData")
        val deferredObject = DeferredObject<ByteArray, CardException, Void>()

        val runnable = Runnable {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                byteArrayOutputStream.write(command.toInt())
                if (string != null) {
                    byteArrayOutputStream.write(string.length)
                    byteArrayOutputStream.write(string.toByteArray(Charsets.UTF_8))
                    byteArrayOutputStream.write(0)
                }

                val data = byteArrayOutputStream.toByteArray()
                if (data.size <= mMtu - 3) {
                    deferredObject.resolve(data)
                } else {
                    deferredObject.reject(CardException(CardError.MAKE_COMMAND_DATA_FAILED))
                }
            } catch (e: IOException) {
                deferredObject.reject(CardException(CardError.MAKE_COMMAND_DATA_FAILED))
            }
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    private fun waitOnControlPointResult(): Promise<ByteArray, CardException, Void> {
        Log.d(tag, "waitOnControlPointResult")
        val deferredObject = DeferredObject<ByteArray, CardException, Void>()

        val runnable = Runnable {
            var controlPointBufferSize = 0

            while (true) {
                BluetoothLog.d("waitOnControlPointResult: waiting")
                SystemClock.sleep(1000)

                if (mControlPointBuffer.size == controlPointBufferSize) {
                    break
                } else {
                    controlPointBufferSize = mControlPointBuffer.size
                }
            }

            val data = Bytes.toArray(mControlPointBuffer)
            mControlPointBuffer.clear()

            Log.i(tag, String.format("waitOnControlPointResult: %d bytes", data.size))

            deferredObject.resolve(data)
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    private fun fileWrite(data: ByteArray): Promise<Void, CardException, Void> {
        Log.i(tag, String.format("fileWrite: %d bytes", data.size))
        val deferredObject = DeferredObject<Void, CardException, Void>()

        val runnable = {
            var round = 0
            var offset = 0

            val chunkMaxSize = mMtu - 3

            val phaser = Phaser()

            do {
                val chunkSize = if (data.size - offset > chunkMaxSize) chunkMaxSize else data.size - offset
                val chunk = Arrays.copyOfRange(data, offset, offset + chunkSize)

                Log.i(tag, String.format("fileWrite <- %d bytes (round %d)", chunk.size, round))

                val phase = phaser.register()

                BluetoothClient.characteristicWrite(serviceUUID, fileWriteUUID, chunk, object : CharacteristicWriteCallback {
                    override fun onCharacteristicNotFound() {
                        deferredObject.reject(CardException(CardError.CHARACTERISTIC_WRITE_FAILURE))
                        phaser.arriveAndDeregister()
                    }

                    override fun onCharacteristicWrite(status: Int) {
                        if (status == GATT_SUCCESS) {
                            Log.i(tag, "fileWrite <- success")
                        } else {
                            deferredObject.reject(CardException(CardError.CHARACTERISTIC_WRITE_FAILURE))
                        }

                        phaser.arriveAndDeregister()
                    }

                    override fun onNotConnected() {
                        deferredObject.reject(CardException(CardError.CHARACTERISTIC_WRITE_FAILURE))
                        phaser.arriveAndDeregister()
                    }

                    override fun onTimeout() {
                        deferredObject.reject(CardException(CardError.CHARACTERISTIC_WRITE_FAILURE))
                        phaser.arriveAndDeregister()
                    }
                })

                try {
                    phaser.awaitAdvanceInterruptibly(phase, 1000, TimeUnit.MILLISECONDS)
                } catch (e: InterruptedException) {
                    break
                } catch (e: TimeoutException) {
                    break
                }

                if (!deferredObject.isPending) {
                    break
                }

                round += 1
                offset += chunkSize
            } while (offset < data.size)

            Log.d(tag, "fileWrite: complete")

            if (deferredObject.isPending) {
                Log.d(tag, "fileWrite: resolve")
                deferredObject.resolve(null)
            }
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    private fun writeToControlPoint(data: ByteArray): Promise<Void, CardException, Void> {
        Log.d(tag, "writeToControlPoint")
        val deferredObject = DeferredObject<Void, CardException, Void>()

        val runnable = {
            mControlPointBuffer.clear()

            val hexString = BaseEncoding.base16().encode(data)
            Log.d(tag, String.format("writeToControlPoint: %s", hexString))

            BluetoothClient.characteristicWrite(serviceUUID, controlPointUUID, data, object : CharacteristicWriteCallback {
                override fun onCharacteristicNotFound() {
                    deferredObject.reject(CardException(CardError.CHARACTERISTIC_WRITE_FAILURE))
                }

                override fun onCharacteristicWrite(status: Int) {
                    if (status == GATT_SUCCESS) {
                        Log.d(tag, "onCharacteristicWrite: success")
                        deferredObject.resolve(null)
                    } else {
                        Log.d(tag, "onCharacteristicWrite: fail")
                        deferredObject.reject(CardException(CardError.CHARACTERISTIC_WRITE_FAILURE))
                    }
                }

                override fun onNotConnected() {
                    deferredObject.reject(CardException(CardError.CHARACTERISTIC_WRITE_FAILURE))
                }

                override fun onTimeout() {
                    deferredObject.reject(CardException(CardError.CHARACTERISTIC_WRITE_FAILURE))
                }
            })
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    fun connect(context: Context): Promise<Void, CardException, Void> {
        Log.d(tag, "connect")
        val deferredObject = DeferredObject<Void, CardException, Void>()

        val runnable = {
            mMtu = 20

            Log.d(tag, "(1/4) Connecting ...")
            BluetoothClient.connect(context, mAddress, object : ConnectCallback {
                override fun onConnectionStateChange(status: Int, newState: Int) {
                    if (status == GATT_SUCCESS) {
                        Log.d(tag, "(2/4) Request MTU ...")
                        BluetoothClient.requestMtu(512, object : RequestMtuCallback {
                            override fun onMtuChanged(mtu: Int, status: Int) {
                                Log.d(tag, "onMtuChanged")
                                if (status == GATT_SUCCESS) {
                                    mMtu = mtu
                                    Log.d(tag, "onMtuChanged: " + mMtu)

                                    Log.d(tag, "(3/4) Discover services ...")
                                    BluetoothClient.discoverServices(object : DiscoverServicesCallback {
                                        override fun onServicesDiscovered(status: Int) {
                                            Log.i(
                                                tag, "(4/4) Enable control point notifications ..."
                                            )
                                            BluetoothClient.descriptorWrite(serviceUUID, controlPointUUID, characteristicConfigurationUUID, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, object : DescriptorWriteCallback {
                                                override fun onDescriptorNotFound() {
                                                    deferredObject.reject(null)
                                                }

                                                override fun onDescriptorWrite(status: Int) {
                                                    if (status == GATT_SUCCESS) {
                                                        if (BluetoothClient.enableNotify(
                                                                serviceUUID, controlPointUUID
                                                            )) {
                                                            BluetoothClient.notify(object : NotifyCallback {
                                                                override fun onNotify(
                                                                    serviceUUID: UUID, characteristicUUID: UUID, value: ByteArray
                                                                ) {
                                                                    if (characteristicUUID == controlPointUUID) {
                                                                        Log.d(
                                                                            tag, String.format(
                                                                                "controlPointBuffer <- %d bytes", value.size
                                                                            )
                                                                        )
                                                                        mControlPointBuffer.addAll(
                                                                            Bytes.asList(*value)
                                                                        )
                                                                    } else {
                                                                        Log.d(
                                                                            tag, "Notification callback received a value for unknown service $serviceUUID, characteristic $characteristicUUID"
                                                                        )
                                                                    }
                                                                }
                                                            })

                                                            deferredObject.resolve(null)
                                                        } else {
                                                            deferredObject.reject(
                                                                CardException(
                                                                    CardError.CONNECTION_FAILED
                                                                )
                                                            )
                                                        }
                                                    } else {
                                                        deferredObject.reject(
                                                            CardException(
                                                                CardError.CONNECTION_FAILED
                                                            )
                                                        )
                                                    }
                                                }

                                                override fun onNotConnected() {
                                                    deferredObject.reject(null)
                                                }

                                                override fun onTimeout() {
                                                    deferredObject.reject(null)
                                                }
                                            })
                                        }

                                        override fun onNotConnected() {
                                            deferredObject.reject(CardException(CardError.CONNECTION_FAILED))
                                        }

                                        override fun onTimeout() {
                                            deferredObject.reject(CardException(CardError.CONNECTION_FAILED))
                                        }
                                    })
                                } else {
                                    deferredObject.reject(CardException(CardError.CONNECTION_FAILED))
                                }
                            }

                            override fun onNotConnected() {
                                Log.i(tag, "onNotConnected")
                                deferredObject.reject(CardException(CardError.CONNECTION_FAILED))
                            }

                            override fun onTimeout() {
                                Log.i(tag, "onTimeout")
                                deferredObject.reject(CardException(CardError.CONNECTION_FAILED))
                            }
                        })
                    } else {
                        deferredObject.reject(CardException(CardError.CONNECTION_FAILED))
                    }
                }

                override fun onBluetoothNotSupported() {
                    deferredObject.reject(CardException(CardError.CONNECTION_FAILED))
                }

                override fun onTimeout() {
                    deferredObject.reject(CardException(CardError.CONNECTION_FAILED))
                }
            })
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    fun disconnect(): Promise<Void, CardException, Void> {
        Log.i(tag, "disconnect")
        val deferredObject = DeferredObject<Void, CardException, Void>()

        val runnable = {
            BluetoothClient.disconnect(object : DisconnectCallback {
                override fun onDisconnected() {
                    Log.d(tag, "disconnect: success")
                }

                override fun onNotConnected() {
                    Log.d(tag, "disconnect: not connected")
                }

                override fun onTimeout() {
                    Log.d(tag, "disconnect: timeout")
                }
            })
        }
        Thread(runnable).start()

        deferredObject.resolve(null)

        return deferredObject.promise()
    }

    fun checkBluetoothState(): Promise<Void, CardException, Void> {
        Log.i(tag, "checkBluetoothState")
        val deferredObject = DeferredObject<Void, CardException, Void>()

        val runnable = Runnable {
            if (BluetoothAdapter.getDefaultAdapter().isEnabled) {
                if (BluetoothClient.getBondState(mAddress) == BluetoothDevice.BOND_BONDED) {
                    deferredObject.resolve(null)
                } else {
                    Log.i(tag, "checkBluetoothState: LE is not paired.")
                    deferredObject.reject(CardException(CardError.CARD_NOT_PAIRED))
                }
            } else {
                Log.i(tag, "checkBluetoothState: Bluetooth adapter not enabled.")
                deferredObject.reject(CardException(CardError.BLUETOOTH_ADAPTER_NOT_ENABLED))
            }
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    fun delete(path: String): Promise<Void, CardException, Void> {
        Log.d(tag, "delete")
        val deferredObject = DeferredObject<Void, CardException, Void>()

        val runnable = Runnable {
            if (path.length <= 30) {
                makeCommandData(
                    7.toByte(), path
                ).then(DonePipe<ByteArray, Void, CardException, Void> {
                    this.writeToControlPoint(it)
                }).then(DonePipe<Void, Void, CardException, Void> {
                    deferredObject.resolve(it)
                }).fail({
                    deferredObject.reject(it)
                })
            } else {
                deferredObject.reject(CardException(CardError.ARGUMENT_INVALID))
            }
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    fun exists(path: String): Promise<Boolean, CardException, Void> {
        Log.d(tag, "exists")
        val deferredObject = DeferredObject<Boolean, CardException, Void>()

        val runnable = Runnable {
            if (path.length <= 30) {
                makeCommandData(
                    10.toByte(), path
                ).then(DonePipe<ByteArray, Void, CardException, Void> {
                    this.writeToControlPoint(it)
                }).then(DonePipe<Void, ByteArray, CardException, Void> {
                    waitOnControlPointResult()
                }).then {
                    val hexString = BaseEncoding.base16().encode(it)
                    if (it.size == 1) {
                        val value = it[0].toInt()
                        when (value) {
                            0x06 -> {
                                Log.d(tag, String.format("exists: %s (true)", hexString))
                                deferredObject.resolve(true)
                            }
                            0x07 -> {
                                Log.d(tag, String.format("exists: %s (false)", hexString))
                                deferredObject.resolve(false)
                            }
                            else -> deferredObject.reject(CardException(CardError.INVALID_RESPONSE))
                        }
                    } else {
                        deferredObject.reject(CardException(CardError.INVALID_RESPONSE))
                    }
                }.fail({ deferredObject.reject(it) })
            } else {
                deferredObject.reject(CardException(CardError.ARGUMENT_INVALID))
            }
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    fun getPath(path: String): Promise<ByteArray, CardException, Void> {
        Log.d(tag, "get")
        val deferredObject = DeferredObject<ByteArray, CardException, Void>()

        val runnable = Runnable {
            if (path.length <= 30) {
                makeCommandData(
                    2.toByte(), path
                ).then(DonePipe<ByteArray, Void, CardException, Void> {
                    this.writeToControlPoint(it)
                }).then(DonePipe<Void, ByteArray, CardException, Void> {
                    waitOnControlPointResult()
                }).then {
                    if (it.size == 1) {
                        if (it[0].toInt() == 0x07) {
                            deferredObject.reject(CardException(CardError.FILE_NOT_FOUND))
                        } else {
                            deferredObject.resolve(it)
                        }
                    } else {
                        deferredObject.resolve(it)
                    }
                }.fail({
                        deferredObject.reject(it)
                    })
            } else {
                deferredObject.reject(CardException(CardError.ARGUMENT_INVALID))
            }
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    fun rename(name: String): Promise<Void, CardException, Void> {
        Log.d(tag, "rename")
        val deferredObject = DeferredObject<Void, CardException, Void>()

        val runnable = Runnable {
            if (name.length <= 11) {
                makeCommandData(
                    8.toByte(), name
                ).then(DonePipe<ByteArray, Void, CardException, Void> {
                    this.writeToControlPoint(it)
                }).then(DonePipe<Void, Void, CardException, Void> {
                    deferredObject.resolve(it)
                }).fail({
                    deferredObject.reject(it)
                })
            } else {
                deferredObject.reject(CardException(CardError.ARGUMENT_INVALID))
            }
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    fun checksum(data: ByteArray): Promise<Void, CardException, Void> {
        Log.d(tag, "checksum")
        val deferredObject = DeferredObject<Void, CardException, Void>()

        val runnable = {
            val ourChecksum = GKCrypto.crc16(data)

            BluetoothClient.characteristicRead(serviceUUID, fileWriteUUID, object : CharacteristicReadCallback {
                override fun onCharacteristicNotFound() {
                    deferredObject.reject(CardException(CardError.CHARACTERISTIC_READ_FAILURE))
                }

                override fun onCharacteristicRead(status: Int, value: ByteArray) {
                    if (status == GATT_SUCCESS) {
                        if (value.size == 2) {
                            val ourHexString = BaseEncoding.base16().encode(
                                byteArrayOf(
                                    ourChecksum.ushr(8).toByte(), ourChecksum.toByte()
                                )
                            )
                            val cardHexString = BaseEncoding.base16().encode(value)

                            val cardChecksum = value[0].toInt() shl 8 and 0x0000ff00 or (value[1].toInt() and 0x000000ff)

                            Log.d(
                                tag, String.format(
                                    "checksum: %s (%d, ours)", ourHexString, ourChecksum
                                )
                            )
                            Log.d(
                                tag, String.format(
                                    "checksum: %s (%d, card)", cardHexString, cardChecksum
                                )
                            )

                            if (ourChecksum == cardChecksum) {
                                Log.d(tag, "checksum: match")
                                deferredObject.resolve(null)
                            } else {
                                Log.d(tag, "checksum: invalid")
                                deferredObject.reject(CardException(CardError.INVALID_CHECKSUM))
                            }
                        } else {
                            deferredObject.reject(CardException(CardError.INVALID_CHECKSUM))
                        }
                    } else {
                        deferredObject.reject(CardException(CardError.CHARACTERISTIC_READ_FAILURE))
                    }
                }

                override fun onDisconnected() {
                    deferredObject.reject(CardException(CardError.CHARACTERISTIC_READ_FAILURE))
                }

                override fun onTimeout() {
                    deferredObject.reject(CardException(CardError.CHARACTERISTIC_READ_FAILURE))
                }
            })
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    fun close(path: String): Promise<Void, CardException, Void> {
        Log.d(tag, "close")
        val deferredObject = DeferredObject<Void, CardException, Void>()

        val runnable = Runnable {
            if (path.length <= 30) {
                makeCommandData(
                    4.toByte(), path
                ).then(DonePipe<ByteArray, Void, CardException, Void> {
                    this.writeToControlPoint(it)
                }).then(DonePipe<Void, ByteArray, CardException, Void> {
                    waitOnControlPointResult()
                }).then({
                    deferredObject.resolve(null)
                }).fail({
                    deferredObject.reject(it)
                })
            } else {
                deferredObject.reject(CardException(CardError.ARGUMENT_INVALID))
            }
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    fun put(data: ByteArray): Promise<Void, CardException, Void> {
        Log.d(tag, "put")
        val deferredObject = DeferredObject<Void, CardException, Void>()

        val runnable = Runnable {
            makeCommandData(3.toByte(), null).then(DonePipe<ByteArray, Void, CardException, Void> {
                this.writeToControlPoint(it)
            }).then(DonePipe<Void, ByteArray, CardException, Void> {
                waitOnControlPointResult()
            }).then(DonePipe<ByteArray, Void, CardException, Void> {
                fileWrite(data)
            }).then({
                deferredObject.resolve(null)
            }).fail({
                deferredObject.reject(it)
            })
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    enum class CardError {
        ARGUMENT_INVALID, BLUETOOTH_NOT_AVAILABLE, BLUETOOTH_ADAPTER_NOT_ENABLED, CARD_NOT_PAIRED, CONNECTION_FAILED, CHARACTERISTIC_READ_FAILURE, CHARACTERISTIC_WRITE_FAILURE, FILE_NOT_FOUND, FILE_READ_FAILED, FILE_WRITE_FAILED, MAKE_COMMAND_DATA_FAILED, INVALID_CHECKSUM, INVALID_RESPONSE
    }

    class CardException internal constructor(val error: CardError) : Exception()

    companion object {
        val serviceUUID: UUID = UUID.fromString("423AD87A-B100-4F14-9EAA-5EB5839F2A54")
        val controlPointUUID: UUID = UUID.fromString("423AD87A-0001-4F14-9EAA-5EB5839F2A54")
        val characteristicConfigurationUUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        val fileWriteUUID: UUID = UUID.fromString("423AD87A-0002-4F14-9EAA-5EB5839F2A54")
        val tag = "GKCard"
    }
}
