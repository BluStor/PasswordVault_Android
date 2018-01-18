package co.blustor.identity.sync

import android.content.Context
import android.util.Log
import co.blustor.identity.gatekeeper.GKCard
import co.blustor.identity.vault.Translator
import co.blustor.identity.vault.Vault
import co.blustor.identity.vault.VaultGroup
import de.slackspace.openkeepass.KeePassDatabase
import de.slackspace.openkeepass.domain.KeePassFileBuilder
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException
import org.greenrobot.eventbus.EventBus
import org.jdeferred.DonePipe
import org.jdeferred.Promise
import org.jdeferred.impl.DeferredObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object SyncManager {
    private const val tag = "SyncManager"
    private const val vaultPath = "/passwordvault/db.kdbx"
    private val eventBus = EventBus.getDefault()

    @Synchronized
    fun getRoot(context: Context, password: String): Promise<VaultGroup, Exception, Void> {
        val deferredObject = DeferredObject<VaultGroup, Exception, Void>()
        val runnable = Runnable {
            eventBus.postSticky(SyncStatus.CONNECTING)

            val address = Vault.getCardAddress(context)

            if (address != null) {
                try {
                    val card = GKCard(address)
                    card.checkBluetoothState()
                        .then(DonePipe<Void, Void, GKCard.CardException, Void> {
                            card.connect(context)
                        }).then(DonePipe<Void, ByteArray, GKCard.CardException, Void> {
                        eventBus.postSticky(SyncStatus.TRANSFERRING)
                        card.getPath(Vault.dbPath)
                    }).then({
                        Log.d(tag, "Decrypting")
                        eventBus.postSticky(SyncStatus.DECRYPTING)
                        try {
                            val byteArrayInputStream = ByteArrayInputStream(it)
                            val keePassFile = KeePassDatabase.getInstance(byteArrayInputStream)
                                .openDatabase(password)

                            val keePassRoot = keePassFile.root.groups[0]
                            val group = Translator.importKeePass(keePassRoot)

                            Vault.instance.root = group
                            Vault.instance.password = password

                            eventBus.postSticky(SyncStatus.SYNCED)
                            deferredObject.resolve(group)
                            Log.d(tag, "Database synced.")
                        } catch (e: KeePassDatabaseUnreadableException) {
                            eventBus.postSticky(SyncStatus.FAILED)
                            deferredObject.reject(SyncException(SyncError.DATABASE_UNREADABLE))
                            Log.d(tag, "Database unreadable.")
                        }
                    }).always({ _, _, _ ->
                        card.disconnect()
                    }).fail({
                        eventBus.postSticky(SyncStatus.FAILED)
                        deferredObject.reject(it)
                    })
                } catch (e: GKCard.CardException) {
                    eventBus.postSticky(SyncStatus.FAILED)
                    deferredObject.reject(e)
                }

            } else {
                eventBus.postSticky(SyncStatus.FAILED)
                deferredObject.reject(SyncException(SyncError.CARD_NOT_CHOSEN))
            }
        }
        Thread(runnable).start()

        return deferredObject.promise()
    }

    @Synchronized
    fun setRoot(context: Context, password: String): Promise<VaultGroup, Exception, Void> {
        val deferredObject = DeferredObject<VaultGroup, Exception, Void>()
        val runnable = Runnable {
            val rootGroup = Vault.instance.root

            if (rootGroup != null) {
                val group = Translator.exportKeePass(rootGroup)

                val byteArrayOutputStream = ByteArrayOutputStream()

                val keePassFile = KeePassFileBuilder("passwords").addTopGroups(group).build()
                KeePassDatabase.write(keePassFile, password, byteArrayOutputStream)

                val data = byteArrayOutputStream.toByteArray()

                val address = Vault.getCardAddress(context)

                if (address != null) {
                    try {
                        val card = GKCard(address)
                        card.checkBluetoothState()
                            .then(DonePipe<Void, Void, GKCard.CardException, Void> {
                                eventBus.postSticky(SyncStatus.CONNECTING)
                                card.connect(context)
                            }).then(DonePipe<Void, Void, GKCard.CardException, Void> {
                            eventBus.postSticky(SyncStatus.TRANSFERRING)
                            card.put(data)
                        }).then(DonePipe<Void, Void, GKCard.CardException, Void> {
                            card.checksum(data)
                        }).then(DonePipe<Void, Void, GKCard.CardException, Void> {
                            card.close(vaultPath)
                        }).then({
                            eventBus.postSticky(SyncStatus.SYNCED)
                            deferredObject.resolve(rootGroup)
                        }).always({ _, _, _ -> card.disconnect() }).fail({
                            eventBus.postSticky(SyncStatus.FAILED)
                            deferredObject.reject(it)
                        })
                    } catch (e: GKCard.CardException) {
                        eventBus.postSticky(SyncStatus.FAILED)
                        deferredObject.reject(e)
                    }

                } else {
                    eventBus.postSticky(SyncStatus.FAILED)
                    deferredObject.reject(SyncException(SyncError.CARD_NOT_CHOSEN))
                }
            } else {
                eventBus.postSticky(SyncStatus.FAILED)
                deferredObject.reject(SyncException(SyncError.VAULT_EMPTY))
            }
        }

        Thread(runnable).start()

        return deferredObject.promise()
    }

    enum class SyncError {
        CARD_NOT_CHOSEN, DATABASE_UNREADABLE, VAULT_EMPTY
    }

    class SyncException internal constructor(val error: SyncError) : Exception()
}
