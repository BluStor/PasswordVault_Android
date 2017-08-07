package co.blustor.pwv.database;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.EntryBuilder;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.GroupBuilder;

public class Translator {

    @NonNull
    private static List<Entry> exportKeePassEntries(@NonNull VaultGroup group) {
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

    @NonNull
    private static List<VaultEntry> importEntries(@NonNull Group group) {
        ArrayList<VaultEntry> entries = new ArrayList<>();
        for (Entry entry : group.getEntries()) {
            VaultEntry vaultEntry = new VaultEntry(
                    group.getUuid(),
                    entry.getUuid(),
                    MoreObjects.firstNonNull(entry.getTitle(), ""),
                    MoreObjects.firstNonNull(entry.getUsername(), ""),
                    MoreObjects.firstNonNull(entry.getPassword(), "")
            );
            vaultEntry.setUrl(MoreObjects.firstNonNull(entry.getUrl(), ""));
            vaultEntry.setNotes(MoreObjects.firstNonNull(entry.getNotes(), ""));
            vaultEntry.setIconId(entry.getIconId());
            entries.add(vaultEntry);
        }

        return entries;
    }

    @NonNull
    private static VaultGroup importGroup(UUID parentUUID, @NonNull Group keePassGroup) {
        VaultGroup group = new VaultGroup(parentUUID, keePassGroup.getUuid(), keePassGroup.getName());
        group.setIconId(keePassGroup.getIconId());

        group.addEntries(importEntries(keePassGroup));

        for (Group g : keePassGroup.getGroups()) {
            group.add(importGroup(keePassGroup.getUuid(), g));
        }

        return group;
    }

    @NonNull
    public static VaultGroup importKeePass(@NonNull Group keePassGroup) {
        VaultGroup rootGroup = new VaultGroup(null, keePassGroup.getUuid(), keePassGroup.getName());
        rootGroup.addEntries(importEntries(keePassGroup));
        rootGroup.setIconId(keePassGroup.getIconId());

        for (Group group : keePassGroup.getGroups()) {
            rootGroup.add(importGroup(keePassGroup.getUuid(), group));
        }

        return rootGroup;
    }

    public static Group exportKeePass(@NonNull VaultGroup group) {
        GroupBuilder groupBuilder = new GroupBuilder(group.getName());
        groupBuilder.iconId(group.getIconId());
        groupBuilder.addEntries(exportKeePassEntries(group));

        for (VaultGroup vaultGroup : group.getGroups()) {
            groupBuilder.addGroup(exportKeePass(vaultGroup));
        }

        return groupBuilder.build();
    }
}
