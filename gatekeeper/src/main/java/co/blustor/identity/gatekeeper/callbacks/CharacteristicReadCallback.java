package co.blustor.identity.gatekeeper.callbacks;

public interface CharacteristicReadCallback {
    void onCharacteristicNotFound();

    void onCharacteristicRead(int status, byte[] value);

    void onDisconnected();

    void onTimeout();

    void onInterrupted();
}
