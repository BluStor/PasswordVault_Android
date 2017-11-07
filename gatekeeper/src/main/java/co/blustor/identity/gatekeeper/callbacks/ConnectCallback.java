package co.blustor.identity.gatekeeper.callbacks;

public interface ConnectCallback {
    void onConnectionStateChange(int status, int newState);

    void onBluetoothNotSupported();

    void onTimeout();
}
