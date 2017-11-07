package co.blustor.identity.gatekeeper.callbacks;

public interface DescriptorWriteCallback {
    void onDescriptorNotFound();

    void onDescriptorWrite(int status);

    void onNotConnected();

    void onTimeout();
}
