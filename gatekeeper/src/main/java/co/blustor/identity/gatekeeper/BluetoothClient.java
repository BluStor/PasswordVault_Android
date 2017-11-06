package co.blustor.identity.gatekeeper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.Nullable;

import com.google.common.io.BaseEncoding;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import co.blustor.identity.gatekeeper.BluetoothConstants.EventName;
import co.blustor.identity.gatekeeper.callbacks.CharacteristicReadCallback;
import co.blustor.identity.gatekeeper.callbacks.CharacteristicWriteCallback;
import co.blustor.identity.gatekeeper.callbacks.ConnectCallback;
import co.blustor.identity.gatekeeper.callbacks.DescriptorReadCallback;
import co.blustor.identity.gatekeeper.callbacks.DescriptorWriteCallback;
import co.blustor.identity.gatekeeper.callbacks.DisconnectCallback;
import co.blustor.identity.gatekeeper.callbacks.DiscoverServicesCallback;
import co.blustor.identity.gatekeeper.callbacks.NotifyCallback;
import co.blustor.identity.gatekeeper.callbacks.RequestMtuCallback;
import co.blustor.identity.gatekeeper.events.CharacteristicReadEvent;
import co.blustor.identity.gatekeeper.events.CharacteristicWriteEvent;
import co.blustor.identity.gatekeeper.events.ConnectEvent;
import co.blustor.identity.gatekeeper.events.DescriptorReadEvent;
import co.blustor.identity.gatekeeper.events.DescriptorWriteEvent;
import co.blustor.identity.gatekeeper.events.DisconnectEvent;
import co.blustor.identity.gatekeeper.events.DiscoverServicesEvent;
import co.blustor.identity.gatekeeper.events.Event;
import co.blustor.identity.gatekeeper.events.RequestMtuEvent;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

