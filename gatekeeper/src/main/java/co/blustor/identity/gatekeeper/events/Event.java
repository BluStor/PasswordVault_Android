package co.blustor.identity.gatekeeper.events;

import android.support.annotation.NonNull;

import static co.blustor.identity.gatekeeper.BluetoothConstants.EventName;

public interface Event {
    @NonNull
    EventName getName();
}
