package co.blustor.identity.gatekeeper.events;

import android.support.annotation.NonNull;

import co.blustor.identity.gatekeeper.callbacks.DisconnectCallback;

import static co.blustor.identity.gatekeeper.BluetoothConstants.EventName;

public class DisconnectEvent implements Event {

    private DisconnectCallback mCallback;

    public DisconnectEvent(DisconnectCallback callback) {
        mCallback = callback;
    }

    @NonNull
    @Override
    public EventName getName() {
        return EventName.DISCONNECT;
    }

    public DisconnectCallback getCallback() {
        return mCallback;
    }
}
