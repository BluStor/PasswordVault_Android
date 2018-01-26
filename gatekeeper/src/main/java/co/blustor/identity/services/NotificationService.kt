package co.blustor.identity.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC
import co.blustor.identity.activities.CloseActivity
import co.blustor.identity.constants.Intents
import co.blustor.identity.constants.NotificationChannels
import co.blustor.identity.constants.Notifications
import co.blustor.identity.vault.Vault

class NotificationService : Service() {

    private val switchToAppPendingIntent: PendingIntent
        get() {
            val intent = Intent(this, CloseActivity::class.java)
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)

            return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

    private val lockDatabaseIntent: PendingIntent
        get() = PendingIntent.getBroadcast(
            this, 0, Intent(Intents.lockDatabase), PendingIntent.FLAG_UPDATE_CURRENT
        )

    private val unlockedNotification: Notification
        get() = NotificationCompat.Builder(this, NotificationChannels.standard).setOngoing(true).setVisibility(VISIBILITY_PUBLIC).setContentTitle("BluStor KeePassDatabase").setContentText("Database is unlocked.").setContentIntent(switchToAppPendingIntent).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).addAction(android.R.drawable.ic_lock_lock, "Lock database", lockDatabaseIntent).build()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Vault.instance.isUnlocked) {
            stopForeground(true)
            startForeground(Notifications.lockStatus, unlockedNotification)
        } else {
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
