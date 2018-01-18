package co.blustor.identity.vault

import android.content.Context
import android.preference.PreferenceManager
import java.util.*

class Vault {

    var password = ""
    var root: VaultGroup? = null

    val isUnlocked: Boolean
        get() = root != null

    fun close() {
        root = null
    }

    fun create() {
        root = VaultGroup(null, UUID.randomUUID(), "GateKeeper")
    }

    fun getGroupByUUID(uuid: UUID): VaultGroup? {
        val root = root

        if (root != null) {
            return VaultGroup.traverser.depthFirstPreOrder(root).firstOrNull {
                it?.uuid == uuid
            }
        }

        return null
    }

    fun getEntryByUUID(uuid: UUID): VaultEntry? {
        val root = root

        if (root != null) {
            val groups = VaultGroup.traverser.depthFirstPreOrder(root).toList()

            groups.mapNotNull { it.getEntry(uuid) }.first { return it }
        }

        return null
    }

    fun findEntriesByTitle(query: String): List<VaultEntry> {
        if (query.isEmpty()) {
            return emptyList()
        }

        val root = root

        return if (root != null) {
            val loweredQuery = query.toLowerCase(Locale.getDefault())

            val vaultGroups = VaultGroup.traverser.depthFirstPreOrder(root).toList()

            vaultGroups.flatMap { it.entries }.filter {
                it.title.toLowerCase(Locale.getDefault()).contains(loweredQuery)
            }
        } else {
            ArrayList()
        }
    }

    companion object {
        const val dbPath = "/passwordvault/db.kdbx"

        val instance: Vault by lazy {
            Vault()
        }

        fun getCardAddress(context: Context): String? {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString("cardAddress", null)
        }

        fun setCardAddress(context: Context, address: String) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPreferences.edit()
            editor.putString("cardAddress", address)
            editor.apply()
        }
    }
}
