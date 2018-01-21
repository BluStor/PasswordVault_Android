package co.blustor.identity.vault

import co.blustor.identity.constants.Icons
import com.google.common.graph.Traverser
import java.util.*

data class VaultGroup(val parentUUID: UUID?, val uuid: UUID, var name: String) {

    private val mGroups = ArrayList<VaultGroup>()
    private val mEntries = ArrayList<VaultEntry>()

    var iconId = Icons.default

    val entries: List<VaultEntry>
        get() = mEntries

    val groups: List<VaultGroup>
        get() = mGroups

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
        mEntries.add(entry)
    }

    fun addEntries(entries: List<VaultEntry>) {
        mEntries.addAll(entries)
    }

    fun getEntry(uuid: UUID): VaultEntry? {
        return mEntries.firstOrNull { it.uuid == uuid }
    }

    fun removeEntry(uuid: UUID) {
        val i = mEntries.iterator()
        while (i.hasNext()) {
            if (i.next().uuid == uuid) {
                i.remove()
            }
        }
    }

    // Groups

    fun add(group: VaultGroup) {
        mGroups.add(group)
    }

    fun removeGroup(uuid: UUID) {
        val i = mGroups.iterator()
        while (i.hasNext()) {
            if (i.next().uuid == uuid) {
                i.remove()
            }
        }
    }

    companion object {
        val traverser: Traverser<VaultGroup> = Traverser.forTree({
            it.groups
        })
    }
}
