package co.blustor.passwordvault.sync;

import android.content.Context;
import android.util.Log;

import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import co.blustor.gatekeepersdk.devices.GKBluetoothCard;
import co.blustor.gatekeepersdk.devices.GKCard;
import co.blustor.passwordvault.database.Translator;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.database.VaultGroup;
import co.blustor.passwordvault.utils.MyApplication;
import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassFileBuilder;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private static final String VAULT_PATH = "/passwordvault/db.kdbx";
    private static SyncStatus lastSyncStatus = SyncStatus.SYNCED;
    private static DeferredObject<Void, Exception, SyncStatus> syncStatus = new DeferredObject<>();

    private static void connect(Context context) throws IOException {
        GKBluetoothCard card = MyApplication.getCard(context);

        Log.d(TAG, "Connection state before: " + card.getConnectionState().name());
        if (card.getConnectionState() == GKCard.ConnectionState.DISCONNECTED) {
            card.connect();
        }
        Log.d(TAG, "Connection state after: " + card.getConnectionState().name());
    }

    public static Promise<VaultGroup, Exception, SyncStatus> getRoot(final Context context, final String password) {
        final DeferredObject<VaultGroup, Exception, SyncStatus> task = new DeferredObject<>();
        new Thread() {
            @Override
            public void run() {
                task.notify(SyncStatus.SAVING);

                GKBluetoothCard card = MyApplication.getCard(context);

                try {
                    Log.d(TAG, "Connection state before: " + card.getConnectionState().name());
                    if (card.getConnectionState() == GKCard.ConnectionState.DISCONNECTED) {
                        card.connect();
                    }
                    Log.d(TAG, "Connection state after: " + card.getConnectionState().name());

                    GKCard.Response response = card.get(VAULT_PATH);
                    int status = response.getStatus();

                    if (status == 226) {
                        File file = response.getDataFile();

                        try {
                            task.notify(SyncStatus.DECRYPTING);
                            KeePassFile keePassFile = KeePassDatabase.getInstance(file).openDatabase(password);

                            Group keePassRoot = keePassFile.getRoot().getGroups().get(0);
                            VaultGroup group = Translator.importKeePass(keePassRoot);

                            Vault vault = Vault.getInstance();
                            vault.setRoot(group);
                            vault.setPassword(password);

                            task.resolve(group);
                        } catch (KeePassDatabaseUnreadableException e) {
                            task.reject(new SyncManagerException("Database password invalid."));
                        }
                    } else if (status == 550) {
                        task.reject(new SyncManagerException("Database not found on card."));
                    } else {
                        task.reject(new SyncManagerException("Card status: " + status));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    task.reject(new SyncManagerException("Unable to connect to card."));
                }

                try {
                    card.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return task.promise();
    }

    public static Promise<VaultGroup, Exception, SyncStatus> setRoot(final Context context, final String password) {
        final DeferredObject<VaultGroup, Exception, SyncStatus> task = new DeferredObject<>();
        new Thread() {
            @Override
            public void run() {
                task.notify(SyncStatus.ENCRYPTING);
                syncStatus.notify(SyncStatus.ENCRYPTING);
                lastSyncStatus = SyncStatus.ENCRYPTING;

                GKBluetoothCard card = MyApplication.getCard(context);

                try {
                    Vault vault = Vault.getInstance();
                    VaultGroup rootGroup = vault.getRoot();

                    Group group = Translator.exportKeePass(vault.getRoot());

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    KeePassFile keePassFile = new KeePassFileBuilder("passwords").addTopGroups(group).build();
                    KeePassDatabase.write(keePassFile, password, byteArrayOutputStream);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

                    task.notify(SyncStatus.SAVING);
                    syncStatus.notify(SyncStatus.SAVING);
                    lastSyncStatus = SyncStatus.SAVING;

                    connect(context);

                    GKCard.Response response = card.put(VAULT_PATH, byteArrayInputStream);

                    int status = response.getStatus();
                    if (status == 226) {
                        vault.setPassword(password);

                        card.finalize(VAULT_PATH);

                        task.resolve(rootGroup);
                        syncStatus.notify(SyncStatus.SYNCED);
                        lastSyncStatus = SyncStatus.SYNCED;
                    } else {
                        task.reject(new SyncManagerException("Card status: " + status));
                        syncStatus.notify(SyncStatus.FAILED);
                        lastSyncStatus = SyncStatus.FAILED;
                    }
                } catch (Vault.GroupNotFoundException e) {
                    task.reject(new SyncManagerException("Vault is empty."));
                    syncStatus.notify(SyncStatus.FAILED);
                    lastSyncStatus = SyncStatus.FAILED;
                } catch (IOException e) {
                    task.reject(new SyncManagerException("Unable to connect to card."));
                    syncStatus.notify(SyncStatus.FAILED);
                    lastSyncStatus = SyncStatus.FAILED;
                }

                try {
                    card.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return task.promise();
    }

    public static Promise<Void, Exception, SyncStatus> getWriteStatusPromise() {
        return syncStatus.promise();
    }

    public static SyncStatus getLastWriteStatus() {
        return lastSyncStatus;
    }

    public enum SyncType {
        READ, WRITE
    }

    public enum SyncStatus {
        SAVING, ENCRYPTING, DECRYPTING, FAILED, SYNCED
    }

    private static class SyncManagerException extends Exception {
        SyncManagerException(String messasge) {
            super(messasge);
        }
    }
}
