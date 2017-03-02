package co.blustor.passwordvault.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CloseActivity extends AppCompatActivity {
    private static final String TAG = "CloseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }
}
