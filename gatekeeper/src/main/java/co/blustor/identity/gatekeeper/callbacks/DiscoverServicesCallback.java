package co.blustor.identity.gatekeeper.callbacks;

public interface DiscoverServicesCallback {
    void onServicesDiscovered(int status);

    void onNotConnected();

    void onTimeout();
}
