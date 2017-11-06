package co.blustor.identity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.UUID;

import co.blustor.identity.R;
import co.blustor.identity.sync.SyncManager;
import co.blustor.identity.utils.MyApplication;
import co.blustor.identity.vault.Vault;
import co.blustor.identity.vault.VaultGroup;


public class EditGroupActivity extends LockingActivity {
    private static final int REQUEST_ICON_CODE = 0;

    @Nullable
    private VaultGroup mGroup = null;
    private Integer mIconId = 49;
    private ImageView mIconImageView = null;
    private TextInputLayout mNameTextInputLayout = null;
    private EditText mNameEditText = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editgroup);

        setTitle("Edit group");

        // Views

        mIconImageView = findViewById(R.id.imageview_icon);
        mNameTextInputLayout = findViewById(R.id.textinputlayout_name);
        mNameEditText = findViewById(R.id.edittext_name);

        mIconImageView.setOnClickListener(v -> {
            Intent iconPickerActivity = new Intent(v.getContext(), IconPickerActivity.class);
            startActivityForResult(iconPickerActivity, REQUEST_ICON_CODE);
        });

        // Load

        UUID uuid = (UUID) getIntent().getSerializableExtra("uuid");

        Vault vault = Vault.getInstance();
        mGroup = vault.getGroupByUUID(uuid);

        if (mGroup != null) {
            load();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ICON_CODE) {
            if (resultCode == RESULT_OK) {
                mIconId = data.getIntExtra("icon", 49);
                mIconImageView.setImageResource(MyApplication.getIcons().get(mIconId));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editgroup, menu);
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
                    .setPositiveButton("Close", (dialog, which) -> finish())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            finish();
        }
    }

    private void load() {
        assert mGroup != null;

        mIconImageView.setImageResource(MyApplication.getIcons().get(mGroup.getIconId()));

        mNameEditText.setText(mGroup.getName());
        mNameEditText.setSelection(mNameEditText.getText().length());

        mIconId = mGroup.getIconId();
    }

    private void save() {
        if (validate()) {
            if (hasBeenEdited()) {
                assert mGroup != null;

                mGroup.setName(mNameEditText.getText().toString());
                mGroup.setIconId(mIconId);

                Vault vault = Vault.getInstance();
                SyncManager.setRoot(this, vault.getPassword());
            }

            finish();
        }
    }

    private boolean hasBeenEdited() {
        assert mGroup != null;

        return !(mGroup.getName().equals(mNameEditText.getText().toString())
                && mGroup.getIconId().equals(mIconId));
    }

    private boolean validate() {
        boolean hasName = mNameEditText.getText().toString().length() > 0;

        if (hasName) {
            mNameTextInputLayout.setError(null);
        } else {
            mNameTextInputLayout.setError(getString(R.string.error_name_is_required));
        }

        return hasName;
    }
}
