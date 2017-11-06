package co.blustor.identity.gatekeeper.events;

import android.support.annotation.NonNull;

import java.util.UUID;

import co.blustor.identity.gatekeeper.callbacks.DescriptorWriteCallback;

import static co.blustor.identity.gatekeeper.BluetoothConstants.EventName;

public class DescriptorWriteEvent implements Event {

    private UUID mServiceUUID;
    private UUID mCharacteristicUUID;
    private UUID mDescriptorUUID;
    private byte[] mValue;
    private DescriptorWriteCallback mCallback;

    public DescriptorWriteEvent(UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID, byte[] value, DescriptorWriteCallback callback) {
        mServiceUUID = serviceUUID;
        mCharacteristicUUID = characteristicUUID;
        mDescriptorUUID = descriptorUUID;
        mValue = value;
        mCallback = callback;
    }

    @NonNull
    @Override
    public EventName getName() {
        return EventName.DESCRIPTOR_WRITE;
    }

    public UUID getServiceUUID() {
        return mServiceUUID;
    }

    public UUID getCharacteristicUUID() {
        return mCharacteristicUUID;
    }

    public UUID getDescriptorUUID() {
        return mDescriptorUUID;
    }

    public byte[] getValue() {
        return mValue;
    }

    public DescriptorWriteCallback getCallback() {
        return mCallback;
    }
}
