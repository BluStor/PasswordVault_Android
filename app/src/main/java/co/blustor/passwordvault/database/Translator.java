package co.blustor.passwordvault.database;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.EntryBuilder;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.GroupBuilder;

public class Translator {
    private static final String TAG = "Translator";

    private static List<Entry> exportKeePassEntries(VaultGroup group) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (VaultEntry vaultEntry : group.getEntries()) {
            Entry entry = new EntryBuilder(vaultEntry.getTitle())
                    .uuid(vaultEntry.getUUID())
                    .username(vaultEntry.getUsername())
                    .password(vaultEntry.getPassword())
                    .url(vaultEntry.getUrl())
                    .notes(vaultEntry.getNotes())
                    .iconId(vaultEntry.getIconId())
                    .build();
            entries.add(entry);
        }

        return entries;
    }

    private static List<VaultEntry> importEntries(Group group) {
        ArrayList<VaultEntry> entries = new ArrayList<>();
        for (Entry entry : group.getEntries()) {
            VaultEntry vaultEntry = new VaultEntry(
                    group.getUuid(),
                    entry.getUuid(),
                    entry.getTitle(),
                    entry.getUsername(),
                    entry.getPassword()
            );
            vaultEntry.setUrl(entry.getUrl());
            vaultEntry.setNotes(entry.getNotes());
            vaultEntry.setIconId(entry.getIconId());
            entries.add(vaultEntry);
        }
        return entries;
    }

    private static VaultGroup importGroup(UUID parentUUID, Group keePassGroup) {
        VaultGroup group = new VaultGroup(parentUUID, keePassGroup.getUuid(), keePassGroup.getName());
        group.setIconId(keePassGroup.getIconId());

        group.addEntries(importEntries(keePassGroup));

        for (Group g : keePassGroup.getGroups()) {
            group.add(importGroup(keePassGroup.getUuid(), g));
        }

        return group;
    }

    public static VaultGroup importKeePass(Group keePassGroup) {
        VaultGroup rootGroup = new VaultGroup(null, keePassGroup.getUuid(), keePassGroup.getName());
        rootGroup.addEntries(importEntries(keePassGroup));
        rootGroup.setIconId(keePassGroup.getIconId());

        for (Group group : keePassGroup.getGroups()) {
            rootGroup.add(importGroup(keePassGroup.getUuid(), group));
        }

        return rootGroup;
    }

    public static Group exportKeePass(VaultGroup group) {
        GroupBuilder groupBuilder = new GroupBuilder(group.getName());
        groupBuilder.iconId(group.getIconId());
        groupBuilder.addEntries(exportKeePassEntries(group));

        for (VaultGroup vaultGroup : group.getGroups()) {
            groupBuilder.addGroup(exportKeePass(vaultGroup));
        }

        return groupBuilder.build();
    }
}
