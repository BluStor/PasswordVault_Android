package co.blustor.pwv.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import co.blustor.pwv.constants.Intents;
import co.blustor.pwv.database.Vault;

@SuppressLint("Registered")
public class LockingActivity extends AppCompatActivity {
    private final BroadcastReceiver lockBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intents.LOCK_DATABASE)) {
                finish();
            }
        }
    };

    @Override
    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intents.LOCK_DATABASE);
        registerReceiver(lockBroadcastReceiver, intentFilter);

        Vault vault = Vault.getInstance();
        if (!vault.isUnlocked()) {
            finish();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(lockBroadcastReceiver);
        super.onPause();
    }
}
