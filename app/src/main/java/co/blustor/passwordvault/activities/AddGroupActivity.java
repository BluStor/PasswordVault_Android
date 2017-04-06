package co.blustor.passwordvault.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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
import co.blustor.passwordvault.database.VaultGroup;
import co.blustor.passwordvault.sync.SyncManager;
import co.blustor.passwordvault.utils.MyApplication;

import static co.blustor.passwordvault.activities.IconPickerActivity.REQUEST_ICON_CODE;
import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class AddGroupActivity extends LockingActivity {
    private static final String TAG = "AddGroupActivity";
    private final AwesomeValidation mAwesomeValidation = new AwesomeValidation(BASIC);
    private VaultGroup mGroup;
    private Integer mIconId = 49;
    private ImageView mIconImageView = null;
    private EditText mNameEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        setTitle("Add group");

        // Validation

        mAwesomeValidation.addValidation(this, R.id.edittext_name, RegexTemplate.NOT_EMPTY, R.string.error_empty);

        // Views

        mIconImageView = (ImageView) findViewById(R.id.imageview_icon);
        mNameEditText = (EditText) findViewById(R.id.edittext_name);

        mIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iconPickerActivity = new Intent(v.getContext(), IconPickerActivity.class);
                startActivityForResult(iconPickerActivity, REQUEST_ICON_CODE);
            }
        });

        // Load

        UUID uuid = (UUID) getIntent().getSerializableExtra("uuid");

        try {
            Vault vault = Vault.getInstance();
            mGroup = vault.getGroupByUUID(uuid);

            load();
        } catch (Vault.GroupNotFoundException e) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ICON_CODE) {
            if (resultCode == RESULT_OK) {
                mIconId = data.getIntExtra("icon", 49);
                mIconImageView.setImageResource(MyApplication.getIcons().get(mIconId));
            }
        }
    }

    private void load() {
        mIconImageView.setImageResource(MyApplication.getIcons().get(49));
    }

    private void save() {
        if (mAwesomeValidation.validate()) {
            VaultGroup group = new VaultGroup(
                    mGroup.getUUID(),
                    UUID.randomUUID(),
                    mNameEditText.getText().toString()
            );
            group.setIconId(mIconId);

            mGroup.add(group);

            Log.d(TAG, "After add, group's icon is " + mGroup.getIconId());

            Vault vault = Vault.getInstance();

            SyncManager.setRoot(this, vault.getPassword());
            finish();
        }
    }
}
