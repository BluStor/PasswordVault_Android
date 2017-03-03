package co.blustor.passwordvault.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import co.blustor.passwordvault.activities.UnlockActivity;
import co.blustor.passwordvault.constants.Intents;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.services.NotificationService;

public class AppBroadcastReceiver extends BroadcastReceiver {
    protected static final String TAG = "AppBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intents.LOCK_DATABASE)) {
                Vault vault = Vault.getInstance(context);
                vault.lock();

                Intent ongoingNotificationService = new Intent(context, NotificationService.class);
                context.stopService(ongoingNotificationService);
            }
        }
    }
}
