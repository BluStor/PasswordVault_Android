package co.blustor.identity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import co.blustor.identity.vault.Vault;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String address = Vault.getCardAddress(this);

        if (address == null) {
            Intent chooseActivity = new Intent(this, ChooseActivity.class);
            startActivity(chooseActivity);
        } else {
            Intent splashActivity = new Intent(this, SplashActivity.class);
            startActivity(splashActivity);
        }

        finish();
    }
}
