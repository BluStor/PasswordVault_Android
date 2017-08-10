package co.blustor.pwv.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.UUID;

import co.blustor.pwv.R;
import co.blustor.pwv.database.Vault;
import co.blustor.pwv.database.VaultEntry;
import co.blustor.pwv.sync.SyncManager;
import co.blustor.pwv.utils.MyApplication;

public class EditEntryActivity extends LockingActivity {
    private static final int REQUEST_ICON_CODE = 0;
    private static final int REQUEST_PASSWORD = 1;

    @Nullable
    private VaultEntry mEntry = null;
    private Integer mIconId = 0;
    private ImageView mIconImageView = null;
    private TextInputLayout mTitleTextInputLayout = null;
    private EditText mTitleEditText = null;
    private EditText mUsernameEditText = null;
    private EditText mPasswordEditText = null;
    private EditText mUrlEditText = null;
    private EditText mNotesEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editentry);

        setTitle("Edit entry");

        // Views

        mIconImageView = findViewById(R.id.imageview_icon);
        mTitleTextInputLayout = findViewById(R.id.textinputlayout_title);
        mTitleEditText = findViewById(R.id.edittext_title);
        mUsernameEditText = findViewById(R.id.edittext_username);
        mPasswordEditText = findViewById(R.id.edittext_password);
        mUrlEditText = findViewById(R.id.edittext_url);
        mNotesEditText = findViewById(R.id.edittext_notes);

        mIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                Intent iconPickerActivity = new Intent(v.getContext(), IconPickerActivity.class);
                startActivityForResult(iconPickerActivity, REQUEST_ICON_CODE);
            }
        });

        Button copyButton = findViewById(R.id.button_copy);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("text", mPasswordEditText.getText()));
                    Toast.makeText(EditEntryActivity.this, "Password copied", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button generateButton = findViewById(R.id.button_generate);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                Intent passswordGeneratorActivity = new Intent(v.getContext(), PasswordGeneratorActivity.class);
                startActivityForResult(passswordGeneratorActivity, REQUEST_PASSWORD);
            }
        });

        // Load

        UUID uuid = (UUID) getIntent().getSerializableExtra("uuid");

        Vault vault = Vault.getInstance();
        mEntry = vault.getEntryByUUID(uuid);

        if (mEntry != null) {
            load();
        } else {
            supportFinishAfterTransition();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editentry, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        if (requestCode == REQUEST_ICON_CODE) {
            if (resultCode == RESULT_OK) {
                mIconId = data.getIntExtra("icon", 0);
                mIconImageView.setImageResource(MyApplication.getIcons().get(mIconId));
            }
        } else if (requestCode == REQUEST_PASSWORD) {
            if (resultCode == RESULT_OK) {
                mPasswordEditText.setText(data.getStringExtra("password"));
            }
        }
    }

    private void load() {
        assert mEntry != null;

        mTitleEditText.setText(mEntry.getTitle());
        mUsernameEditText.setText(mEntry.getUsername());
        mPasswordEditText.setText(mEntry.getPassword());
        mUrlEditText.setText(mEntry.getUrl());
        mNotesEditText.setText(mEntry.getNotes());

        mIconId = mEntry.getIconId();
        mIconImageView.setImageResource(MyApplication.getIcons().get(mIconId));

        mTitleEditText.setSelection(mTitleEditText.getText().length());
    }

    private void save() {
        if (validate()) {
            if (hasBeenEdited()) {
                assert mEntry != null;

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

    private boolean hasBeenEdited() {
        assert mEntry != null;

        return !(mEntry.getTitle().equals(mTitleEditText.getText().toString())
                && mEntry.getUsername().equals(mUsernameEditText.getText().toString())
                && mEntry.getPassword().equals(mPasswordEditText.getText().toString())
                && mEntry.getUrl().equals(mUrlEditText.getText().toString())
                && mEntry.getNotes().equals(mNotesEditText.getText().toString())
                && mEntry.getIconId().equals(mIconId));
    }

    private boolean validate() {
        boolean hasTitle = mTitleEditText.getText().length() > 0;

        if (hasTitle) {
            mTitleTextInputLayout.setError(null);
        } else {
            mTitleTextInputLayout.setError(getString(R.string.error_title_is_required));
        }

        return hasTitle;
    }
}
