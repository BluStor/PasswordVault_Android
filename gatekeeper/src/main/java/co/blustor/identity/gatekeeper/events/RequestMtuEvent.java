package co.blustor.identity.gatekeeper.events;

import android.support.annotation.NonNull;

import co.blustor.identity.gatekeeper.callbacks.RequestMtuCallback;

import static co.blustor.identity.gatekeeper.BluetoothConstants.EventName;

public class RequestMtuEvent implements Event {

    private int mMtu;
    private RequestMtuCallback mCallback;

    public RequestMtuEvent(int mtu, RequestMtuCallback callback) {
        mMtu = mtu;
        mCallback = callback;
    }

    @NonNull
    @Override
    public EventName getName() {
        return EventName.REQUEST_MTU;
    }

    public int getMtu() {
        return mMtu;
    }

    public RequestMtuCallback getCallback() {
        return mCallback;
    }
}
