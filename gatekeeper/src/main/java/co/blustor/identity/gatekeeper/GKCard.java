package co.blustor.identity.gatekeeper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;

import org.jdeferred.DonePipe;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import co.blustor.identity.gatekeeper.callbacks.CharacteristicReadCallback;
import co.blustor.identity.gatekeeper.callbacks.CharacteristicWriteCallback;
import co.blustor.identity.gatekeeper.callbacks.ConnectCallback;
import co.blustor.identity.gatekeeper.callbacks.DescriptorWriteCallback;
import co.blustor.identity.gatekeeper.callbacks.DisconnectCallback;
import co.blustor.identity.gatekeeper.callbacks.DiscoverServicesCallback;
import co.blustor.identity.gatekeeper.callbacks.RequestMtuCallback;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

public class GKCard {

    public static final UUID SERVICE_UUID = UUID.fromString("423AD87A-B100-4F14-9EAA-5EB5839F2A54");
    private static final UUID CONTROL_POINT_UUID = UUID.fromString("423AD87A-0001-4F14-9EAA-5EB5839F2A54");
    private static final UUID CHARACTERISTIC_CONFIGURATION_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID FILE_WRITE_UUID = UUID.fromString("423AD87A-0002-4F14-9EAA-5EB5839F2A54");
    private static final String TAG = "GKCard";

    private final String mAddress;
    private final List<Byte> mControlPointBuffer = new ArrayList<>();

    private int mMtu = 20;

    public GKCard(String address) throws CardException {
        mAddress = address;
    }

