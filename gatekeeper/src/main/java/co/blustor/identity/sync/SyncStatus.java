package co.blustor.identity.sync;

public enum SyncStatus {
    SYNCED,
    CONNECTING,
    DECRYPTING,
    ENCRYPTING,
    TRANSFERRING,
    FAILED
}
