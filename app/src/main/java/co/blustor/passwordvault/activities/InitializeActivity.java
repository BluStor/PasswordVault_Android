package co.blustor.passwordvault.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.utils.AlertUtils;

import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class InitializeActivity extends AppCompatActivity {
    private static final String TAG = "InitializeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize);

        // Validation

        final AwesomeValidation awesomeValidation = new AwesomeValidation(BASIC);
        awesomeValidation.addValidation(this, R.id.edittext_password, RegexTemplate.NOT_EMPTY, R.string.error_empty);
        awesomeValidation.addValidation(this, R.id.edittext_password_repeat, RegexTemplate.NOT_EMPTY, R.string.error_empty);
        awesomeValidation.addValidation(this, R.id.edittext_password, R.id.edittext_password_repeat, R.string.error_match);

        // Views

        final EditText passwordEditText = (EditText)findViewById(R.id.edittext_password);

        Button createButton = (Button) findViewById(R.id.button_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (awesomeValidation.validate()) {
                    createDatabase(passwordEditText.getText().toString());
                }
            }
        });
    }

    private void createDatabase(String password) {
        try {
            Vault vault = Vault.getInstance(this);
            vault.create();
            vault.save(password);

            Intent unlockActivity = new Intent(this, UnlockActivity.class);
            startActivity(unlockActivity);
            finish();
        } catch (Vault.NotFoundException e) {
            AlertUtils.showError(this, "Unable to create databse.");
        }
    }
}