public class BluetoothClient {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final CyclicBarrier CYCLIC_BARRIER = new CyclicBarrier(2);
    @Nullable
    private static BluetoothDevice DEVICE = null;
    @Nullable
    private static BluetoothGatt GATT = null;
    @Nullable
    private static NotifyCallback NOTIFY_CALLBACK = null;
    @Nullable
    private static CharacteristicReadCallback CHARACTERISTIC_READ_CALLBACK = null;
    @Nullable
    private static CharacteristicWriteCallback CHARACTERISTIC_WRITE_CALLBACK = null;
    @Nullable
    private static ConnectCallback CONNECT_CALLBACK = null;
    @Nullable
    private static DescriptorReadCallback DESCRIPTOR_READ_CALLBACK = null;
    @Nullable
    private static DescriptorWriteCallback DESCRIPTOR_WRITE_CALLBACK = null;
    @Nullable
    private static DisconnectCallback DISCONNECT_CALLBACK = null;
    @Nullable
    private static DiscoverServicesCallback DISCOVER_SERVICES_CALLBACK = null;
    @Nullable
    private static RequestMtuCallback REQUEST_MTU_CALLBACK = null;
    private static final BluetoothGattCallback CALLBACK = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothLog.d("onConnectionStateChange");
            if (newState == STATE_CONNECTED || newState == 133) {
                if (CONNECT_CALLBACK != null) {
                    BluetoothLog.d("onConnectionStateChange: CONNECT_CALLBACK");
                    CONNECT_CALLBACK.onConnectionStateChange(status, newState);
                    try {
                        CYCLIC_BARRIER.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            } else if (newState == STATE_DISCONNECTED) {
                if (DISCONNECT_CALLBACK != null) {
                    BluetoothLog.d("onConnectionStateChange: DISCONNECT_CALLBACK");
                    DISCONNECT_CALLBACK.onDisconnected();
                    try {
                        CYCLIC_BARRIER.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                BluetoothLog.d(String.format(Locale.getDefault(), "onConnectionStateChange: Unknown status %d, newState = %d", status, newState));
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothLog.d("onServicesDiscovered");
            if (DISCOVER_SERVICES_CALLBACK != null) {
                BluetoothLog.d("onServicesDiscovered: DISCOVER_SERVICES_CALLBACK");
                DISCOVER_SERVICES_CALLBACK.onServicesDiscovered(status);
                try {
                    CYCLIC_BARRIER.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BluetoothLog.d("onCharacteristicRead");
            if (CHARACTERISTIC_READ_CALLBACK != null) {
                BluetoothLog.d("onCharacteristicRead: CHARACTERISTIC_READ_CALLBACK");
                CHARACTERISTIC_READ_CALLBACK.onCharacteristicRead(status, characteristic.getValue());
                try {
                    CYCLIC_BARRIER.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BluetoothLog.d("onCharacteristicWrite");
            if (CHARACTERISTIC_WRITE_CALLBACK != null) {
                BluetoothLog.d("onCharacteristicWrite: CHARACTERISTIC_WRITE_CALLBACK");
                CHARACTERISTIC_WRITE_CALLBACK.onCharacteristicWrite(status);
                try {
                    CYCLIC_BARRIER.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String hexString = BaseEncoding.base16().encode(characteristic.getValue());
            BluetoothLog.d(String.format("onCharacteristicChanged: %s to %s", characteristic.getUuid(), hexString));
            if (NOTIFY_CALLBACK != null) {
                BluetoothLog.d("onCharacteristicChanged: NOTIFY_CALLBACK");
                BluetoothGattService service = characteristic.getService();
                NOTIFY_CALLBACK.onNotify(service.getUuid(), characteristic.getUuid(), characteristic.getValue());
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BluetoothLog.d("onDescriptorRead");
            if (DESCRIPTOR_READ_CALLBACK != null) {
                BluetoothLog.d("onDescriptorRead: DESCRIPTOR_READ_CALLBACK");
                DESCRIPTOR_READ_CALLBACK.onDescriptorRead(status, descriptor.getValue());
                try {
                    CYCLIC_BARRIER.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BluetoothLog.d("onDescriptorWrite");
            if (DESCRIPTOR_WRITE_CALLBACK != null) {
                BluetoothLog.d("onDescriptorWrite: DESCRIPTOR_WRITE_CALLBACK");
                DESCRIPTOR_WRITE_CALLBACK.onDescriptorWrite(status);
                try {
                    CYCLIC_BARRIER.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            BluetoothLog.d("onMtuChanged");
            if (REQUEST_MTU_CALLBACK != null) {
                BluetoothLog.d("onMtuChanged: REQUEST_MTU_CALLBACK");
                REQUEST_MTU_CALLBACK.onMtuChanged(mtu, status);
                try {
                    CYCLIC_BARRIER.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    // Event processing

    @Nullable
    private static BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt, UUID serviceUUID, UUID characteristicUUID) {
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service == null) {
            return null;
        } else {
            return service.getCharacteristic(characteristicUUID);
        }
    }

    @Nullable
    private static BluetoothGattDescriptor getDescriptor(BluetoothGatt gatt, UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID) {
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service == null) {
            return null;
        } else {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            if (characteristic == null) {
                return null;
            } else {
                return characteristic.getDescriptor(descriptorUUID);
            }
        }
    }

    private static void queueEvent(Event event) {
        EXECUTOR_SERVICE.execute(() -> {

            CYCLIC_BARRIER.reset();

            EventName eventName = event.getName();
            BluetoothLog.d("Processing " + eventName.name());
            switch (eventName) {
                case CHARACTERISTIC_READ:
                    CharacteristicReadEvent characteristicReadEvent = (CharacteristicReadEvent) event;
                    CHARACTERISTIC_READ_CALLBACK = characteristicReadEvent.getCallback();

                    if (GATT == null) {
                        BluetoothLog.d("Not connected.");
                        if (CHARACTERISTIC_READ_CALLBACK != null) {
                            CHARACTERISTIC_READ_CALLBACK.onDisconnected();
                        }
                    } else {
                        BluetoothGattCharacteristic characteristic = getCharacteristic(GATT, characteristicReadEvent.getServiceUUID(), characteristicReadEvent.getCharacteristicUUID());
                        if (characteristic == null) {
                            if (CHARACTERISTIC_READ_CALLBACK != null) {
                                CHARACTERISTIC_READ_CALLBACK.onCharacteristicNotFound();
                            }
                        } else {
                            GATT.readCharacteristic(characteristic);
                            try {
                                CYCLIC_BARRIER.await(1000, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException | BrokenBarrierException e) {
                                if (CHARACTERISTIC_READ_CALLBACK != null) {
                                    CHARACTERISTIC_READ_CALLBACK.onInterrupted();
                                }
                            } catch (TimeoutException e) {
                                if (CHARACTERISTIC_READ_CALLBACK != null) {
                                    CHARACTERISTIC_READ_CALLBACK.onTimeout();
                                }
                            }
                        }
                    }
                    break;
                case CHARACTERISTIC_WRITE:
                    CharacteristicWriteEvent characteristicWriteEvent = (CharacteristicWriteEvent) event;
                    CHARACTERISTIC_WRITE_CALLBACK = characteristicWriteEvent.getCallback();

                    if (GATT == null) {
                        if (CHARACTERISTIC_WRITE_CALLBACK != null) {
                            CHARACTERISTIC_WRITE_CALLBACK.onNotConnected();
                        }
                    } else {
                        BluetoothGattCharacteristic characteristic = getCharacteristic(GATT, characteristicWriteEvent.getServiceUUID(), characteristicWriteEvent.getCharacteristicUUID());
                        if (characteristic == null) {
                            if (CHARACTERISTIC_WRITE_CALLBACK != null) {
                                CHARACTERISTIC_WRITE_CALLBACK.onCharacteristicNotFound();
                            }
                        } else {
                            byte[] value = characteristicWriteEvent.getValue();
                            String hexString = BaseEncoding.base16().encode(value);

                            BluetoothLog.d(String.format("writeDescriptor: %s to %s", hexString, characteristic.getUuid()));
                            characteristic.setValue(characteristicWriteEvent.getValue());

                            GATT.writeCharacteristic(characteristic);
                            try {
                                CYCLIC_BARRIER.await(1000, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException | BrokenBarrierException e) {
                                if (CHARACTERISTIC_WRITE_CALLBACK != null) {
                                    CHARACTERISTIC_WRITE_CALLBACK.onInterrupted();
                                }
                            } catch (TimeoutException e) {
                                if (CHARACTERISTIC_WRITE_CALLBACK != null) {
                                    CHARACTERISTIC_WRITE_CALLBACK.onTimeout();
                                }
                            }
                        }
                    }
                    break;
                case CONNECT:
                    ConnectEvent connectEvent = (ConnectEvent) event;
                    CONNECT_CALLBACK = connectEvent.getCallback();

                    String address = connectEvent.getAddress();
                    DEVICE = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);

                    if (DEVICE == null) {
                        if (CONNECT_CALLBACK != null) {
                            CONNECT_CALLBACK.onBluetoothNotSupported();
                        }
                    } else {
                        if (GATT == null) {
                            GATT = DEVICE.connectGatt(connectEvent.getContext(), false, CALLBACK);
                        } else {
                            GATT.close();
                            GATT = DEVICE.connectGatt(connectEvent.getContext(), false, CALLBACK);
                        }

                        try {
                            CYCLIC_BARRIER.await(10000, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException | BrokenBarrierException e) {
                            if (CONNECT_CALLBACK != null) {
                                CONNECT_CALLBACK.onInterrupted();
                            }
                        } catch (TimeoutException e) {
                            if (CONNECT_CALLBACK != null) {
                                CONNECT_CALLBACK.onTimeout();
                            }
                        }
                    }
                    break;
                case DESCRIPTOR_READ:
                    DescriptorReadEvent descriptorReadEvent = (DescriptorReadEvent) event;
                    DESCRIPTOR_READ_CALLBACK = descriptorReadEvent.getCallback();

                    if (GATT == null) {
                        if (DESCRIPTOR_READ_CALLBACK != null) {
                            DESCRIPTOR_READ_CALLBACK.onNotConnected();
                        }
                    } else {
                        BluetoothGattDescriptor descriptor = getDescriptor(GATT, descriptorReadEvent.getServiceUUID(), descriptorReadEvent.getCharacteristicUUID(), descriptorReadEvent.getDescriptorUUID());
                        if (descriptor == null) {
                            if (DESCRIPTOR_READ_CALLBACK != null) {
                                DESCRIPTOR_READ_CALLBACK.onDescriptorNotFound();
                            }
                        } else {
                            GATT.readDescriptor(descriptor);
                            try {
                                CYCLIC_BARRIER.await(1000, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException | BrokenBarrierException e) {
                                if (DESCRIPTOR_READ_CALLBACK != null) {
                                    DESCRIPTOR_READ_CALLBACK.onInterrupted();
                                }
                            } catch (TimeoutException e) {
                                if (DESCRIPTOR_READ_CALLBACK != null) {
                                    DESCRIPTOR_READ_CALLBACK.onTimeout();
                                }
                            }
                        }
                    }
                    break;
                case DESCRIPTOR_WRITE:
                    DescriptorWriteEvent descriptorWriteEvent = (DescriptorWriteEvent) event;
                    DESCRIPTOR_WRITE_CALLBACK = descriptorWriteEvent.getCallback();

                    if (GATT == null) {
                        if (DESCRIPTOR_WRITE_CALLBACK != null) {
                            DESCRIPTOR_WRITE_CALLBACK.onNotConnected();
                        }
                    } else {
                        BluetoothGattDescriptor descriptor = getDescriptor(GATT, descriptorWriteEvent.getServiceUUID(), descriptorWriteEvent.getCharacteristicUUID(), descriptorWriteEvent.getDescriptorUUID());
                        if (descriptor == null) {
                            if (DESCRIPTOR_WRITE_CALLBACK != null) {
                                DESCRIPTOR_WRITE_CALLBACK.onDescriptorNotFound();
                            }
                        } else {
                            byte[] value = descriptorWriteEvent.getValue();
                            String hexString = BaseEncoding.base16().encode(value);
                            BluetoothLog.d(String.format("writeDescriptor: %s to %s", hexString, descriptor.getUuid()));

                            descriptor.setValue(value);

                            GATT.writeDescriptor(descriptor);
                            try {
                                CYCLIC_BARRIER.await(1000, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException | BrokenBarrierException e) {
                                if (DESCRIPTOR_WRITE_CALLBACK != null) {
                                    DESCRIPTOR_WRITE_CALLBACK.onInterrupted();
                                }
                            } catch (TimeoutException e) {
                                if (DESCRIPTOR_WRITE_CALLBACK != null) {
                                    DESCRIPTOR_WRITE_CALLBACK.onTimeout();
                                }
                            }
                        }
                    }
                    break;
                case DISCONNECT:
                    DisconnectEvent disconnectEvent = (DisconnectEvent) event;
                    DISCONNECT_CALLBACK = disconnectEvent.getCallback();

                    if (GATT == null) {
                        DISCONNECT_CALLBACK.onNotConnected();
                    } else {
                        GATT.disconnect();
                        try {
                            CYCLIC_BARRIER.await(1000, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException | BrokenBarrierException e) {
                            if (DISCONNECT_CALLBACK != null) {
                                DISCONNECT_CALLBACK.onInterrupted();
                            }
                        } catch (TimeoutException e) {
                            if (DISCONNECT_CALLBACK != null) {
                                DISCONNECT_CALLBACK.onTimeout();
                            }
                        }
                    }
                    break;
                case DISCOVER_SERVICES:
                    DiscoverServicesEvent discoverServicesEvent = (DiscoverServicesEvent) event;
                    DISCOVER_SERVICES_CALLBACK = discoverServicesEvent.getCallback();

                    if (GATT == null) {
                        if (DISCOVER_SERVICES_CALLBACK != null) {
                            DISCOVER_SERVICES_CALLBACK.onNotConnected();
                        }
                    } else {
                        GATT.discoverServices();
                        try {
                            CYCLIC_BARRIER.await(1000, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException | BrokenBarrierException e) {
                            if (DISCOVER_SERVICES_CALLBACK != null) {
                                DISCOVER_SERVICES_CALLBACK.onInterrupted();
                            }
                        } catch (TimeoutException e) {
                            if (DISCOVER_SERVICES_CALLBACK != null) {
                                DISCOVER_SERVICES_CALLBACK.onTimeout();
                            }
                        }
                    }
                    break;
                case NOTIFY:

                    break;
                case REQUEST_MTU:
                    RequestMtuEvent requestMtuEvent = (RequestMtuEvent) event;
                    REQUEST_MTU_CALLBACK = requestMtuEvent.getCallback();

                    if (GATT == null) {
                        if (REQUEST_MTU_CALLBACK != null) {
                            REQUEST_MTU_CALLBACK.onNotConnected();
                        }
                    } else {
                        GATT.requestMtu(requestMtuEvent.getMtu());
                        try {
                            CYCLIC_BARRIER.await(1000, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException | BrokenBarrierException e) {
                            if (REQUEST_MTU_CALLBACK != null) {
                                REQUEST_MTU_CALLBACK.onInterrupted();
                            }
                        } catch (TimeoutException e) {
                            if (REQUEST_MTU_CALLBACK != null) {
                                REQUEST_MTU_CALLBACK.onTimeout();
                            }
                        }
                    }
                    break;
            }
        });
    }

    // Events

    public static void connect(Context context, String address, ConnectCallback callback) {
        queueEvent(new ConnectEvent(context, address, callback));
    }

    public static void disconnect(DisconnectCallback callback) {
        queueEvent(new DisconnectEvent(callback));
    }

    public static void discoverServices(DiscoverServicesCallback callback) {
        queueEvent(new DiscoverServicesEvent(callback));
    }

    public static void characteristicRead(UUID serviceUUID, UUID characteristicUUID, CharacteristicReadCallback callback) {
        queueEvent(new CharacteristicReadEvent(serviceUUID, characteristicUUID, callback));
    }

    public static void readDescriptor(UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID, DescriptorReadCallback callback) {
        queueEvent(new DescriptorReadEvent(serviceUUID, characteristicUUID, descriptorUUID, callback));
    }

    public static void requestMtu(int mtu, RequestMtuCallback callback) {
        queueEvent(new RequestMtuEvent(mtu, callback));
    }

    public static void characteristicWrite(UUID serviceUUID, UUID characteristicUUID, byte[] value, CharacteristicWriteCallback callback) {
        queueEvent(new CharacteristicWriteEvent(serviceUUID, characteristicUUID, value, callback));
    }

    public static void descriptorWrite(UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID, byte[] value, DescriptorWriteCallback callback) {
        queueEvent(new DescriptorWriteEvent(serviceUUID, characteristicUUID, descriptorUUID, value, callback));
    }

    // Device

    public static int getBondState(String address) {
        return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address).getBondState();
    }

    // Helpers

    public static void notify(NotifyCallback callback) {
        NOTIFY_CALLBACK = callback;
        BluetoothLog.d("Notify callback set to " + callback.toString());
    }

    public static boolean enableNotify(UUID serviceUUID, UUID characteristicUUID) {
        if (GATT == null) {
            return false;
        } else {
            BluetoothGattCharacteristic characteristic = getCharacteristic(GATT, serviceUUID, characteristicUUID);
            if (characteristic == null) {
                return false;
            } else {
                BluetoothLog.d("enableNotify: " + characteristic.getUuid());
                return GATT.setCharacteristicNotification(characteristic, true);
            }
        }
    }

    public static boolean setCharacteristicWriteType(UUID serviceUUID, UUID characteristicUUID, int writeType) {
        if (GATT == null) {
            return false;
        } else {
            BluetoothGattCharacteristic characteristic = getCharacteristic(GATT, serviceUUID, characteristicUUID);
            if (characteristic == null) {
                return false;
            } else {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                return true;
            }
        }
    }
}
