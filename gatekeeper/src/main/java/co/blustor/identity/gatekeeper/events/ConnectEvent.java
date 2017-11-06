package co.blustor.identity.gatekeeper.events;

import android.content.Context;
import android.support.annotation.NonNull;

import co.blustor.identity.gatekeeper.callbacks.ConnectCallback;

import static co.blustor.identity.gatekeeper.BluetoothConstants.EventName;

public class ConnectEvent implements Event {
    private Context mContext;
    private String mAddress;
    private ConnectCallback mCallback;

    public ConnectEvent(Context context, String address, ConnectCallback callback) {
        mContext = context;
        mAddress = address;
        mCallback = callback;
    }

    @NonNull
    @Override
    public EventName getName() {
        return EventName.CONNECT;
    }

    public String getAddress() {
        return mAddress;
    }

    public Context getContext() {
        return mContext;
    }

    public ConnectCallback getCallback() {
        return mCallback;
    }
}
