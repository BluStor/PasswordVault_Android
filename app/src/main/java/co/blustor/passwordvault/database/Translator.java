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
            entries.add(new VaultEntry(entry.getUuid(), entry.getTitle(), entry.getUsername(), entry.getPassword()));
        }
        return entries;
    }

    public static VaultGroup importKeePass(Group keePassGroup, VaultGroup rootGroup) {
        List<Group> keePassGroups = keePassGroup.getGroups();
        for (Group group : keePassGroups) {
            VaultGroup newGroup = new VaultGroup(keePassGroup.getUuid(), group.getUuid(), group.getName());
            for (Entry entry : group.getEntries()) {
                newGroup.add(new VaultEntry(
                        entry.getUuid(),
                        entry.getTitle(),
                        entry.getUsername(),
                        entry.getPassword()
                ));
            }

            rootGroup.add(newGroup);
            importKeePass(group, newGroup);
        }

        return rootGroup;
    }

    public static Group exportKeePass(VaultGroup group) {
        GroupBuilder groupBuilder = new GroupBuilder(group.getName());
        groupBuilder.addEntries(exportKeePassEntries(group));

        for (VaultGroup vaultGroup : group.getGroups()) {
            groupBuilder.addGroup(exportKeePass(vaultGroup));
            groupBuilder.addEntries(exportKeePassEntries(vaultGroup));
        }

        return groupBuilder.build();
    }
}
