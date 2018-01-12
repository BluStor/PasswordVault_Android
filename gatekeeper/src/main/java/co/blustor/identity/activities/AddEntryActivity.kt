package co.blustor.identity.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import co.blustor.identity.R
import co.blustor.identity.sync.SyncManager
import co.blustor.identity.utils.MyApplication
import co.blustor.identity.vault.Vault
import co.blustor.identity.vault.VaultEntry
import co.blustor.identity.vault.VaultGroup
import kotlinx.android.synthetic.main.inc_entry.*
import java.util.*

class AddEntryActivity : LockingActivity() {

    private var group: VaultGroup? = null
    private var iconId = 0
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }

        override fun afterTextChanged(editable: Editable) {
            validate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addentry)

        // Views

        title = "Add entry"

        editTextTitle.addTextChangedListener(textWatcher)

        imageViewIcon.setOnClickListener { v ->
            val iconPickerActivity = Intent(v.context, IconPickerActivity::class.java)
            startActivityForResult(iconPickerActivity, requestIcon)
        }

        buttonGenerate.setOnClickListener { v ->
            val passwordGeneratorActivity = Intent(v.context, PasswordGeneratorActivity::class.java)
            startActivityForResult(passwordGeneratorActivity, requestPassword)
        }

        // Load

        val uuid = intent.getSerializableExtra("uuid") as UUID

        group = Vault.instance.getGroupByUUID(uuid)
        if (group != null) {
            load()
        } else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_addentry, menu)
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
        AlertDialog.Builder(this)
                .setMessage("Close without saving?")
                .setPositiveButton("Close") { _, _ -> finish() }
                .setNegativeButton("Cancel", null)
                .show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == requestIcon) {
            if (resultCode == RESULT_OK) {
                iconId = data.getIntExtra("icon", 0)
                imageViewIcon.setImageResource(MyApplication.icons.get(iconId))
            }
        } else if (requestCode == requestPassword) {
            if (resultCode == RESULT_OK) {
                editTextPassword.setText(data.getStringExtra("password"))
            }
        }
    }

    private fun load() {
        imageViewIcon.setImageResource(MyApplication.icons.get(0))
    }

    private fun save() {
        if (validate()) {
            val uuid = group?.uuid
            if (uuid != null) {
                val title = editTextTitle.text.toString()
                val username = editTextUsername.text.toString()
                val password = editTextPassword.text.toString()
                val url = editTextUrl.text.toString()

                val entry = VaultEntry(uuid, UUID.randomUUID(), title, username, password)
                entry.url = url
                entry.iconId = iconId
                group?.add(entry)

                SyncManager.setRoot(this, Vault.instance.password)
                finish()
            }
        }
    }

    private fun validate(): Boolean {
        val title = editTextTitle.text.toString()

        val hasTitle = title.isNotEmpty()
        if (hasTitle) {
            textInputLayoutTitle.error = null
        } else {
            textInputLayoutTitle.error = getString(R.string.error_title_is_required)
        }

        return hasTitle
    }

    companion object {
        private val requestIcon = 0
        private val requestPassword = 1
    }
}
