package co.blustor.identity.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import co.blustor.identity.R
import co.blustor.identity.fragments.SyncDialogFragment
import co.blustor.identity.utils.AlertUtils
import co.blustor.identity.utils.Biometrics
import com.zwsb.palmsdk.activities.AuthActivity
import com.zwsb.palmsdk.activities.AuthActivity.ON_SCAN_RESULT_ERROR
import com.zwsb.palmsdk.activities.AuthActivity.ON_SCAN_RESULT_OK
import kotlinx.android.synthetic.main.activity_unlock.*
import java.util.*

class UnlockActivity : AppCompatActivity(), SyncDialogFragment.SyncListener {

    private val applicationTitle: String
        get() {
            val packageManager = packageManager

            val name = applicationInfo.loadLabel(packageManager).toString()
            val version = try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                "?.?"
            }

            return name + " " + version
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock)

        title = applicationTitle

        // Views

        editTextPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val password = editTextPassword.text.toString()
                openVault(password)
                true
            } else {
                false
            }
        }

        checkBoxFingerprint.setOnClickListener {
            val biometrics = Biometrics(this)

            if (checkBoxFingerprint.isChecked) {
                if (biometrics.hasPalm()) {
                    AlertUtils.showError(this, "Please disable palm before enabling fingerprint.")
                    reloadUI()
                } else {
                    val password = editTextPassword.text.toString()
                    if (password.isEmpty()) {
                        AlertUtils.showError(this, "You must enter a password.")
                        reloadUI()
                    } else {
                        biometrics.setFingerprint(password, {
                            if (it == null) {
                                Log.d(tag, "setFingerprint: success")
                                editTextPassword.text.clear()
                            } else {
                                Log.d(tag, "setFingerprint: exception")
                            }
                            reloadUI()
                        })
                    }
                }
            } else {
                biometrics.deleteFingerprint()
                reloadUI()
            }
        }

        checkBoxPalm.setOnClickListener {
            val biometrics = Biometrics(this)

            if (checkBoxPalm.isChecked) {
                if (biometrics.hasFingerprint()) {
                    AlertUtils.showError(this, "Please disable fingerprint before enabling palm.")
                    reloadUI()
                } else {
                    val password = editTextPassword.text.toString()
                    if (password.isEmpty()) {
                        AlertUtils.showError(this, "You must enter a password.")
                        reloadUI()
                    } else {
                        Biometrics(this).setPalm(password, {
                            if (it == null) {
                                Log.d(tag, "setPalm: success")
                                editTextPassword.text.clear()
                            } else {
                                Log.d(tag, "setPalm: exception")
                            }

                            reloadUI()
                        })
                    }
                }
            } else {
                Biometrics(this).deletePalm()
                reloadUI()
            }
        }

        buttonFloatingAction.setOnClickListener {
            val biometrics = Biometrics(this)
            when (biometrics.which) {
                Biometrics.AuthType.NONE -> {
                    val password = editTextPassword.text.toString()
                    openVault(password)
                }
                Biometrics.AuthType.FINGERPRINT -> {
                    biometrics.getFingerprint {
                        openVault(it)
                    }
                }
                Biometrics.AuthType.PALM -> {
                    biometrics.getPalm {
                        openVault(it)
                    }
                }
            }
        }

        // Load

        reloadUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AuthActivity.NEW_USER_ACTION) {
            when (resultCode) {
                ON_SCAN_RESULT_OK -> {

                }
                ON_SCAN_RESULT_ERROR -> {
                    Log.d(tag, "Error")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_unlock, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_about -> {
                val aboutActivity = Intent(this, AboutActivity::class.java)
                startActivity(aboutActivity)
            }
            R.id.action_choose -> {
                val chooseActivity = Intent(this, ChooseActivity::class.java)
                startActivity(chooseActivity)
            }
            R.id.action_new -> {
                val createActivity = Intent(applicationContext, CreateActivity::class.java)
                startActivity(createActivity)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun syncComplete(uuid: UUID) {
        Log.i(tag, "syncComplete: " + uuid.toString())
        val groupActivity = Intent(this@UnlockActivity, GroupActivity::class.java)
        groupActivity.putExtra("uuid", uuid)
        startActivity(groupActivity)
    }

    private fun openVault(password: String) {
        val syncDialogFragment = SyncDialogFragment()

        val args = Bundle()
        args.putSerializable("type", "read")
        args.putSerializable("password", password)

        syncDialogFragment.arguments = args
        syncDialogFragment.setSyncListener(this)
        syncDialogFragment.show(fragmentManager, "dialog")
    }

    private fun reloadUI() {
        val biometrics = Biometrics(this)
        when (biometrics.which) {
            Biometrics.AuthType.NONE -> {
                textInputLayoutPassword.visibility = View.VISIBLE
                checkBoxFingerprint.isChecked = false
                checkBoxPalm.isChecked = false
            }
            Biometrics.AuthType.FINGERPRINT -> {
                textInputLayoutPassword.visibility = View.GONE
                checkBoxFingerprint.isChecked = true
                checkBoxPalm.isChecked = false
            }
            Biometrics.AuthType.PALM -> {
                textInputLayoutPassword.visibility = View.GONE
                checkBoxFingerprint.isChecked = false
                checkBoxPalm.isChecked = true
            }
        }
    }

    companion object {
        private const val tag = "UnlockActivity"
    }
}
