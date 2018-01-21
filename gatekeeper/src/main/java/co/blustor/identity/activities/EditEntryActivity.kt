package co.blustor.identity.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import co.blustor.identity.R
import co.blustor.identity.sync.SyncManager
import co.blustor.identity.utils.MyApplication
import co.blustor.identity.vault.Vault
import co.blustor.identity.vault.VaultEntry
import kotlinx.android.synthetic.main.inc_entry.*
import java.util.*

class EditEntryActivity : LockingActivity() {

    private var entry: VaultEntry? = null
    private var iconId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editentry)

        title = "Edit entry"

        // Views

        imageViewIcon.setOnClickListener { v ->
            val iconPickerActivity = Intent(v.context, IconPickerActivity::class.java)
            startActivityForResult(iconPickerActivity, requestIcon)
        }

        buttonCopy.setOnClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.primaryClip = ClipData.newPlainText("text", editTextPassword.text)
            Toast.makeText(this@EditEntryActivity, "Password copied", Toast.LENGTH_SHORT).show()
        }

        buttonGenerate.setOnClickListener { v ->
            val passwordGeneratorActivity = Intent(v.context, PasswordGeneratorActivity::class.java)
            startActivityForResult(passwordGeneratorActivity, requestPassword)
        }

        // Load

        val uuid = intent.getSerializableExtra("uuid") as UUID

        entry = Vault.instance.getEntryByUUID(uuid)

        if (entry != null) {
            load()
        } else {
            supportFinishAfterTransition()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_editentry, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item != null) {
            val id = item.itemId

            if (id == R.id.action_save) {
                save()
            }

            super.onOptionsItemSelected(item)
        } else {
            false
        }
    }

    override fun onBackPressed() {
        if (hasBeenEdited()) {
            AlertDialog.Builder(this).setMessage("Close without saving?")
                .setPositiveButton("Close") { _, _ -> supportFinishAfterTransition() }
                .setNegativeButton("Cancel", null).show()
        } else {
            supportFinishAfterTransition()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
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
    }

    private fun load() {
        val title = entry?.title ?: ""

        editTextTitle.setText(title)
        editTextUsername.setText(entry?.username)
        editTextPassword.setText(entry?.password)
        editTextUrl.setText(entry?.url)
        editTextNotes.setText(entry?.notes)

        val iconId = entry?.iconId ?: 49

        this.iconId = iconId
        imageViewIcon.setImageResource(MyApplication.icons.get(this.iconId))

        editTextTitle.setSelection(title.length)
    }

    private fun save() {
        if (validate()) {
            if (hasBeenEdited()) {
                entry?.title = editTextTitle.text.toString()
                entry?.username = editTextUsername.text.toString()
                entry?.password = editTextPassword.text.toString()
                entry?.url = editTextUrl.text.toString()
                entry?.notes = editTextNotes.text.toString()
                entry?.iconId = iconId

                SyncManager.setRoot(this)
            }

            finish()
        }
    }

    private fun hasBeenEdited(): Boolean {
        return !(entry?.title == editTextTitle.text.toString() && entry?.username == editTextUsername.text.toString() && entry?.password == editTextPassword.text.toString() && entry?.url == editTextUrl.text.toString() && entry?.notes == editTextNotes.text.toString() && entry?.iconId == iconId)
    }

    private fun validate(): Boolean {
        val title = editTextTitle.text ?: ""

        val hasTitle = title.isNotEmpty()

        if (hasTitle) {
            textInputLayoutTitle.error = null
        } else {
            textInputLayoutTitle.error = getString(R.string.error_title_is_required)
        }

        return hasTitle
    }

    companion object {
        private const val requestIcon = 0
        private const val requestPassword = 1
    }
}
