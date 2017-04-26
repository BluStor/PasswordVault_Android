package co.blustor.passwordvault.database;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Vault {

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
        mRoot = new VaultGroup(null, UUID.randomUUID(), "Password Vault");
    }

    public VaultGroup getGroupByUUID(final UUID uuid) {
        Optional<VaultGroup> match = VaultGroup.traverser.preOrderTraversal(mRoot).firstMatch(new Predicate<VaultGroup>() {
            @Override
            public boolean apply(VaultGroup input) {
                return input.getUUID().equals(uuid);
            }
        });

        if (match.isPresent()) {
            return match.get();
        } else {
            return null;
        }
    }

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

    public List<VaultGroup> findGroupsByName(final String query) {
        if (query.isEmpty()) {
            return new ArrayList<>();
        }

        final String loweredQuery = query.toLowerCase();

        FluentIterable<VaultGroup> vaultGroups = VaultGroup.traverser.preOrderTraversal(mRoot).filter(new Predicate<VaultGroup>() {
            @Override
            public boolean apply(VaultGroup input) {
                return input.getParentUUID() != null && input.getName().toLowerCase().contains(loweredQuery);
            }
        });

        return vaultGroups.toList();
    }

    public List<VaultEntry> findEntriesByTitle(String query, Boolean includeGroupName) {
        if (query.isEmpty()) {
            return new ArrayList<>();
        }

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
    }
}