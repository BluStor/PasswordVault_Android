package co.blustor.identity.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import co.blustor.identity.R
import co.blustor.identity.fragments.SyncDialogFragment
import co.blustor.identity.sync.SyncManager
import co.blustor.identity.vault.Vault
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class SettingsActivity : LockingActivity(), SyncDialogFragment.SyncListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Views

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                validatePassword()
            }
        }

        editTextPassword.addTextChangedListener(textWatcher)
        editTextPasswordRepeat.addTextChangedListener(textWatcher)

        buttonChangePassword.setOnClickListener { savePassword() }
    }

    override fun syncComplete(uuid: UUID) {
        finish()
    }

    private fun savePassword() {
        if (validatePassword()) {
            val password = editTextPassword.text.toString()

            Vault.instance.password = password

            SyncManager.setRoot(this)

            finish()
        }
    }

    private fun validatePassword(): Boolean {
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
}
