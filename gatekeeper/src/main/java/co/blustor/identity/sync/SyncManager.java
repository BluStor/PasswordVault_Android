package co.blustor.identity.sync;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import org.greenrobot.eventbus.EventBus;
import org.jdeferred.DonePipe;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import co.blustor.identity.vault.Translator;
import co.blustor.identity.vault.Vault;
import co.blustor.identity.vault.VaultGroup;
import co.blustor.identity.gatekeeper.GKBLECard;
import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassFileBuilder;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private static final String VAULT_PATH = "/passwordvault/db.kdbx";
    private static final EventBus EVENT_BUS = EventBus.getDefault();

    public static synchronized Promise<VaultGroup, Exception, Void> getRoot(Context context, String password) {
        final DeferredObject<VaultGroup, Exception, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            Pair<String, String> cardAddressName = Vault.getCardAddressName(context);
            String address = cardAddressName.first;
            String name = cardAddressName.second;

            if (address != null && name != null) {
                try {
                    GKBLECard card = new GKBLECard(context, address, name);
                    card.checkBluetoothState().then((DonePipe<Void, Void, GKBLECard.CardException, Void>) result -> {
                        EVENT_BUS.postSticky(SyncStatus.CONNECTING);
                        return card.connect();
                    }).then((DonePipe<Void, byte[], GKBLECard.CardException, Void>) result -> {
                        EVENT_BUS.postSticky(SyncStatus.TRANSFERRING);
                        return card.get(Vault.DB_PATH);
                    }).then(result -> {
                        EVENT_BUS.postSticky(SyncStatus.DECRYPTING);
                        try {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(result);
                            KeePassFile keePassFile = KeePassDatabase.getInstance(byteArrayInputStream).openDatabase(password);

                            Group keePassRoot = keePassFile.getRoot().getGroups().get(0);
                            VaultGroup group = Translator.importKeePass(keePassRoot);

                            Vault vault = Vault.getInstance();
                            vault.setRoot(group);
                            vault.setPassword(password);

                            EVENT_BUS.postSticky(SyncStatus.SYNCED);
                            deferredObject.resolve(group);
                        } catch (KeePassDatabaseUnreadableException e) {
                            EVENT_BUS.postSticky(SyncStatus.FAILED);
                            deferredObject.reject(new SyncException(SyncError.DATABASE_UNREADABLE));
                        }
                    }).always((state, resolved, rejected) ->
                            card.disconnect()
                    ).fail(result -> {
                        EVENT_BUS.postSticky(SyncStatus.FAILED);
                        deferredObject.reject(result);
                    });
                } catch (GKBLECard.CardException e) {
                    EVENT_BUS.postSticky(SyncStatus.FAILED);
                    deferredObject.reject(e);
                }
            } else {
                EVENT_BUS.postSticky(SyncStatus.FAILED);
                deferredObject.reject(new SyncException(SyncError.CARD_NOT_CHOSEN));
            }
        };

        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public static synchronized Promise<VaultGroup, Exception, Void> setRoot(Context context, String password) {
        final DeferredObject<VaultGroup, Exception, Void> deferredObject = new DeferredObject<>();
        Runnable runnable = () -> {
            Vault vault = Vault.getInstance();
            VaultGroup rootGroup = vault.getRoot();

            if (rootGroup != null) {
                Group group = Translator.exportKeePass(vault.getRoot());

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                KeePassFile keePassFile = new KeePassFileBuilder("passwords").addTopGroups(group).build();
                KeePassDatabase.write(keePassFile, password, byteArrayOutputStream);

                byte[] bytes = byteArrayOutputStream.toByteArray();

                Pair<String, String> cardAddressName = Vault.getCardAddressName(context);
                String address = cardAddressName.first;
                String name = cardAddressName.second;

                if (address != null && name != null) {
                    try {
                        GKBLECard card = new GKBLECard(context, address, name);
                        card.checkBluetoothState().then((DonePipe<Void, Void, GKBLECard.CardException, Void>) result -> {
                            EVENT_BUS.postSticky(SyncStatus.CONNECTING);
                            return card.connect();
                        }).then((DonePipe<Void, Void, GKBLECard.CardException, Void>) result -> {
                            EVENT_BUS.postSticky(SyncStatus.TRANSFERRING);
                            return card.put(VAULT_PATH, bytes);
                        }).then(result -> {
                            EVENT_BUS.postSticky(SyncStatus.SYNCED);
                            deferredObject.resolve(rootGroup);
                        }).always((state, resolved, rejected) ->
                                card.disconnect()
                        ).fail(result -> {
                            EVENT_BUS.postSticky(SyncStatus.FAILED);
                            deferredObject.reject(result);
                        });
                    } catch (GKBLECard.CardException e) {
                        EVENT_BUS.postSticky(SyncStatus.FAILED);
                        deferredObject.reject(e);
                    }
                } else {
                    EVENT_BUS.postSticky(SyncStatus.FAILED);
                    deferredObject.reject(new SyncException(SyncError.CARD_NOT_CHOSEN));
                }
            } else {
                EVENT_BUS.postSticky(SyncStatus.FAILED);
                deferredObject.reject(new SyncException(SyncError.VAULT_EMPTY));
            }
        };

        new Thread(runnable).start();

        return deferredObject.promise();
    }

    public enum SyncError {
        CARD_NOT_CHOSEN,
        DATABASE_UNREADABLE,
        VAULT_EMPTY
    }

    public static class SyncException extends Exception {
        final SyncError mError;

        SyncException(SyncError error) {
            mError = error;
        }

        public SyncError getError() {
            return mError;
        }
    }
}
