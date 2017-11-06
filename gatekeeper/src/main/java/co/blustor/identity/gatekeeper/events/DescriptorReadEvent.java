package co.blustor.identity.gatekeeper.events;

import android.support.annotation.NonNull;

import java.util.UUID;

import co.blustor.identity.gatekeeper.BluetoothConstants;
import co.blustor.identity.gatekeeper.callbacks.DescriptorReadCallback;

public class DescriptorReadEvent implements Event {

    private UUID mServiceUUID;
    private UUID mCharacteristicUUID;
    private UUID mDescriptorUUID;
    private DescriptorReadCallback mCallback;

    public DescriptorReadEvent(UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID, DescriptorReadCallback callback) {
        mServiceUUID = serviceUUID;
        mCharacteristicUUID = characteristicUUID;
        mDescriptorUUID = descriptorUUID;
        mCallback = callback;
    }

    @NonNull
    @Override
    public BluetoothConstants.EventName getName() {
        return BluetoothConstants.EventName.DESCRIPTOR_READ;
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

    public DescriptorReadCallback getCallback() {
        return mCallback;
    }
}
