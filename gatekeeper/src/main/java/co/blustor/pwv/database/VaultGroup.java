package co.blustor.pwv.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class VaultGroup {
    static final TreeTraverser<VaultGroup> traverser = new TreeTraverser<VaultGroup>() {
        @Override
        public Iterable<VaultGroup> children(@NonNull VaultGroup root) {
            return root.getGroups();
        }
    };
    private final UUID mParentUUID;
    private final UUID mUUID;
    private final List<VaultGroup> mGroups = new ArrayList<>();
    private final List<VaultEntry> mEntries = new ArrayList<>();
    private String mName = "";
    private Integer mIconId = 49;

    public VaultGroup(@Nullable UUID parentUUID, UUID uuid, String name) {
        mParentUUID = parentUUID;
        mUUID = uuid;
        mName = name;
    }

    // Entries

    public void add(VaultEntry entry) {
        mEntries.add(entry);
    }

    public void addEntries(List<VaultEntry> entries) {
        mEntries.addAll(entries);
    }

    public List<VaultEntry> getEntries() {
        return mEntries;
    }

    @Nullable
    public VaultEntry getEntry(UUID uuid) {
        for (VaultEntry entry : mEntries) {
            if (entry.getUUID().equals(uuid)) {
                return entry;
            }
        }

        return null;
    }

    public void removeEntry(final UUID uuid) {
        Iterator<VaultEntry> i = mEntries.iterator();
        while (i.hasNext()) {
            if (i.next().getUUID().equals(uuid)) {
                i.remove();
            }
        }
    }

    // Groups

    public void add(VaultGroup group) {
        mGroups.add(group);
    }

    public List<VaultGroup> getGroups() {
        return mGroups;
    }

    public void removeGroup(UUID uuid) {
        Iterator<VaultGroup> i = mGroups.iterator();
        while (i.hasNext()) {
            if (i.next().getUUID().equals(uuid)) {
                i.remove();
            }
        }
    }

    public List<String> getPath() {
        Vault vault = Vault.getInstance();

        List<String> reversePath = new ArrayList<>();

        UUID uuid;

        VaultGroup g = this;
        while ((uuid = g != null ? g.getParentUUID() : null) != null) {
            g = vault.getGroupByUUID(uuid);
            if (g != null) {
                reversePath.add(g.getName());
            }
        }

        return Lists.reverse(reversePath);
    }

    // Properties

    public UUID getParentUUID() {
        return mParentUUID;
    }

    public UUID getUUID() {
        return mUUID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Integer getIconId() {
        return mIconId;
    }

    public void setIconId(Integer iconId) {
        mIconId = iconId;
    }
}
