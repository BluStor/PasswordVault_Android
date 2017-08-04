package co.blustor.pwv.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.github.clans.fab.FloatingActionButton;

import java.util.UUID;

import co.blustor.pwv.R;
import co.blustor.pwv.database.Vault;
import co.blustor.pwv.fragments.SyncDialogFragment;
import co.blustor.pwv.sync.SyncManager;

import static co.blustor.pwv.fragments.SyncDialogFragment.SyncInterface;
import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class CreateActivity extends AppCompatActivity implements SyncInterface {
    private final AwesomeValidation mAwesomeValidation = new AwesomeValidation(BASIC);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        setTitle("New database");

        // Validation

        mAwesomeValidation.addValidation(this, R.id.edittext_password, RegexTemplate.NOT_EMPTY, R.string.error_empty);
        mAwesomeValidation.addValidation(this, R.id.edittext_password, R.id.edittext_password_repeat, R.string.error_match);

        // Views

        final EditText passwordTextView = findViewById(R.id.edittext_password);

        final Context context = this;

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mAwesomeValidation.validate()) {
                    new AlertDialog.Builder(context)
                            .setTitle("Replace database?")
                            .setMessage("This will replace the password database on your card, potentially destroying data.  Are you sure?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(@NonNull DialogInterface dialog, int which) {
                                    dialog.cancel();

                                    Vault vault = Vault.getInstance();
                                    vault.create();

                                    SyncDialogFragment syncDialogFragment = new SyncDialogFragment();

                                    Bundle args = new Bundle();
                                    args.putSerializable("type", SyncManager.SyncType.WRITE);
                                    args.putSerializable("password", passwordTextView.getText().toString());

                                    syncDialogFragment.setArguments(args);
                                    syncDialogFragment.show(getFragmentManager(), "dialog");
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(@NonNull DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
            }
        });
    }

    @Override
    public void syncComplete(UUID uuid) {
        Intent groupActivity = new Intent(CreateActivity.this, GroupActivity.class);
        groupActivity.putExtra("uuid", uuid);
        startActivity(groupActivity);

        finish();
    }
}
