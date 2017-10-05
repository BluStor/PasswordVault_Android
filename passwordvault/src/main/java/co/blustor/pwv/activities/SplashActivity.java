package co.blustor.pwv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.jdeferred.DonePipe;
import org.jdeferred.android.AndroidDeferredManager;

import co.blustor.pwv.R;
import co.blustor.pwv.database.Vault;
import co.blustor.pwv.gatekeeper.GKBLECard;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkIfDatabaseExists();
    }

    public void checkIfDatabaseExists() {
        String macAddress = Vault.getCardMacAddress(this);
        if (macAddress != null) {
            try {
                GKBLECard card = new GKBLECard(this, macAddress);
                AndroidDeferredManager androidDeferredManager = new AndroidDeferredManager();
                androidDeferredManager.when(card.checkBluetoothState()).then((DonePipe<Void, Void, GKBLECard.CardException, Void>) result ->
                        card.connect()
                ).then((DonePipe<Void, Boolean, GKBLECard.CardException, Void>) result ->
                        card.exists(Vault.DB_PATH)
                ).then(result -> {
                    if (result) {
                        unlock();
                    } else {
                        create();
                    }
                }).always((state, resolved, rejected) ->
                        card.disconnect()
                ).fail(result ->
                        unlock()
                );
            } catch (GKBLECard.CardException e) {
                unlock();
            }
        } else {
            unlock();
        }
    }

    public void create() {
        Intent createActivity = new Intent(this, CreateActivity.class);
        startActivity(createActivity);
        finish();
    }

    public void unlock() {
        Intent unlockActivity = new Intent(this, UnlockActivity.class);
        startActivity(unlockActivity);
        finish();
    }
}
