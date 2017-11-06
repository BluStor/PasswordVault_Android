package co.blustor.identity.gatekeeper.callbacks;

public interface RequestMtuCallback {
    void onMtuChanged(int mtu, int status);

    void onNotConnected();

    void onTimeout();

    void onInterrupted();
}
