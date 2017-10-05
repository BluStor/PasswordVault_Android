package co.blustor.pwv.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Vault {
    public static final String DB_PATH = "/passwordvault/db.kdbx";
    private static Vault instance = null;

    private String mPassword = "";
    @Nullable
    private VaultGroup mRoot = null;

    public static Vault getInstance() {
        if (instance == null) {
            instance = new Vault();
        }
        return instance;
    }

    @Nullable
    public static String getCardMacAddress(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString("cardMacAddress", null);
    }

    public static void setCardMacAddress(Context context, String cardMacAddress) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cardMacAddress", cardMacAddress);
        editor.apply();
    }

    public Boolean isUnlocked() {
        return mRoot != null;
    }

    public void close() {
        mRoot = null;
    }

    public void create() {
        mRoot = new VaultGroup(null, UUID.randomUUID(), "Password Vault");
    }

    @Nullable
    public VaultGroup getGroupByUUID(final UUID uuid) {
        if (mRoot != null) {
            Optional<VaultGroup> match = VaultGroup.traverser.preOrderTraversal(mRoot).firstMatch(input -> input != null && input.getUUID().equals(uuid));

            if (match.isPresent()) {
                return match.get();
            } else {
                return null;
            }
        }

        return null;
    }

    @Nullable
    public VaultEntry getEntryByUUID(final UUID uuid) {
        VaultGroup root = getRoot();

        if (root != null) {
            List<VaultGroup> groups = VaultGroup.traverser.preOrderTraversal(root).toList();
            for (VaultGroup group : groups) {
                VaultEntry entry = group.getEntry(uuid);
                if (entry != null) {
                    return entry;
                }
            }
        }

        return null;
    }

    @Nullable
    public VaultGroup getRoot() {
        return mRoot;
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

    public List<VaultEntry> findEntriesByTitle(String query) {
        if (query.isEmpty()) {
            return new ArrayList<>();
        }

        if (mRoot != null) {
            String loweredQuery = query.toLowerCase();

            List<VaultEntry> results = new ArrayList<>();

            List<VaultGroup> vaultGroups = VaultGroup.traverser.preOrderTraversal(mRoot).toList();

            for (VaultGroup group : vaultGroups) {
                for (VaultEntry entry : group.getEntries()) {
                    if (entry.getTitle().toLowerCase().contains(loweredQuery)) {
                        results.add(entry);
                    }
                }
            }

            return results;
        } else {
            return new ArrayList<>();
        }
    }
}