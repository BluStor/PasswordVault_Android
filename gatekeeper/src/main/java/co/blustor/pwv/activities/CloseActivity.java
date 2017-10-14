package co.blustor.pwv.activities;

import android.support.v7.app.AppCompatActivity;

public class CloseActivity extends AppCompatActivity {
    @Override
    protected void onStart() {
        super.onStart();
        finish();
    }
}
