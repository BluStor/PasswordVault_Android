package co.blustor.identity.gatekeeper.callbacks;

public interface CharacteristicWriteCallback {
    void onCharacteristicNotFound();

    void onCharacteristicWrite(int status);

    void onNotConnected();

    void onTimeout();

    void onInterrupted();
}
