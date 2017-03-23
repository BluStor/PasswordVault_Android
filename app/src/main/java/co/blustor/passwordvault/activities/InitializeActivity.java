package co.blustor.passwordvault.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.github.clans.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.sync.SyncDialogFragment;
import co.blustor.passwordvault.sync.SyncManager;

import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class InitializeActivity extends AppCompatActivity implements SyncDialogFragment.SyncInterface {
    private static final String TAG = "InitializeActivity";
    private final AwesomeValidation mAwesomeValidation = new AwesomeValidation(BASIC);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize);

        setTitle("New database");

        // Validation

        mAwesomeValidation.addValidation(this, R.id.edittext_password, RegexTemplate.NOT_EMPTY, R.string.error_empty);
        mAwesomeValidation.addValidation(this, R.id.edittext_password, R.id.edittext_password_repeat, R.string.error_match);

        // Views

        final EditText passwordTextView = (EditText) findViewById(R.id.edittext_password);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mAwesomeValidation.validate()) {
                    Vault vault = Vault.getInstance();
                    vault.create();

                    SyncDialogFragment syncDialogFragment = new SyncDialogFragment();

                    Bundle args = new Bundle();
                    args.putSerializable("type", SyncManager.SyncType.WRITE);
                    args.putSerializable("password", passwordTextView.getText().toString());

                    syncDialogFragment.setArguments(args);
                    syncDialogFragment.show(getFragmentManager(), "dialog");
                }
            }
        });
    }

    @Override
    public void syncComplete(UUID uuid) {
        Intent groupActivity = new Intent(InitializeActivity.this, GroupActivity.class);
        groupActivity.putExtra("uuid", uuid);
        groupActivity.putExtra("path", new ArrayList<String>());
        startActivity(groupActivity);

        finish();
    }
}
