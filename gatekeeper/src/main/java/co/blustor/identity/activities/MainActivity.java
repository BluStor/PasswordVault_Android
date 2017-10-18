package co.blustor.identity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import co.blustor.identity.database.Vault;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Vault.getCardMacAddress(this) == null) {
            Intent scanActivity = new Intent(this, ChooseActivity.class);
            startActivity(scanActivity);
        } else {
            Intent splashActivity = new Intent(this, SplashActivity.class);
            startActivity(splashActivity);
        }

        finish();
    }
}
