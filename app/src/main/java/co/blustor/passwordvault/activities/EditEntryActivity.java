package co.blustor.passwordvault.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.database.VaultEntry;
import co.blustor.passwordvault.database.VaultGroup;
import co.blustor.passwordvault.extensions.LockingActivity;

import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class EditEntryActivity extends LockingActivity {
    private static final String TAG = "EditEntryActivity";

    private VaultGroup mGroup = null;
    private VaultEntry mEntry = null;

    private final AwesomeValidation mAwesomeValidation = new AwesomeValidation(BASIC);
    private EditText mTitleEditText = null;
    private EditText mUsernameEditText = null;
    private EditText mPasswordEditText = null;
    private EditText mUrlEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);

        // Validation

        mAwesomeValidation.addValidation(this, R.id.edittext_title, RegexTemplate.NOT_EMPTY, R.string.error_empty);

        // Views

        mTitleEditText = (EditText)findViewById(R.id.edittext_title);
        mUsernameEditText = (EditText)findViewById(R.id.edittext_username);
        mPasswordEditText = (EditText)findViewById(R.id.edittext_password);
        mUrlEditText = (EditText)findViewById(R.id.edittext_url);

        // Load

        UUID groupUUID = (UUID)getIntent().getSerializableExtra("groupUUID");
        UUID uuid = (UUID)getIntent().getSerializableExtra("uuid");

        try {
            Vault vault = Vault.getInstance(this);

            mGroup = vault.getGroupByUUID(groupUUID);
            mEntry = mGroup.getEntry(uuid);
            load();
        } catch (Vault.GroupNotFoundException | VaultGroup.EntryNotFoundException e) {
            e.printStackTrace();
            supportFinishAfterTransition();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_entry, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_unmask) {
            if (mPasswordEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                mPasswordEditText.setTransformationMethod(null);
            } else {
                mPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
            }
        } else if (id == R.id.action_delete) {
            delete();
        } else if (id == R.id.action_save) {
            if (save()) {
                supportFinishAfterTransition();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Close without saving?")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        supportFinishAfterTransition();
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void load() {
        setTitle("Edit entry");

        mTitleEditText.setText(mEntry.getTitle());
        mUsernameEditText.setText(mEntry.getUsername());
        mPasswordEditText.setText(mEntry.getPassword());
        mUrlEditText.setText(mEntry.getUrl());
    }

    private void delete() {
        final Context context = this;

        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this entry?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mGroup.removeEntry(mEntry.getUUID());

                        Toast.makeText(context, "Entry deleted.", Toast.LENGTH_SHORT).show();

                        finish();
                    }
                }).show();
    }

    private Boolean save() {
        if (mAwesomeValidation.validate()) {
            mEntry.setTitle(mTitleEditText.getText().toString());
            mEntry.setUsername(mUsernameEditText.getText().toString());
            mEntry.setPassword(mPasswordEditText.getText().toString());
            mEntry.setUrl(mUrlEditText.getText().toString());
            return true;
        } else {
            return false;
        }
    }
}
