package co.blustor.pwv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;

import co.blustor.pwv.R;
import co.blustor.pwv.sync.SyncManager;
import co.blustor.pwv.sync.SyncManager.SyncManagerException;
import co.blustor.pwv.sync.SyncManager.SyncStatus;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Promise<Boolean, SyncManagerException, SyncStatus> promise = SyncManager.exists(this);

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(promise).done(new DoneCallback<Boolean>() {
            @Override
            public void onDone(Boolean result) {
                Intent startupActivity;
                if (result) {
                    startupActivity = new Intent(SplashActivity.this, UnlockActivity.class);
                } else {
                    Intent unlockActivity = new Intent(SplashActivity.this, UnlockActivity.class);
                    unlockActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(unlockActivity);

                    startupActivity = new Intent(SplashActivity.this, CreateActivity.class);
                }

                startActivity(startupActivity);
                finish();
            }
        }).fail(new FailCallback<SyncManagerException>() {
            @Override
            public void onFail(SyncManagerException result) {
                Intent unlockActivity = new Intent(SplashActivity.this, UnlockActivity.class);
                startActivity(unlockActivity);
                finish();
            }
        });
    }
}
