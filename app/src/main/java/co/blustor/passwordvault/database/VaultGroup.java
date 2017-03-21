package co.blustor.passwordvault.database;

import android.support.annotation.NonNull;

import com.google.common.collect.TreeTraverser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import co.blustor.passwordvault.comparators.VaultEntryComparator;
import co.blustor.passwordvault.comparators.VaultGroupComparator;

public class VaultGroup {
    static final TreeTraverser<VaultGroup> traverser = new TreeTraverser<VaultGroup>() {
        @Override
        public Iterable<VaultGroup> children(@NonNull VaultGroup root) {
            return root.getGroups();
        }
    };
    private static String TAG = "VaultGroup";
    private final UUID mParentUUID;
    private final UUID mUUID;
    private final List<VaultGroup> mGroups = new ArrayList<>();
    private final List<VaultEntry> mEntries = new ArrayList<>();
    private String mName;

    public VaultGroup(UUID parentUUID, UUID uuid, String name) {
        mParentUUID = parentUUID;
        mUUID = uuid;
        mName = name;
    }

    // Entries

    public void add(VaultEntry entry) {
        mEntries.add(entry);
        Collections.sort(mEntries, new VaultEntryComparator());
    }

    public void addEntries(List<VaultEntry> entries) {
        mEntries.addAll(entries);
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
        Collections.sort(mGroups, new VaultGroupComparator());
    }

    public void addGroups(List<VaultGroup> groups) {
        mGroups.addAll(groups);
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

    public static class EntryNotFoundException extends Exception {
    }
}
