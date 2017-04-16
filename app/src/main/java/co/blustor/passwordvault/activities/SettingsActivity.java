package co.blustor.passwordvault.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.fragments.SyncDialogFragment;
import co.blustor.passwordvault.sync.SyncManager;

import static co.blustor.passwordvault.fragments.SyncDialogFragment.SyncInterface;

public class SettingsActivity extends AppCompatActivity implements SyncInterface {
    private final AwesomeValidation mAwesomeValidationChangePassword = new AwesomeValidation(ValidationStyle.BASIC);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Validation

        mAwesomeValidationChangePassword.addValidation(this, R.id.edittext_password, RegexTemplate.NOT_EMPTY, R.string.error_empty);
        mAwesomeValidationChangePassword.addValidation(this, R.id.edittext_password, R.id.edittext_password_repeat, R.string.error_match);

        // Views

        final EditText passwordEditText = (EditText) findViewById(R.id.edittext_password);

        Button changePasswordButton = (Button) findViewById(R.id.button_change_password);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAwesomeValidationChangePassword.validate()) {
                    Vault vault = Vault.getInstance();
                    vault.setPassword(passwordEditText.getText().toString());

                    save();
                }
            }
        });
    }

    @Override
    public void syncComplete(UUID uuid) {
        finish();
    }

    private void save() {
        Vault vault = Vault.getInstance();

        SyncDialogFragment syncDialogFragment = new SyncDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("type", SyncManager.SyncType.WRITE);
        args.putSerializable("password", vault.getPassword());

        syncDialogFragment.setArguments(args);
        syncDialogFragment.show(getFragmentManager(), "dialog");
    }
}
