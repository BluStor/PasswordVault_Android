package co.blustor.identity.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import co.blustor.identity.R
import co.blustor.identity.constants.Icons
import co.blustor.identity.sync.SyncManager
import co.blustor.identity.utils.MyApplication
import co.blustor.identity.vault.Vault
import co.blustor.identity.vault.VaultGroup
import kotlinx.android.synthetic.main.inc_group.*
import java.util.*

class AddGroupActivity : LockingActivity() {

    private var group: VaultGroup? = null
    private var iconId = Icons.default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addgroup)

        title = "Add group"

        // Views

        imageViewIcon.setOnClickListener { v ->
            val iconPickerActivity = Intent(v.context, IconPickerActivity::class.java)
            startActivityForResult(iconPickerActivity, requestIcon)
        }

        // Load

        val uuid = intent.getSerializableExtra("uuid") as UUID

        group = Vault.instance.getGroupByUUID(uuid)

        if (group == null) {
            finish()
        } else {
            load()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_addgroup, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_save) {
            save()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).setMessage("Close without saving?")
            .setPositiveButton("Close") { _, _ -> finish() }.setNegativeButton("Cancel", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == requestIcon) {
            if (resultCode == RESULT_OK) {
                iconId = data.getIntExtra("icon", 49)
                imageViewIcon.setImageResource(MyApplication.icons.get(iconId))
            }
        }
    }

    private fun load() {
        imageViewIcon.setImageResource(MyApplication.icons.get(49))
    }

    private fun save() {
        if (validate()) {
            val group = VaultGroup(
                group?.uuid, UUID.randomUUID(), editTextName.text.toString()
            )
            group.iconId = iconId

            this.group?.add(group)

            Log.d(tag, "After add, group's icon is " + this.group?.iconId)

            SyncManager.setRoot(this, Vault.instance.password)
            finish()
        }
    }

    private fun validate(): Boolean {
        val name = editTextName.text.toString()

        val hasName = name.isNotEmpty()

        if (hasName) {
            textInputLayoutName.error = null
        } else {
            textInputLayoutName.error = getString(R.string.error_name_is_required)
        }

        return hasName
    }

    companion object {
        private const val requestIcon = 0
        private const val tag = "AddGroupActivity"
    }
}
