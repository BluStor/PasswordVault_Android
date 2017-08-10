package co.blustor.pwv.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.github.clans.fab.FloatingActionButton;

import java.util.UUID;

import co.blustor.pwv.R;
import co.blustor.pwv.database.Vault;
import co.blustor.pwv.fragments.SyncDialogFragment;
import co.blustor.pwv.sync.SyncManager;

import static co.blustor.pwv.fragments.SyncDialogFragment.SyncInterface;

public class CreateActivity extends AppCompatActivity implements SyncInterface {
    private TextInputLayout mPasswordTextInputLayout = null;
    private EditText mPasswordEditText = null;
    private TextInputLayout mPasswordRepeatTextInputLayout = null;
    private EditText mPasswordRepeatEditText = null;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            validate();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        setTitle("New database");

        // Views

        mPasswordTextInputLayout = findViewById(R.id.textinputlayout_password);
        mPasswordEditText = findViewById(R.id.edittext_password);
        mPasswordEditText.addTextChangedListener(mTextWatcher);
        mPasswordRepeatTextInputLayout = findViewById(R.id.textinputlayout_password_repeat);
        mPasswordRepeatEditText = findViewById(R.id.edittext_password_repeat);
        mPasswordRepeatEditText.addTextChangedListener(mTextWatcher);

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                create();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_about) {
            Intent aboutActivity = new Intent(this, AboutActivity.class);
            startActivity(aboutActivity);
        } else if (itemId == R.id.action_existing) {
            Intent unlockActivity = new Intent(getApplicationContext(), UnlockActivity.class);
            startActivity(unlockActivity);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void syncComplete(UUID uuid) {
        Intent groupActivity = new Intent(CreateActivity.this, GroupActivity.class);
        groupActivity.putExtra("uuid", uuid);
        startActivity(groupActivity);

        finish();
    }

    private void create() {
        if (validate()) {
            new AlertDialog.Builder(CreateActivity.this)
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
                            args.putSerializable("password", mPasswordEditText.getText().toString());

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

    private boolean validate() {
        String password = mPasswordEditText.getText().toString();
        String passwordRepeat = mPasswordRepeatEditText.getText().toString();

        boolean hasPassword = password.length() > 0;
        boolean passwordsMatch = password.equals(passwordRepeat);

        if (hasPassword) {
            mPasswordTextInputLayout.setError(null);
        } else {
            mPasswordTextInputLayout.setError(getString(R.string.error_password_is_required));
        }

        if (passwordsMatch) {
            mPasswordRepeatTextInputLayout.setError(null);
        } else {
            mPasswordRepeatTextInputLayout.setError(getString(R.string.error_passwords_do_not_match));
        }

        return hasPassword && passwordsMatch;
    }
}
