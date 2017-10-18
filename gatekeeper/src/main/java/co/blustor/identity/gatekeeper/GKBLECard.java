package co.blustor.identity.gatekeeper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;

import org.jdeferred.DonePipe;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import co.blustor.gatekeepersdk.devices.GKBluetoothCard;
import co.blustor.gatekeepersdk.devices.GKCard;
import co.blustor.identity.utils.FileUtils;

public class GKBLECard {

    public static final UUID SERVICE_UUID = UUID.fromString("423AD87A-B100-4F14-9EAA-5EB5839F2A54");
    private static final UUID CONTROL_POINT_UUID = UUID.fromString("423AD87A-0001-4F14-9EAA-5EB5839F2A54");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "GKBLECard";
    private static final String BLUETOOTH_CARD_NAME = "CYBERGATE";
    private final Context mContext;
    private final List<Byte> mControlPointBuffer = new ArrayList<>();
    private final BluetoothManager mBluetoothManager;
    private final BluetoothDevice mBluetoothDevice;
    private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
    private Queue<OnCompleteListener> mControlPointWriteListeners = new LinkedList<>();
    @Nullable
    private BluetoothGatt mBluetoothGatt;
    @Nullable
    private BluetoothGattService mService;
    @Nullable
    private BluetoothGattCharacteristic mControlPointCharacteristic;
    @Nullable
    private BluetoothGattDescriptor mControlPointCharacteristicConfigurationDescriptor;

