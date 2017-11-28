package co.blustor.identity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.jdeferred.DonePipe;

import co.blustor.identity.R;
import co.blustor.identity.gatekeeper.GKCard;
import co.blustor.identity.vault.Vault;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkIfDatabaseExists();
    }

    private void startCreate() {
        Intent createActivity = new Intent(this, CreateActivity.class); 
        startActivity(createActivity);
        finish();
    }

    private void startUnlock() {
        Intent unlockActivity = new Intent(this, UnlockActivity.class);
        startActivity(unlockActivity);
        finish();
    }

    private void startChoose() {
        Intent chooseActivity = new Intent(this, ChooseActivity.class);
        startActivity(chooseActivity);
        finish();
    }

    private void checkIfDatabaseExists() {
        String address = Vault.getCardAddress(this);

        if (address != null) {
            try {
                GKCard card = new GKCard(address);
                card.checkBluetoothState().then((DonePipe<Void, Void, GKCard.CardException, Void>) result ->
                        card.connect(this)
                ).then((DonePipe<Void, Boolean, GKCard.CardException, Void>) result ->
                        card.exists(Vault.DB_PATH)
                ).then(result -> {
                    if (result) {
                        startUnlock();
                    } else {
                        startCreate();
                    }
                }).always((state, resolved, rejected) ->
                        card.disconnect()
                ).fail(result -> {
                    // startUnlock();
                    startChoose();
                });
            } catch (GKCard.CardException e) {
                startChoose();
            }
        } else {
            startUnlock();
        }
    }
}
