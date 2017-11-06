package co.blustor.identity.gatekeeper.events;

import android.support.annotation.NonNull;

import java.util.UUID;

import co.blustor.identity.gatekeeper.BluetoothConstants;
import co.blustor.identity.gatekeeper.callbacks.CharacteristicReadCallback;

public class CharacteristicReadEvent implements Event {

    private UUID mServiceUUID;
    private UUID mCharacteristicUUID;
    private CharacteristicReadCallback mCallback;

    public CharacteristicReadEvent(UUID serviceUUID, UUID characteristicUUID, CharacteristicReadCallback callback) {
        mServiceUUID = serviceUUID;
        mCharacteristicUUID = characteristicUUID;
        mCallback = callback;
    }

    @NonNull
    @Override
    public BluetoothConstants.EventName getName() {
        return BluetoothConstants.EventName.CHARACTERISTIC_READ;
    }

    public UUID getServiceUUID() {
        return mServiceUUID;
    }

    public UUID getCharacteristicUUID() {
        return mCharacteristicUUID;
    }

    public CharacteristicReadCallback getCallback() {
        return mCallback;
    }
}
