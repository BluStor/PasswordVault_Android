package co.blustor.identity.vault

import co.blustor.identity.constants.Icons
import java.util.*

class VaultEntry(val groupUUID: UUID, val uuid: UUID, title: String, username: String, password: String) {
    var title = ""
    var username = ""
    var password = ""
    var url = ""
    var notes = ""
    var iconId = Icons.entry

    init {
        this.title = title
        this.username = username
        this.password = password
    }
}
