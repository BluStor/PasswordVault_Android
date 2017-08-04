package co.blustor.pwv.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import java.util.UUID;

import co.blustor.pwv.R;
import co.blustor.pwv.database.Vault;
import co.blustor.pwv.database.VaultEntry;
import co.blustor.pwv.database.VaultGroup;
import co.blustor.pwv.sync.SyncManager;
import co.blustor.pwv.utils.MyApplication;

import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class AddEntryActivity extends LockingActivity {
    private static final int REQUEST_ICON_CODE = 0;
    private static final int REQUEST_PASSWORD = 1;
    private final AwesomeValidation mAwesomeValidation = new AwesomeValidation(BASIC);
    @Nullable
    private VaultGroup mGroup;
    private Integer mIconId = 0;
    private ImageView mIconImageView = null;
    private EditText mTitleEditText = null;
    private EditText mUsernameEditText = null;
    private EditText mPasswordEditText = null;
    private EditText mUrlEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addentry);

        // Validation

        mAwesomeValidation.addValidation(this, R.id.edittext_title, RegexTemplate.NOT_EMPTY, R.string.error_empty);

        // Views

        setTitle("Add entry");
        mIconImageView = findViewById(R.id.imageview_icon);
        mTitleEditText = findViewById(R.id.edittext_title);
        mUsernameEditText = findViewById(R.id.edittext_username);
        mPasswordEditText = findViewById(R.id.edittext_password);
        mUrlEditText = findViewById(R.id.edittext_url);

        mIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                Intent iconPickerActivity = new Intent(v.getContext(), IconPickerActivity.class);
                startActivityForResult(iconPickerActivity, REQUEST_ICON_CODE);
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
        mGroup = vault.getGroupByUUID(uuid);
        if (mGroup != null) {
            load();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addentry, menu);
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
        mIconImageView.setImageResource(MyApplication.getIcons().get(0));
    }

    private void save() {
        if (mAwesomeValidation.validate()) {
            assert mGroup != null;

            VaultEntry entry = new VaultEntry(
                    mGroup.getUUID(),
                    UUID.randomUUID(),
                    mTitleEditText.getText().toString(),
                    mUsernameEditText.getText().toString(),
                    mPasswordEditText.getText().toString()
            );
            entry.setUrl(mUrlEditText.getText().toString());
            entry.setIconId(mIconId);
            mGroup.add(entry);

            Vault vault = Vault.getInstance();

            SyncManager.setRoot(this, vault.getPassword());
            finish();
        }
    }
}
