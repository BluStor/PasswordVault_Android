package co.blustor.passwordvault.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.database.VaultGroup;
import co.blustor.passwordvault.sync.SyncManager;

import static co.blustor.passwordvault.activities.IconPickerActivity.REQUEST_ICON_CODE;
import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class EditGroupActivity extends LockingActivity {
    private static final String TAG = "EditGroupActivity";
    private final AwesomeValidation mAwesomeValidation = new AwesomeValidation(BASIC);
    private VaultGroup mGroup = null;
    private Integer mIconId = 0;
    private EditText mNameEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        // Validation

        mAwesomeValidation.addValidation(this, R.id.edittext_name, RegexTemplate.NOT_EMPTY, R.string.error_empty);

        // Views

        mNameEditText = (EditText) findViewById(R.id.edittext_name);

        // Load

        UUID uuid = (UUID) getIntent().getSerializableExtra("uuid");

        try {
            Vault vault = Vault.getInstance();
            mGroup = vault.getGroupByUUID(uuid);

            load();
        } catch (Vault.GroupNotFoundException e) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ICON_CODE) {
            mIconId = data.getIntExtra("icon", 0);
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
            save();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasBeenEdited()) {
            new AlertDialog.Builder(this)
                    .setMessage("Close without saving?")
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            finish();
        }
    }

    private void load() {
        setTitle("Edit group");
        mNameEditText.setText(mGroup.getName());

        mNameEditText.setSelection(mNameEditText.getText().length());
    }

    private void save() {
        if (mAwesomeValidation.validate()) {
            if (hasBeenEdited()) {
                mGroup.setName(mNameEditText.getText().toString());
                mGroup.setIconId(mIconId);

                Vault vault = Vault.getInstance();
                SyncManager.setRoot(this, vault.getPassword());
            }

            finish();
        }
    }

    private boolean hasBeenEdited() {
        if (mGroup.getName().equals(mNameEditText.getText().toString())) {
            return false;
        } else {
            Log.d(TAG, "'" + mGroup.getName() + "' does not equal '" + mNameEditText.getText().toString() + "'.");
            return true;
        }
    }
}
