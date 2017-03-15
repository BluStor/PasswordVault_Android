package co.blustor.passwordvault.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.database.VaultGroup;
import co.blustor.passwordvault.extensions.LockingActivity;

import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class AddGroupActivity extends LockingActivity {
    private static final String TAG = "AddGroupActivity";
    private final AwesomeValidation mAwesomeValidation = new AwesomeValidation(BASIC);
    private VaultGroup mGroup;
    private EditText mNameEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        // Validation

        mAwesomeValidation.addValidation(this, R.id.edittext_name, RegexTemplate.NOT_EMPTY, R.string.error_empty);

        // Views

        setTitle("Add group");
        mNameEditText = (EditText) findViewById(R.id.edittext_name);

        // Load

        UUID uuid = (UUID) getIntent().getSerializableExtra("uuid");

        try {
            Vault vault = Vault.getInstance(this);
            mGroup = vault.getGroupByUUID(uuid);
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
            if (save()) {
                finish();
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
                        finish();
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private Boolean save() {
        if (mAwesomeValidation.validate()) {
            VaultGroup group = new VaultGroup(
                    mGroup.getUUID(),
                    UUID.randomUUID(),
                    mNameEditText.getText().toString()
            );
            mGroup.add(group);
            return true;
        } else {
            return false;
        }
    }
}
