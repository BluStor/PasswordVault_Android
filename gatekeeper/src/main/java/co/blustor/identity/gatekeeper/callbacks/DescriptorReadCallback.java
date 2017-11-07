package co.blustor.identity.gatekeeper.callbacks;

public interface DescriptorReadCallback {
    void onDescriptorNotFound();

    void onDescriptorRead(int status, byte[] value);

    void onNotConnected();

    void onTimeout();
}
