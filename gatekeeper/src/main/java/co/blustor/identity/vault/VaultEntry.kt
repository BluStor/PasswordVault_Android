package co.blustor.identity.vault

import java.util.*

class VaultEntry(
    val groupUUID: UUID, val uuid: UUID, title: String, username: String, password: String
) {
    var title = ""
    var username = ""
    var password = ""
    var url = ""
    var notes = ""
    var iconId = 0

    init {
        this.title = title
        this.username = username
        this.password = password
    }
}
