package co.blustor.identity.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import co.blustor.identity.R
import co.blustor.identity.fragments.SyncDialogFragment
import co.blustor.identity.vault.Vault
import kotlinx.android.synthetic.main.activity_create.*
import java.util.*

class CreateActivity : AppCompatActivity(), SyncDialogFragment.SyncListener {

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
        setContentView(R.layout.activity_create)

        title = "New database"

        // Views

        editTextPassword.addTextChangedListener(textWatcher)
        editTextPasswordRepeat.addTextChangedListener(textWatcher)

        buttonFloatingAction.setOnClickListener { create() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.action_choose -> {
                val chooseActivity = Intent(this, ChooseActivity::class.java)
                startActivity(chooseActivity)
                finish()
            }
            R.id.action_about -> {
                val aboutActivity = Intent(this, AboutActivity::class.java)
                startActivity(aboutActivity)
            }
            R.id.action_existing -> {
                val unlockActivity = Intent(applicationContext, UnlockActivity::class.java)
                startActivity(unlockActivity)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun syncComplete(uuid: UUID) {
        Log.i(tag, "syncComplete: " + uuid.toString())

        val unlockActivity = Intent(this, UnlockActivity::class.java)
        startActivity(unlockActivity)

        val groupActivity = Intent(this, GroupActivity::class.java)
        groupActivity.putExtra("uuid", uuid)
        startActivity(groupActivity)

        finish()
    }

    private fun create() {
        if (validate()) {
            AlertDialog.Builder(this@CreateActivity)
                    .setTitle("Replace database?")
                    .setMessage("This will replace the password database on your card, potentially destroying data.  Are you sure?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        dialog.cancel()

                        Vault.instance.create()

                        val syncDialogFragment = SyncDialogFragment()

                        val args = Bundle()
                        args.putSerializable("type", "write")
                        args.putSerializable("password", editTextPassword.text.toString())

                        syncDialogFragment.arguments = args
                        syncDialogFragment.setSyncListener(this)
                        syncDialogFragment.show(fragmentManager, "dialog")
                    }
                    .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
                    .show()
        }
    }

    private fun validate(): Boolean {
        val password = editTextPassword.text.toString()
        val passwordRepeat = editTextPasswordRepeat.text.toString()

        val hasPassword = password.isNotEmpty()
        val passwordsMatch = password == passwordRepeat

        if (hasPassword) {
            textInputLayoutPassword.error = null
        } else {
            textInputLayoutPassword.error = getString(R.string.error_password_is_required)
        }

        if (passwordsMatch) {
            textInputLayoutPasswordRepeat.error = null
        } else {
            textInputLayoutPasswordRepeat.error = getString(R.string.error_passwords_do_not_match)
        }

        return hasPassword && passwordsMatch
    }

    companion object {
        private val tag = "CreateActivity"
    }
}
