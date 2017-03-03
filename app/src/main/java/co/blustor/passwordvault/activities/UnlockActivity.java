package co.blustor.passwordvault.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nanotasks.BackgroundWork;
import com.nanotasks.Completion;
import com.nanotasks.Tasks;

import java.util.ArrayList;
import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.services.NotificationService;
import co.blustor.passwordvault.utils.AlertUtils;

public class UnlockActivity extends AppCompatActivity {
    private static final String TAG = "UnlockActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        // Initialize if keePassDatabase does not exist

        Vault vault = Vault.getInstance(this);
        if (!vault.exists()) {
            Intent initializeActivity = new Intent(this, InitializeActivity.class);
            startActivity(initializeActivity);
            finish();
        }

        // Views

        final EditText passwordTextView = (EditText)findViewById(R.id.edittext_password);

        Button unlockButton = (Button)findViewById(R.id.button_unlock);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = passwordTextView.getText();
                openVault(editable.toString());
                editable.clear();
            }
        });
    }

    public void openVault(final String password) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Opening ...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        final Context context = this;

        Tasks.executeInBackground(this, new BackgroundWork<UUID>() {
            @Override
            public UUID doInBackground() throws Exception {
                Vault vault = Vault.getInstance(context);
                vault.unlock(password);
                return vault.getRoot().getUUID();
            }
        }, new Completion<UUID>() {
            @Override
            public void onSuccess(Context context, UUID result) {
                progressDialog.cancel();

                Intent groupActivity = new Intent(context, GroupActivity.class);
                groupActivity.putExtra("uuid", result);
                groupActivity.putStringArrayListExtra("path", new ArrayList<String>());
                startActivity(groupActivity);

                Intent notificationService = new Intent(context, NotificationService.class);
                startService(notificationService);
            }

            @Override
            public void onError(Context context, Exception e) {
                progressDialog.cancel();
                if (e.getClass() == Vault.PasswordInvalidException.class) {
                    AlertUtils.showError(context, "Your password was invalid.");
                }
            }
        });
    }
}