    private Promise<byte[], CardException, Void> makeCommandData(byte command, @Nullable String string) {
        Log.d(TAG, "makeCommandData");
        DeferredObject<byte[], CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(command);
                if (string != null) {
                    byteArrayOutputStream.write(string.length());
                    byteArrayOutputStream.write(string.getBytes(Charsets.UTF_8));
                    byteArrayOutputStream.write(0);
                }

                byte[] data = byteArrayOutputStream.toByteArray();
                if (data.length <= mMtu - 3) {
                    deferredObject.resolve(data);
                } else {
                    deferredObject.reject(new CardException(CardError.MAKE_COMMAND_DATA_FAILED));
                }
            } catch (IOException e) {
                deferredObject.reject(new CardException(CardError.MAKE_COMMAND_DATA_FAILED));
            }
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    private Promise<byte[], CardException, Void> waitOnControlPointResult() {
        Log.d(TAG, "waitOnControlPointResult");
        DeferredObject<byte[], CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            int controlPointBufferSize = 0;

            while (true) {
                BluetoothLog.d("waitOnControlPointResult: waiting");
                SystemClock.sleep(1000);

                if (mControlPointBuffer.size() == controlPointBufferSize) {
                    break;
                } else {
                    controlPointBufferSize = mControlPointBuffer.size();
                }
            }

            byte[] data = Bytes.toArray(mControlPointBuffer);
            mControlPointBuffer.clear();

            Log.i(TAG, String.format("waitOnControlPointResult: %d bytes", data.length));

            deferredObject.resolve(data);
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    private Promise<Void, CardException, Void> fileWrite(byte[] data) {
        Log.i(TAG, String.format("fileWrite: %d bytes", data.length));
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            int round = 0;
            int offset = 0;

            int chunkMaxSize = mMtu - 3;

            Phaser phaser = new Phaser();

            do {
                int chunkSize = (data.length - offset) > chunkMaxSize ? chunkMaxSize : data.length - offset;
                byte[] chunk = Arrays.copyOfRange(data, offset, offset + chunkSize);

                Log.i(TAG, String.format("fileWrite <- %d bytes (round %d)", chunk.length, round));

                int phase = phaser.register();

                BluetoothClient.characteristicWrite(SERVICE_UUID, FILE_WRITE_UUID, chunk, new CharacteristicWriteCallback() {
                    @Override
                    public void onCharacteristicNotFound() {
                        deferredObject.reject(new CardException(CardError.CHARACTERISTIC_WRITE_FAILURE));
                        phaser.arriveAndDeregister();
                    }

                    @Override
                    public void onCharacteristicWrite(int status) {
                        if (status == GATT_SUCCESS) {
                            Log.i(TAG, "fileWrite <- success");
                        } else {
                            deferredObject.reject(new CardException(CardError.CHARACTERISTIC_WRITE_FAILURE));
                        }

                        phaser.arriveAndDeregister();
                    }

                    @Override
                    public void onNotConnected() {
                        deferredObject.reject(new CardException(CardError.CHARACTERISTIC_WRITE_FAILURE));
                        phaser.arriveAndDeregister();
                    }

                    @Override
                    public void onTimeout() {
                        deferredObject.reject(new CardException(CardError.CHARACTERISTIC_WRITE_FAILURE));
                        phaser.arriveAndDeregister();
                    }
                });

                try {
                    phaser.awaitAdvanceInterruptibly(phase, 1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | TimeoutException e) {
                    break;
                }

                if (!deferredObject.isPending()) {
                    break;
                }

                round += 1;
                offset += chunkSize;
            } while (offset < data.length);

            Log.d(TAG, "fileWrite: complete");

            if (deferredObject.isPending()) {
                Log.d(TAG, "fileWrite: resolve");
                deferredObject.resolve(null);
            }
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    private Promise<Void, CardException, Void> writeToControlPoint(byte[] data) {
        Log.d(TAG, "writeToControlPoint");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            mControlPointBuffer.clear();

            String hexString = BaseEncoding.base16().encode(data);
            Log.d(TAG, String.format("writeToControlPoint: %s", hexString));

            BluetoothClient.characteristicWrite(SERVICE_UUID, CONTROL_POINT_UUID, data, new CharacteristicWriteCallback() {
                @Override
                public void onCharacteristicNotFound() {
                    deferredObject.reject(new CardException(CardError.CHARACTERISTIC_WRITE_FAILURE));
                }

                @Override
                public void onCharacteristicWrite(int status) {
                    if (status == GATT_SUCCESS) {
                        Log.d(TAG, "onCharacteristicWrite: success");
                        deferredObject.resolve(null);
                    } else {
                        Log.d(TAG, "onCharacteristicWrite: fail");
                        deferredObject.reject(new CardException(CardError.CHARACTERISTIC_WRITE_FAILURE));
                    }
                }

                @Override
                public void onNotConnected() {
                    deferredObject.reject(new CardException(CardError.CHARACTERISTIC_WRITE_FAILURE));
                }

                @Override
                public void onTimeout() {
                    deferredObject.reject(new CardException(CardError.CHARACTERISTIC_WRITE_FAILURE));
                }
            });
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> connect(Context context) {
        Log.d(TAG, "connect");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            mMtu = 20;

            Log.d(TAG, "(1/4) Connecting ...");
            BluetoothClient.connect(context, mAddress, new ConnectCallback() {
                @Override
                public void onConnectionStateChange(int status, int newState) {
                    if (status == GATT_SUCCESS) {
                        Log.d(TAG, "(2/4) Request MTU ...");
                        BluetoothClient.requestMtu(512, new RequestMtuCallback() {
                            @Override
                            public void onMtuChanged(int mtu, int status) {
                                Log.d(TAG, "onMtuChanged");
                                if (status == GATT_SUCCESS) {
                                    mMtu = mtu;
                                    Log.d(TAG, "onMtuChanged: " + mMtu);

                                    Log.d(TAG, "(3/4) Discover services ...");
                                    BluetoothClient.discoverServices(new DiscoverServicesCallback() {
                                        @Override
                                        public void onServicesDiscovered(int status) {
                                            Log.i(TAG, "(4/4) Enable control point notifications ...");
                                            BluetoothClient.descriptorWrite(SERVICE_UUID, CONTROL_POINT_UUID, CHARACTERISTIC_CONFIGURATION_UUID, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, new DescriptorWriteCallback() {
                                                @Override
                                                public void onDescriptorNotFound() {
                                                    deferredObject.reject(null);
                                                }

                                                @Override
                                                public void onDescriptorWrite(int status) {
                                                    if (status == GATT_SUCCESS) {
                                                        if (BluetoothClient.enableNotify(SERVICE_UUID, CONTROL_POINT_UUID)) {
                                                            BluetoothClient.notify((serviceUUID, characteristicUUID, value) -> {
                                                                if (characteristicUUID.equals(CONTROL_POINT_UUID)) {
                                                                    Log.d(TAG, String.format("controlPointBuffer <- %d bytes", value.length));
                                                                    mControlPointBuffer.addAll(Bytes.asList(value));
                                                                } else {
                                                                    Log.d(TAG, "Notification callback received a value for unknown service " + serviceUUID + ", characteristic " + characteristicUUID);
                                                                }
                                                            });
                                                            deferredObject.resolve(null);
                                                        } else {
                                                            deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                                                        }
                                                    } else {
                                                        deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                                                    }
                                                }

                                                @Override
                                                public void onNotConnected() {
                                                    deferredObject.reject(null);
                                                }

                                                @Override
                                                public void onTimeout() {
                                                    deferredObject.reject(null);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onNotConnected() {
                                            deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                                        }

                                        @Override
                                        public void onTimeout() {
                                            deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                                        }
                                    });
                                } else {
                                    deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                                }
                            }

                            @Override
                            public void onNotConnected() {
                                Log.i(TAG, "onNotConnected");
                                deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                            }

                            @Override
                            public void onTimeout() {
                                Log.i(TAG, "onTimeout");
                                deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                            }
                        });
                    } else {
                        deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                    }
                }

                @Override
                public void onBluetoothNotSupported() {
                    deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                }

                @Override
                public void onTimeout() {
                    deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                }
            });
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> disconnect() {
        Log.i(TAG, "disconnect");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            BluetoothClient.disconnect(new DisconnectCallback() {
                @Override
                public void onDisconnected() {
                    Log.d(TAG, "disconnect: success");
                }

                @Override
                public void onNotConnected() {
                    Log.d(TAG, "disconnect: not connected");
                }

                @Override
                public void onTimeout() {
                    Log.d(TAG, "disconnect: timeout");
                }
            });
        };
        new Thread(runnable).start();

        deferredObject.resolve(null);

        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> checkBluetoothState() {
        Log.i(TAG, "checkBluetoothState");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                if (BluetoothClient.getBondState(mAddress) == BluetoothDevice.BOND_BONDED) {
                    deferredObject.resolve(null);
                } else {
                    Log.i(TAG, "checkBluetoothState: LE is not paired.");
                    deferredObject.reject(new CardException(CardError.CARD_NOT_PAIRED));
                }
            } else {
                Log.i(TAG, "checkBluetoothState: Bluetooth adapter not enabled.");
                deferredObject.reject(new CardException(CardError.BLUETOOTH_ADAPTER_NOT_ENABLED));
            }
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> delete(String path) {
        Log.d(TAG, "delete");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            if (path.length() <= 30) {
                makeCommandData((byte) 7, path).then(
                        this::writeToControlPoint
                ).then(
                        deferredObject::resolve
                ).fail(
                        deferredObject::reject
                );
            } else {
                deferredObject.reject(new CardException(CardError.ARGUMENT_INVALID));
            }
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public Promise<Boolean, CardException, Void> exists(String path) {
        Log.d(TAG, "exists");
        DeferredObject<Boolean, CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            if (path.length() <= 30) {
                makeCommandData((byte) 10, path).then(
                        this::writeToControlPoint
                ).then((DonePipe<Void, byte[], CardException, Void>) result ->
                        waitOnControlPointResult()
                ).then(result -> {
                    String hexString = BaseEncoding.base16().encode(result);
                    if (result.length == 1) {
                        if (result[0] == 0x06) {
                            Log.d(TAG, String.format("exists: %s (true)", hexString));
                            deferredObject.resolve(true);
                        } else if (result[0] == 0x07) {
                            Log.d(TAG, String.format("exists: %s (false)", hexString));
                            deferredObject.resolve(false);
                        } else {
                            deferredObject.reject(new CardException(CardError.INVALID_RESPONSE));
                        }
                    } else {
                        deferredObject.reject(new CardException(CardError.INVALID_RESPONSE));
                    }
                }).fail(
                        deferredObject::reject
                );
            } else {
                deferredObject.reject(new CardException(CardError.ARGUMENT_INVALID));
            }
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public Promise<byte[], CardException, Void> get(String path) {
        Log.d(TAG, "get");
        DeferredObject<byte[], CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            if (path.length() <= 30) {
                makeCommandData((byte) 2, path).then(
                        this::writeToControlPoint
                ).then((DonePipe<Void, byte[], CardException, Void>) result ->
                        waitOnControlPointResult()
                ).then(result -> {
                    if (result.length == 1) {
                        if (result[0] == 0x07) {
                            deferredObject.reject(new CardException(CardError.FILE_NOT_FOUND));
                        } else {
                            deferredObject.resolve(result);
                        }
                    } else {
                        deferredObject.resolve(result);
                    }
                }).fail(
                        deferredObject::reject
                );
            } else {
                deferredObject.reject(new CardException(CardError.ARGUMENT_INVALID));
            }
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> rename(String name) {
        Log.d(TAG, "rename");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            if (name.length() <= 11) {
                makeCommandData((byte) 8, name).then(
                        this::writeToControlPoint
                ).then(
                        deferredObject::resolve
                ).fail(
                        deferredObject::reject
                );
            } else {
                deferredObject.reject(new CardException(CardError.ARGUMENT_INVALID));
            }
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> checksum(byte[] data) {
        Log.d(TAG, "checksum");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            int ourChecksum = GKCrypto.crc16(data);

            BluetoothClient.characteristicRead(SERVICE_UUID, FILE_WRITE_UUID, new CharacteristicReadCallback() {
                @Override
                public void onCharacteristicNotFound() {
                    deferredObject.reject(new CardException(CardError.CHARACTERISTIC_READ_FAILURE));
                }

                @Override
                public void onCharacteristicRead(int status, byte[] value) {
                    if (status == GATT_SUCCESS) {
                        if (value.length == 2) {
                            String ourHexString = BaseEncoding.base16().encode(new byte[]{(byte) (ourChecksum >>> 8), (byte) ourChecksum});
                            String cardHexString = BaseEncoding.base16().encode(value);

                            int cardChecksum = ((value[0] << 8) & 0x0000ff00) | (value[1] & 0x000000ff);

                            Log.d(TAG, String.format("checksum: %s (%d, ours)", ourHexString, ourChecksum));
                            Log.d(TAG, String.format("checksum: %s (%d, card)", cardHexString, cardChecksum));

                            if (ourChecksum == cardChecksum) {
                                Log.d(TAG, "checksum: match");
                                deferredObject.resolve(null);
                            } else {
                                Log.d(TAG, "checksum: invalid");
                                deferredObject.reject(new CardException(CardError.INVALID_CHECKSUM));
                            }
                        } else {
                            deferredObject.reject(new CardException(CardError.INVALID_CHECKSUM));
                        }
                    } else {
                        deferredObject.reject(new CardException(CardError.CHARACTERISTIC_READ_FAILURE));
                    }
                }

                @Override
                public void onDisconnected() {
                    deferredObject.reject(new CardException(CardError.CHARACTERISTIC_READ_FAILURE));
                }

                @Override
                public void onTimeout() {
                    deferredObject.reject(new CardException(CardError.CHARACTERISTIC_READ_FAILURE));
                }
            });
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> close(String path) {
        Log.d(TAG, "close");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            if (path.length() <= 30) {
                makeCommandData((byte) 4, path).then(
                        this::writeToControlPoint
                ).then((DonePipe<Void, byte[], CardException, Void>) result ->
                        waitOnControlPointResult()
                ).then(result -> {
                    deferredObject.resolve(null);
                }).fail(
                        deferredObject::reject
                );
            } else {
                deferredObject.reject(new CardException(CardError.ARGUMENT_INVALID));
            }
        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> put(byte[] data) {
        Log.d(TAG, "put");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();

        Runnable runnable = () -> {
            makeCommandData((byte) 3, null).then(
                    this::writeToControlPoint
            ).then((DonePipe<Void, byte[], CardException, Void>) result ->
                    waitOnControlPointResult()
            ).then((DonePipe<byte[], Void, CardException, Void>) result ->
                    fileWrite(data)
            ).then(result -> {
                deferredObject.resolve(null);
            }).fail(deferredObject::reject);

        };
        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public enum CardError {
        ARGUMENT_INVALID,
        BLUETOOTH_NOT_AVAILABLE,
        BLUETOOTH_ADAPTER_NOT_ENABLED,
        CARD_NOT_PAIRED,
        CONNECTION_FAILED,
        CHARACTERISTIC_READ_FAILURE,
        CHARACTERISTIC_WRITE_FAILURE,
        FILE_NOT_FOUND,
        FILE_READ_FAILED,
        FILE_WRITE_FAILED,
        MAKE_COMMAND_DATA_FAILED,
        INVALID_CHECKSUM,
        INVALID_RESPONSE
    }

    public static class CardException extends Exception {
        final CardError mError;

        CardException(CardError error) {
            mError = error;
        }

        public CardError getError() {
            return mError;
        }
    }
}
