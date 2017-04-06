package co.blustor.passwordvault.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.database.VaultEntry;
import co.blustor.passwordvault.database.VaultGroup;
import co.blustor.passwordvault.sync.SyncManager;
import co.blustor.passwordvault.utils.MyApplication;

import static co.blustor.passwordvault.activities.IconPickerActivity.REQUEST_ICON_CODE;
import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class EditEntryActivity extends LockingActivity {
    private static final String TAG = "EditEntryActivity";
    private final AwesomeValidation mAwesomeValidation = new AwesomeValidation(BASIC);
    private VaultGroup mGroup = null;
    private VaultEntry mEntry = null;
    private Integer mIconId = 0;
    private ImageView mIconImageView = null;
    private EditText mTitleEditText = null;
    private EditText mUsernameEditText = null;
    private EditText mPasswordEditText = null;
    private EditText mUrlEditText = null;
    private EditText mNotesEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);

        setTitle("Edit entry");

        // Validation

        mAwesomeValidation.addValidation(this, R.id.edittext_title, RegexTemplate.NOT_EMPTY, R.string.error_empty);

        // Views

        mIconImageView = (ImageView) findViewById(R.id.imageview_icon);
        mTitleEditText = (EditText) findViewById(R.id.edittext_title);
        mUsernameEditText = (EditText) findViewById(R.id.edittext_username);
        mPasswordEditText = (EditText) findViewById(R.id.edittext_password);
        mUrlEditText = (EditText) findViewById(R.id.edittext_url);
        mNotesEditText = (EditText) findViewById(R.id.edittext_notes);

        mIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iconPickerActivity = new Intent(v.getContext(), IconPickerActivity.class);
                startActivityForResult(iconPickerActivity, REQUEST_ICON_CODE);
            }
        });

        // Load

        UUID groupUUID = (UUID) getIntent().getSerializableExtra("groupUUID");
        UUID uuid = (UUID) getIntent().getSerializableExtra("uuid");

        try {
            Vault vault = Vault.getInstance();

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

        if (id == R.id.action_delete) {
            delete();
        } else if (id == R.id.action_save) {
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
                            supportFinishAfterTransition();
                        }

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            supportFinishAfterTransition();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ICON_CODE) {
            if (resultCode == RESULT_OK) {
                mIconId = data.getIntExtra("icon", 0);
                mIconImageView.setImageResource(MyApplication.getIcons().get(mIconId));
            }
        }
    }

    private void load() {
        mTitleEditText.setText(mEntry.getTitle());
        mUsernameEditText.setText(mEntry.getUsername());
        mPasswordEditText.setText(mEntry.getPassword());
        mUrlEditText.setText(mEntry.getUrl());
        mNotesEditText.setText(mEntry.getNotes());

        mIconId = mEntry.getIconId();
        mIconImageView.setImageResource(MyApplication.getIcons().get(mIconId));

        mTitleEditText.setSelection(mTitleEditText.getText().length());
    }

    private void delete() {
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

                        Vault vault = Vault.getInstance();

                        SyncManager.setRoot(EditEntryActivity.this, vault.getPassword());
                        finish();
                    }
                }).show();
    }

    private void save() {
        if (mAwesomeValidation.validate()) {
            if (hasBeenEdited()) {
                mEntry.setTitle(mTitleEditText.getText().toString());
                mEntry.setUsername(mUsernameEditText.getText().toString());
                mEntry.setPassword(mPasswordEditText.getText().toString());
                mEntry.setUrl(mUrlEditText.getText().toString());
                mEntry.setNotes(mNotesEditText.getText().toString());
                mEntry.setIconId(mIconId);

                Vault vault = Vault.getInstance();

                SyncManager.setRoot(this, vault.getPassword());
            }

            finish();
        }
    }

    private Boolean hasBeenEdited() {
        return !(mEntry.getTitle().equals(mTitleEditText.getText().toString())
                && mEntry.getUsername().equals(mUsernameEditText.getText().toString())
                && mEntry.getPassword().equals(mPasswordEditText.getText().toString())
                && mEntry.getUrl().equals(mUrlEditText.getText().toString())
                && mEntry.getNotes().equals(mNotesEditText.getText().toString())
                && mEntry.getIconId().equals(mIconId));
    }
}
