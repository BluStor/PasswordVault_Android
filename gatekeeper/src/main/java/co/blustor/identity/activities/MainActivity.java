package co.blustor.identity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.zwsb.palmsdk.PalmSDK;

import co.blustor.identity.vault.Vault;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String address = Vault.getCardAddress(this);

        PalmSDK.init(this, "sdk@blustor.com", new PalmSDK.InitSDKCallback() {
            @Override
            public void onSuccess() {
                /**
                 * Do something
                 */
            }
            @Override
            public void onError() {
                /**
                 * Do something
                 */
            }
        });

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
