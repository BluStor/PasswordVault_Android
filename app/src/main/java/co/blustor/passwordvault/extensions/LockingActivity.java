package co.blustor.passwordvault.extensions;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import co.blustor.passwordvault.constants.Intents;

public class LockingActivity extends AppCompatActivity {
    private BroadcastReceiver lockBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intents.LOCK_DATABASE)) {
                finish();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intents.LOCK_DATABASE);
        registerReceiver(lockBroadcastReceiver, intentFilter);
    }
}
