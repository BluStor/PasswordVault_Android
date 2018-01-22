package co.blustor.identity.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
                submit()
                true
            } else {
                false
            }
        }

        buttonFloatingAction.setOnClickListener { submit() }
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
        private const val tag = "UnlockActivity"
    }
}
