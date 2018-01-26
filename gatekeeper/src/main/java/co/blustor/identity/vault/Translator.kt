package co.blustor.identity.vault

import de.slackspace.openkeepass.domain.Entry
import de.slackspace.openkeepass.domain.EntryBuilder
import de.slackspace.openkeepass.domain.Group
import de.slackspace.openkeepass.domain.GroupBuilder
import java.util.*

object Translator {

    private fun exportKeePassEntries(group: VaultGroup): List<Entry> {
        return group.entries.map {
            EntryBuilder(it.title)
                .uuid(it.uuid)
                .username(it.username)
                .password(it.password)
                .url(it.url)
                .notes(it.notes)
                .iconId(it.iconId)
                .build()
        }
    }

    private fun importEntries(group: Group): List<VaultEntry> {
        val entries = mutableListOf<VaultEntry>()
        for (entry in group.entries) {
            val vaultEntry = VaultEntry(group.uuid, entry.uuid, entry.title, entry.username, entry.password)
            vaultEntry.url = entry.url
            vaultEntry.notes = entry.notes
            vaultEntry.iconId = entry.iconId
            entries.add(vaultEntry)
        }

        return entries
    }

    private fun importGroup(parentUUID: UUID, keePassGroup: Group): VaultGroup {
        val group = VaultGroup(parentUUID, keePassGroup.uuid, keePassGroup.name)
        group.iconId = keePassGroup.iconId

        group.addEntries(importEntries(keePassGroup))

        for (g in keePassGroup.groups) {
            group.add(importGroup(keePassGroup.uuid, g))
        }

        return group
    }

    fun importKeePass(keePassGroup: Group): VaultGroup {
        val rootGroup = VaultGroup(null, keePassGroup.uuid, keePassGroup.name)
        rootGroup.addEntries(importEntries(keePassGroup))
        rootGroup.iconId = keePassGroup.iconId

        for (group in keePassGroup.groups) {
            rootGroup.add(importGroup(keePassGroup.uuid, group))
        }

        return rootGroup
    }

    fun exportKeePass(group: VaultGroup): Group {
        val groupBuilder = GroupBuilder(group.name)
        groupBuilder.iconId(group.iconId)
        groupBuilder.addEntries(exportKeePassEntries(group))

        for (vaultGroup in group.groups) {
            groupBuilder.addGroup(exportKeePass(vaultGroup))
        }

        return groupBuilder.build()
    }
}
