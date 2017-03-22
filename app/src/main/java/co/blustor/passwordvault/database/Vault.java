package co.blustor.passwordvault.database;

import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.UUID;

public class Vault {
    private static final String TAG = "Vault";

    private static Vault instance = null;
    private String mPassword = "";
    private VaultGroup mRoot = null;

    public static Vault getInstance() {
        if (instance == null) {
            instance = new Vault();
        }
        return instance;
    }

    public Boolean isUnlocked() {
        return mRoot != null;
    }

    public void lock() {
        mRoot = null;
    }

    public void create() {
        mRoot = new VaultGroup(null, UUID.randomUUID(), "Passwords");
        Log.d(TAG, "Created new vault with root group " + mRoot.getUUID());
    }

    public VaultGroup getGroupByUUID(final UUID uuid) throws GroupNotFoundException {
        Log.d(TAG, "Get group: " + uuid);
        VaultGroup root = getRoot();

        if (root.getUUID().equals(uuid)) {
            return mRoot;
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

    public VaultGroup getRoot() throws GroupNotFoundException {
        if (mRoot == null) {
            throw new GroupNotFoundException();
        } else {
            return mRoot;
        }
    }

    public void setRoot(VaultGroup group) {
        mRoot = group;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public static class GroupNotFoundException extends Exception {
    }
}