    public GKBLECard(Context context, String macAddress) throws CardException {
        Log.i(TAG, "init");
        mContext = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (mBluetoothManager == null) {
            throw new CardException(CardError.BLUETOOTH_NOT_AVAILABLE);
        } else {
            BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
            mBluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);
        }
    }

    private Promise<byte[], CardException, Void> makeCommandData(byte command, @Nullable String string) {
        Log.i(TAG, "makeCommandData");
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
                deferredObject.resolve(data);
            } catch (IOException e) {
                deferredObject.reject(new CardException(CardError.MAKE_COMMAND_DATA_FAILED));
            }
        };
        new Thread(runnable).start();
        return deferredObject.promise();
    }

    private Promise<byte[], CardException, Void> waitOnControlPointResult() {
        Log.i(TAG, "waitOnControlPointResult");
        DeferredObject<byte[], CardException, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            int controlPointBufferSize = 0;

            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

                if (mControlPointBuffer.size() == controlPointBufferSize) {
                    break;
                } else {
                    controlPointBufferSize = mControlPointBuffer.size();
                }
            }

            byte[] data = Bytes.toArray(mControlPointBuffer);
            mControlPointBuffer.clear();

            Log.i(TAG, "waitOnControlPointResult: " + data.length + " bytes");

            deferredObject.resolve(data);
        };
        new Thread(runnable).start();
        return deferredObject.promise();
    }

    private Promise<byte[], CardException, Void> fileRead(String path) {
        Log.i(TAG, "fileRead: " + path);
        DeferredObject<byte[], CardException, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            GKBluetoothCard bluetoothCard = new GKBluetoothCard(BLUETOOTH_CARD_NAME, mContext.getCacheDir());
            try {
                bluetoothCard.connect();
            } catch (IOException e) {
                e.printStackTrace();
                deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                return;
            }

            switch (bluetoothCard.getConnectionState()) {
                case BLUETOOTH_DISABLED:
                    deferredObject.reject(new CardException(CardError.BLUETOOTH_NOT_ENABLED));
                    return;
                case CARD_NOT_PAIRED:
                    deferredObject.reject(new CardException(CardError.CARD_NOT_PAIRED));
                    return;
                case CONNECTING:
                    break;
                case CONNECTED:
                    break;
                case TRANSFERRING:
                    break;
                case DISCONNECTING:
                    break;
                case DISCONNECTED:
                    break;
            }

            try {
                GKCard.Response response = bluetoothCard.get(path);
                bluetoothCard.disconnect();

                File file = response.getDataFile();
                byte[] bytes = FileUtils.read(file);

                Log.i(TAG, "fileRead: " + bytes.length + " bytes");

                deferredObject.resolve(bytes);
            } catch (IOException e) {
                e.printStackTrace();
                deferredObject.reject(new CardException(CardError.FILE_READ_FAILED));
            }
        };
        new Thread(runnable).start();
        return deferredObject.promise();
    }

    private Promise<Void, CardException, Void> fileWrite(String path, byte[] data) {
        Log.i(TAG, "fileWrite: " + data.length + " bytes to " + path);
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            GKBluetoothCard bluetoothCard = new GKBluetoothCard(BLUETOOTH_CARD_NAME, mContext.getCacheDir());
            try {
                bluetoothCard.connect();
            } catch (IOException e) {
                e.printStackTrace();
                deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                return;
            }

            switch (bluetoothCard.getConnectionState()) {
                case BLUETOOTH_DISABLED:
                    deferredObject.reject(new CardException(CardError.BLUETOOTH_NOT_ENABLED));
                    return;
                case CARD_NOT_PAIRED:
                    deferredObject.reject(new CardException(CardError.CARD_NOT_PAIRED));
                    return;
                case CONNECTING:
                    break;
                case CONNECTED:
                    break;
                case TRANSFERRING:
                    break;
                case DISCONNECTING:
                    break;
                case DISCONNECTED:
                    break;
            }

            try {
                bluetoothCard.put(path, new ByteArrayInputStream(data));
                deferredObject.resolve(null);
            } catch (IOException e) {
                e.printStackTrace();
                deferredObject.reject(new CardException(CardError.FILE_WRITE_FAILED));
            }
        };
        new Thread(runnable).start();
        return deferredObject.promise();
    }

    private Promise<Void, CardException, Void> writeToControlPoint(byte[] data) {
        Log.i(TAG, "writeToControlPoint");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            if (mBluetoothGatt == null || mControlPointCharacteristic == null) {
                deferredObject.reject(new CardException(CardError.CONNECTION_REQUIRED));
            } else {
                String hexString = BaseEncoding.base16().encode(data);
                Log.i(TAG, "writeToControlPoint: " + hexString);

                mControlPointCharacteristic.setValue(data);
                mControlPointWriteListeners.add(new OnCompleteListener() {
                    @Override
                    public void success() {
                        deferredObject.resolve(null);
                    }

                    @Override
                    public void failure() {
                        deferredObject.reject(new CardException(CardError.CHARACTERISTIC_WRITE_FAILURE));
                    }
                });
                mBluetoothGatt.writeCharacteristic(mControlPointCharacteristic);
            }
        };
        new Thread(runnable).start();
        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> connect() {
        Log.i("GKBLECard", "connect");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            if (mBluetoothGatt == null) {
                mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        Log.i("GKBLECard", "onConnectionStateChange: " + status + " (status) " + newState + " (newState)");
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED) {
                                    Log.i("GKBLECard", "onConnectionStateChange: discovering services ...");
                                    gatt.discoverServices();
                                }
                            }

                            mConnectionState = newState;
                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        Log.i("GKBLECard", "onServicesDiscovered");
                        mService = gatt.getService(SERVICE_UUID);
                        mControlPointCharacteristic = mService.getCharacteristic(CONTROL_POINT_UUID);

                        if (mControlPointCharacteristic == null) {
                            deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                        } else {
                            mControlPointCharacteristicConfigurationDescriptor = mControlPointCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);
                            mControlPointCharacteristicConfigurationDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(mControlPointCharacteristicConfigurationDescriptor);

                            gatt.setCharacteristicNotification(mControlPointCharacteristic, true);
                        }
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        if (characteristic.getUuid().equals(CONTROL_POINT_UUID)) {
                            Log.i("GKBLECard", "controlPointBuffer -> +" + characteristic.getValue().length + " bytes");
                            mControlPointBuffer.addAll(Bytes.asList(characteristic.getValue()));
                        }
                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            Log.i("GKBLECard", "onCharacteristicWrite: success");
                            if (characteristic.getUuid().equals(CONTROL_POINT_UUID)) {
                                while (mControlPointWriteListeners.size() > 0) {
                                    mControlPointWriteListeners.remove().success();
                                }
                            }
                        } else {
                            Log.i("GKBLECard", "onCharacteristicWrite: failure");
                            if (characteristic.getUuid().equals(CONTROL_POINT_UUID)) {
                                while (mControlPointWriteListeners.size() > 0) {
                                    mControlPointWriteListeners.remove().failure();
                                }
                            }
                        }
                    }

                    @Override
                    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                        Log.i("GKBLECard", "onDescriptorWrite: " + descriptor.getUuid());
                        if (descriptor.getUuid().equals(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID)) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                if (deferredObject.isPending()) {
                                    deferredObject.resolve(null);
                                }
                            } else {
                                if (deferredObject.isPending()) {
                                    deferredObject.reject(new CardException(CardError.CONNECTION_FAILED));
                                }
                            }
                        }
                    }
                });

                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (deferredObject.isPending()) {
                    deferredObject.reject(new CardException(CardError.CONNECTION_TIMEOUT));
                }
            } else {
                Log.i("GKBLECard", "connect: connect (already established)");
                mBluetoothGatt.connect();
            }
        };
        new Thread(runnable).start();
        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> disconnect() {
        Log.i(TAG, "disconnect");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.disconnect();
            }

            deferredObject.resolve(null);
        };
        new Thread(runnable).start();
        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> checkBluetoothState() {
        Log.i(TAG, "checkBluetoothState");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            if (mBluetoothManager.getAdapter().isEnabled()) {
                deferredObject.resolve(null);
            } else {
                deferredObject.reject(new CardException(CardError.BLUETOOTH_NOT_ENABLED));
            }
        };
        new Thread(runnable).start();
        return deferredObject.promise();
    }

    public Promise<Void, CardException, Void> delete(String path) {
        Log.i(TAG, "delete");
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
        Log.i(TAG, "exists");
        DeferredObject<Boolean, CardException, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            if (path.length() <= 30) {
                makeCommandData((byte) 10, path).then(
                        this::writeToControlPoint
                ).then((DonePipe<Void, byte[], CardException, Void>) result ->
                        waitOnControlPointResult()
                ).then(result -> {
                    if (result.length == 1) {
                        if (result[0] == 0x06) {
                            deferredObject.resolve(true);
                        } else if (result[0] == 0x07) {
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
        Log.i(TAG, "get");
        DeferredObject<byte[], CardException, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            if (path.length() <= 30) {
                makeCommandData((byte) 1, path).then(
                        this::writeToControlPoint
                ).then((DonePipe<Void, byte[], CardException, Void>) result ->
                        waitOnControlPointResult()
                ).then((DonePipe<byte[], byte[], CardException, Void>) result ->
                        fileRead(path)
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
        Log.i(TAG, "rename");
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

    public Promise<Void, CardException, Void> put(String path, byte[] data) {
        Log.i(TAG, "put");
        DeferredObject<Void, CardException, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            if (path.length() <= 30) {
                makeCommandData((byte) 1, null).then(
                        this::writeToControlPoint
                ).then((DonePipe<Void, byte[], CardException, Void>) result ->
                        waitOnControlPointResult()
                ).then((DonePipe<byte[], Void, CardException, Void>) result ->
                        fileWrite(path, data)
                ).then(result -> {
                            deferredObject.resolve(null);
                        }
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

    public enum CardError {
        ARGUMENT_INVALID,
        BLUETOOTH_NOT_AVAILABLE,
        BLUETOOTH_NOT_ENABLED,
        CARD_NOT_PAIRED,
        CONNECTION_FAILED,
        CONNECTION_REQUIRED,
        CONNECTION_TIMEOUT,
        CHARACTERISTIC_WRITE_FAILURE,
        FILE_NOT_FOUND,
        FILE_READ_FAILED,
        FILE_WRITE_FAILED,
        MAKE_COMMAND_DATA_FAILED,
        INVALID_RESPONSE
    }

    interface OnCompleteListener {
        void success();

        void failure();
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
