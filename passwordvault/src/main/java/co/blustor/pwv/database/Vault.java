package co.blustor.pwv.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Vault {

    private static Vault instance = null;
    private String mPassword = "";
    @Nullable
    private VaultGroup mRoot = null;

    @NonNull
    public static Vault getInstance() {
        if (instance == null) {
            instance = new Vault();
        }
        return instance;
    }

    @NonNull
    public Boolean isUnlocked() {
        return mRoot != null;
    }

    public void lock() {
        mRoot = null;
    }

    public void create() {
        mRoot = new VaultGroup(null, UUID.randomUUID(), "Password Vault");
    }

    @Nullable
    public VaultGroup getGroupByUUID(final UUID uuid) {
        if (mRoot != null) {
            Optional<VaultGroup> match = VaultGroup.traverser.preOrderTraversal(mRoot).firstMatch(new Predicate<VaultGroup>() {
                @Override
                public boolean apply(@Nullable VaultGroup input) {
                    return input != null && input.getUUID().equals(uuid);
                }
            });

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

    @NonNull
    public List<VaultEntry> findEntriesByTitle(@NonNull String query, Boolean includeGroupName) {
        if (query.isEmpty()) {
            return new ArrayList<>();
        }

        if (mRoot != null) {
            String loweredQuery = query.toLowerCase();

            List<VaultEntry> results = new ArrayList<>();

            List<VaultGroup> vaultGroups = VaultGroup.traverser.preOrderTraversal(mRoot).toList();

            for (VaultGroup group : vaultGroups) {
                for (VaultEntry entry : group.getEntries()) {
                    if (includeGroupName) {
                        Boolean titleContains = entry.getTitle().toLowerCase().contains(loweredQuery);
                        Boolean nameContains = group.getName().toLowerCase().contains(loweredQuery);
                        if (titleContains || (group.getParentUUID() != null && nameContains)) {
                            results.add(entry);
                        }
                    } else {
                        if (entry.getTitle().toLowerCase().contains(loweredQuery)) {
                            results.add(entry);
                        }
                    }
                }
            }

            return results;
        } else {
            return new ArrayList<>();
        }
    }
}