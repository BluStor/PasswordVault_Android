package co.blustor.identity.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import co.blustor.identity.constants.Intents
import co.blustor.identity.services.NotificationService
import co.blustor.identity.vault.Vault

class AppBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null) {
            if (action == Intents.lockDatabase) {
                Vault.instance.close()

                val ongoingNotificationService = Intent(context, NotificationService::class.java)
                context.stopService(ongoingNotificationService)
            }
        }
    }
}
