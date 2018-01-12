package co.blustor.identity.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import co.blustor.identity.R
import co.blustor.identity.fragments.SyncDialogFragment
import com.zwsb.palmsdk.activities.AuthActivity
import com.zwsb.palmsdk.activities.AuthActivity.ON_SCAN_RESULT_ERROR
import com.zwsb.palmsdk.activities.AuthActivity.ON_SCAN_RESULT_OK
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper
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

        editTextPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }

        buttonToggle.setOnCheckedChangeListener { _, isChecked ->
            checkPermissions()

            if (isChecked) {
                Log.d(tag, "Biometic palm enabled")
            } else {
                Log.d(tag, "Disable palm")
                deletePalmCredentials()
            }
        }

        buttonFloatingAction.setOnClickListener { submit() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        Log.d(tag, "onActivityResult() called")
        Log.d(tag, "requestCode = " + requestCode)

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

    private fun deletePalmCredentials() {
        val users = SharedPreferenceHelper.getStringArray(this@UnlockActivity, SharedPreferenceHelper.USER_NAMES_KEY)
        users.remove(palmUser)
        SharedPreferenceHelper.setStringArray(this@UnlockActivity, SharedPreferenceHelper.USER_NAMES_KEY, users)
        SharedPreferenceHelper.setLeftPalmEnabled(false, palmUser)
        SharedPreferenceHelper.setRightPalmEnabled(false, palmUser)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 0)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_unlock, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
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

    private fun submit() {
        val password = editTextPassword.text.toString()

        openVault(password)
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

    companion object {
        private val tag = "UnlockActivity"
        private val palmUser = "Palm Authentication"
    }
}
