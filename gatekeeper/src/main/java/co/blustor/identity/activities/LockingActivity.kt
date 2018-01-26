package co.blustor.identity.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity

import co.blustor.identity.constants.Intents
import co.blustor.identity.vault.Vault

@SuppressLint("Registered")
open class LockingActivity : AppCompatActivity() {

    private val lockBroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null) {
                if (action == Intents.lockDatabase) {
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intents.lockDatabase)
        registerReceiver(lockBroadcastReceiver, intentFilter)

        if (!Vault.instance.isUnlocked) {
            finish()
        }

        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(lockBroadcastReceiver)
        super.onPause()
    }
}
