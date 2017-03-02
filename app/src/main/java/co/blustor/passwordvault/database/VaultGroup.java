package co.blustor.passwordvault.database;

import android.support.annotation.NonNull;

import com.google.common.collect.TreeTraverser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class VaultGroup {
    private static String TAG = "VaultGroup";

    private UUID mParentUUID;
    private UUID mUUID;
    private String mName;

    private List<VaultGroup> mGroups = new ArrayList<>();
    private List<VaultEntry> mEntries = new ArrayList<>();

    static TreeTraverser<VaultGroup> traverser = new TreeTraverser<VaultGroup>() {
        @Override
        public Iterable<VaultGroup> children(@NonNull VaultGroup root) {
            return root.getGroups();
        }
    };

    public VaultGroup(UUID parentUUID, UUID uuid, String name) {
        mParentUUID = parentUUID;
        mUUID = uuid;
        mName = name;
    }

    // Entries

    public void add(VaultEntry entry) {
        mEntries.add(entry);
    }

    public List<VaultEntry> getEntries() {
        return mEntries;
    }

    public VaultEntry getEntry(UUID uuid) throws EntryNotFoundException {
        for (VaultEntry entry : mEntries) {
            if (entry.getUUID().equals(uuid)) {
                return entry;
            }
        }

        throw new EntryNotFoundException();
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

    public static class EntryNotFoundException extends Exception {}
}
