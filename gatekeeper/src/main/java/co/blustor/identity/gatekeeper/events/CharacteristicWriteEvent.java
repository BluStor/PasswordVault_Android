package co.blustor.identity.gatekeeper.events;

import android.support.annotation.NonNull;

import java.util.UUID;

import co.blustor.identity.gatekeeper.callbacks.CharacteristicWriteCallback;

import static co.blustor.identity.gatekeeper.BluetoothConstants.EventName;

public class CharacteristicWriteEvent implements Event {

    private final UUID mServiceUUID;
    private final UUID mCharacteristicUUID;
    private final byte[] mValue;
    private final CharacteristicWriteCallback mCallback;

    public CharacteristicWriteEvent(UUID serviceUUID, UUID characteristicUUID, byte[] value, CharacteristicWriteCallback callback) {
        mServiceUUID = serviceUUID;
        mCharacteristicUUID = characteristicUUID;
        mValue = value;
        mCallback = callback;
    }

    @NonNull
    @Override
    public EventName getName() {
        return EventName.CHARACTERISTIC_WRITE;
    }

    public UUID getServiceUUID() {
        return mServiceUUID;
    }

    public UUID getCharacteristicUUID() {
        return mCharacteristicUUID;
    }

    public byte[] getValue() {
        return mValue;
    }

    public CharacteristicWriteCallback getCallback() {
        return mCallback;
    }
}
