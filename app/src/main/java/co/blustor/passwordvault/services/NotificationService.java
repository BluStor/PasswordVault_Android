package co.blustor.passwordvault.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.activities.CloseActivity;
import co.blustor.passwordvault.constants.Intents;
import co.blustor.passwordvault.constants.Notifications;
import co.blustor.passwordvault.database.Vault;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class NotificationService extends Service {
    private static final String TAG = "NotificationService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        Vault vault = Vault.getInstance();

        if (vault.isUnlocked()) {
            stopForeground(true);
            startForeground(Notifications.LOCK_STATUS_NOTIFICATION, getUnlockedNotification());
        } else {
            Log.d(TAG, "Stopping self.");
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private PendingIntent getSwitchToAppPendingIntent() {
        Intent intent = new Intent(this, CloseActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getLockDatabaseIntent() {
        return PendingIntent.getBroadcast(this, 0, new Intent(Intents.LOCK_DATABASE), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification getUnlockedNotification() {
        return new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setVisibility(VISIBILITY_PUBLIC)
                .setContentTitle("BluStor KeePassDatabase")
                .setContentText("Database is unlocked.")
                .setContentIntent(getSwitchToAppPendingIntent())
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(R.drawable.lock_black, "Lock database", getLockDatabaseIntent())
                .build();
    }
}
