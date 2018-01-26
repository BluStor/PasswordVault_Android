package co.blustor.identity.vault

import co.blustor.identity.constants.Icons
import com.google.common.graph.Traverser
import java.util.*

data class VaultGroup(val parentUUID: UUID?, val uuid: UUID, var name: String) {

    val groups = mutableListOf<VaultGroup>()
    val entries = mutableListOf<VaultEntry>()

    var iconId = Icons.folder

    val path: List<String>
        get() {
            val path = mutableListOf<String>()

            var g = this
            while (true) {
                val parentUUID = g.parentUUID
                if (parentUUID == null) {
                    break
                } else {
                    val group = Vault.instance.getGroupByUUID(parentUUID)
                    if (group != null) {
                        g = group
                        path.add(0, g.name)
                    } else {
                        break
                    }
                }
            }

            return path
        }

    // Entries

    fun add(entry: VaultEntry) {
        entries.add(entry)
    }

    fun addEntries(entries: List<VaultEntry>) {
        this.entries.addAll(entries)
    }

    fun getEntry(uuid: UUID): VaultEntry? {
        return entries.firstOrNull { it.uuid == uuid }
    }

    fun removeEntry(uuid: UUID) {
        val i = entries.iterator()
        while (i.hasNext()) {
            if (i.next().uuid == uuid) {
                i.remove()
            }
        }
    }

    // Groups

    fun add(group: VaultGroup) {
        groups.add(group)
    }

    fun removeGroup(uuid: UUID) {
        groups.removeAll {
            it.uuid == uuid
        }
    }

    companion object {
        val traverser: Traverser<VaultGroup> = Traverser.forTree({
            it.groups
        })
    }
}
