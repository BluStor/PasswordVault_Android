package co.blustor.passwordvault.database;

import java.util.List;

import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.Group;

public class Importer {
    private static final String TAG = "Importer";

    public static VaultGroup addKeePass(Group keePassGroup, VaultGroup rootGroup) {
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
            addKeePass(group, newGroup);
        }

        return rootGroup;
    }
}
