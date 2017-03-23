package co.blustor.passwordvault.database;

import java.util.ArrayList;
import java.util.List;

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
                    .build();
            entries.add(entry);
        }

        return entries;
    }

    private static List<VaultEntry> importEntries(Group group) {
        ArrayList<VaultEntry> entries = new ArrayList<>();
        for (Entry entry : group.getEntries()) {
            entries.add(new VaultEntry(
                    entry.getUuid(),
                    entry.getTitle(),
                    entry.getUsername(),
                    entry.getPassword()
            ));
        }
        return entries;
    }

    private static VaultGroup importGroup(Group keePassGroup) {
        VaultGroup group = new VaultGroup(keePassGroup.getUuid(), keePassGroup.getUuid(), keePassGroup.getName());
        group.addEntries(importEntries(keePassGroup));

        for (Group g : keePassGroup.getGroups()) {
            group.add(importGroup(g));
        }

        return group;
    }

    public static VaultGroup importKeePass(Group keePassGroup) {
        VaultGroup rootGroup = new VaultGroup(null, keePassGroup.getUuid(), keePassGroup.getName());
        rootGroup.addEntries(importEntries(keePassGroup));

        for (Group group : keePassGroup.getGroups()) {
            rootGroup.add(importGroup(group));
        }

        return rootGroup;
    }

    public static Group exportKeePass(VaultGroup group) {
        GroupBuilder groupBuilder = new GroupBuilder(group.getName());
        groupBuilder.addEntries(exportKeePassEntries(group));

        for (VaultGroup vaultGroup : group.getGroups()) {
            groupBuilder.addGroup(exportKeePass(vaultGroup));
        }

        return groupBuilder.build();
    }
}
