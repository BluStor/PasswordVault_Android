package co.blustor.passwordvault.database;

import android.content.Context;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException;

public class Vault {
    private static final String TAG = "Vault";

    private static Vault instance = null;
    private final File mFile;
    private VaultGroup mRoot = null;

    public static Vault getInstance(Context context) {
        if (instance == null) {
            instance = new Vault(context);
        }
        return instance;
    }

    private Vault(Context context) {
        File path = context.getFilesDir();
        mFile = new File(path, "passwords.kdbx");

        if (!mFile.exists()) {
            try {
                InputStream fileInputStream = context.getAssets().open("passwords.kdbx");

                FileOutputStream fileOutputStream = new FileOutputStream(mFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = fileInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean exists() {
        return mFile.exists();
    }

    public Boolean isUnlocked() {
        return mRoot != null;
    }

    public void lock() {
        mRoot = null;
    }

    public void create() {
        mRoot = new VaultGroup(null, UUID.randomUUID(), "Passwords");
    }

    public void unlock(String password) throws NotFoundException, PasswordInvalidException {
        try {
            FileInputStream fileInputStream = new FileInputStream(mFile);
            KeePassFile database = KeePassDatabase.getInstance(fileInputStream).openDatabase(password);

            Group keePassRoot = database.getRoot().getGroups().get(0);
            mRoot = Importer.addKeePass(keePassRoot, new VaultGroup(null, keePassRoot.getUuid(), keePassRoot.getName()));
        } catch (FileNotFoundException e) {
            throw new NotFoundException();
        } catch (KeePassDatabaseUnreadableException e) {
            throw new PasswordInvalidException();
        }
    }

    public void save(String password) throws NotFoundException {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(mFile);
        } catch (FileNotFoundException e) {
            throw new NotFoundException();
        }
    }

    public VaultGroup getRoot() throws GroupNotFoundException {
        if (mRoot == null) {
            throw new GroupNotFoundException();
        } else {
            return mRoot;
        }
    }

    public VaultGroup getGroupByUUID(final UUID uuid) throws GroupNotFoundException {
        VaultGroup root = getRoot();

        if (root.getUUID().equals(uuid)) {
            return mRoot;
        } else {
            Log.d("Vault", "Root UUID is " + root.getUUID() + root.getUUID());
        }

        Optional<VaultGroup> match = VaultGroup.traverser.preOrderTraversal(root).firstMatch(new Predicate<VaultGroup>() {
            @Override
            public boolean apply(VaultGroup input) {
                return input.getUUID().equals(uuid);
            }
        });

        if (match.isPresent()) {
            return match.get();
        } else {
            throw new GroupNotFoundException();
        }
    }

    public static class NotFoundException extends Exception {}
    public static class PasswordInvalidException extends Exception {}
    public static class GroupNotFoundException extends Exception {}
}