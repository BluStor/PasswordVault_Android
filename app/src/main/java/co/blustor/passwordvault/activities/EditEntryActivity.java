package co.blustor.passwordvault.activities;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.database.VaultEntry;
import co.blustor.passwordvault.database.VaultGroup;

import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class EditEntryActivity extends AppCompatActivity {
    private static final String TAG = "EditEntryActivity";

    private VaultEntry mEntry = null;

    private AwesomeValidation mAwesomeValidation = new AwesomeValidation(BASIC);
    private EditText mTitleEditText = null;
    private EditText mUsernameEditText = null;
    private EditText mPasswordEditText = null;
    private EditText mUriEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);

        // Validation

        mAwesomeValidation.addValidation(this, R.id.edittext_title, RegexTemplate.NOT_EMPTY, R.string.error_empty);
        mAwesomeValidation.addValidation(this, R.id.edittext_username, RegexTemplate.NOT_EMPTY, R.string.error_empty);

        // Views

        mTitleEditText = (EditText)findViewById(R.id.edittext_title);
        mUsernameEditText = (EditText)findViewById(R.id.edittext_username);
        mPasswordEditText = (EditText)findViewById(R.id.edittext_password);
        mUriEditText = (EditText)findViewById(R.id.edittext_uri);

        // Load

        UUID groupUUID = (UUID)getIntent().getSerializableExtra("groupUUID");
        UUID uuid = (UUID)getIntent().getSerializableExtra("uuid");

        try {
            Vault vault = Vault.getInstance(this);
            VaultGroup group = vault.getGroupByUUID(groupUUID);
            mEntry = group.getEntry(uuid);
            load();
        } catch (Vault.GroupNotFoundException | VaultGroup.EntryNotFoundException e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (save()) {
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Warning")
                .setMessage("Close without saving?")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void load() {
        setTitle("Edit entry");

        mTitleEditText.setText(mEntry.getTitle());
        mUsernameEditText.setText(mEntry.getUsername());
        mPasswordEditText.setText(mEntry.getPassword());
        mUriEditText.setText(mEntry.getUri());
    }

    public Boolean save() {
        if (mAwesomeValidation.validate()) {
            mEntry.setTitle(mTitleEditText.getText().toString());
            mEntry.setUsername(mUsernameEditText.getText().toString());
            mEntry.setPassword(mPasswordEditText.getText().toString());
            mEntry.setUri(mUriEditText.getText().toString());
            return true;
        } else {
            return false;
        }
    }
}
