package co.blustor.identity.gatekeeper.callbacks;

public interface DisconnectCallback {
    void onDisconnected();

    void onNotConnected();

    void onTimeout();
}
