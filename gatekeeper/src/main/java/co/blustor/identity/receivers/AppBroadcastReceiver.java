package co.blustor.identity.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import co.blustor.identity.constants.Intents;
import co.blustor.identity.vault.Vault;
import co.blustor.identity.services.NotificationService;

public class AppBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intents.LOCK_DATABASE)) {
                Vault vault = Vault.getInstance();
                vault.close();

                Intent ongoingNotificationService = new Intent(context, NotificationService.class);
                context.stopService(ongoingNotificationService);
            }
        }
    }
}
