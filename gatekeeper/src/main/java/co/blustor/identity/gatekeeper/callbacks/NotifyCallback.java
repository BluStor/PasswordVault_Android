package co.blustor.identity.gatekeeper.callbacks;

import java.util.UUID;

public interface NotifyCallback {
    void onNotify(UUID serviceUUID, UUID characteristicUUID, byte[] value);
}
