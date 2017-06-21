package co.blustor.pwv.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import co.blustor.pwv.constants.Intents;
import co.blustor.pwv.database.Vault;
import co.blustor.pwv.services.NotificationService;

public class AppBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intents.LOCK_DATABASE)) {
                Vault vault = Vault.getInstance();
                vault.lock();

                Intent ongoingNotificationService = new Intent(context, NotificationService.class);
                context.stopService(ongoingNotificationService);
            }
        }
    }
}
