package co.blustor.identity.gatekeeper.events;

import android.support.annotation.NonNull;

import co.blustor.identity.gatekeeper.callbacks.DiscoverServicesCallback;

import static co.blustor.identity.gatekeeper.BluetoothConstants.EventName;

public class DiscoverServicesEvent implements Event {

    private DiscoverServicesCallback mCallback;

    public DiscoverServicesEvent(DiscoverServicesCallback callback) {
        mCallback = callback;
    }

    @NonNull
    @Override
    public EventName getName() {
        return EventName.DISCOVER_SERVICES;
    }

    public DiscoverServicesCallback getCallback() {
        return mCallback;
    }
}
