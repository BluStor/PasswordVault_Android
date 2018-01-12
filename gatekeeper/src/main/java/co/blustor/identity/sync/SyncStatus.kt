package co.blustor.identity.sync

enum class SyncStatus {
    SYNCED,
    CONNECTING,
    DECRYPTING,
    ENCRYPTING,
    TRANSFERRING,
    FAILED
}
