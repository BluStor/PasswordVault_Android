package co.blustor.pwv.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

import co.blustor.pwv.R;
import co.blustor.pwv.database.Vault;
import co.blustor.pwv.fragments.SyncDialogFragment;

public class SettingsActivity extends LockingActivity implements SyncDialogFragment.SyncListener {

    private TextInputLayout mPasswordTextInputLayout = null;
    private EditText mPasswordEditText = null;
    private TextInputLayout mPasswordRepeatTextInputLayout = null;
    private EditText mPasswordRepeatEditText = null;

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            validatePassword();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Views

        mPasswordTextInputLayout = findViewById(R.id.textinputlayout_password);
        mPasswordEditText = findViewById(R.id.edittext_password);
        mPasswordEditText.addTextChangedListener(mTextWatcher);
        mPasswordRepeatTextInputLayout = findViewById(R.id.textinputlayout_password_repeat);
        mPasswordRepeatEditText = findViewById(R.id.edittext_password_repeat);
        mPasswordRepeatEditText.addTextChangedListener(mTextWatcher);

        Button changePasswordButton = findViewById(R.id.button_change_password);
        changePasswordButton.setOnClickListener(v -> savePassword());
    }

    @Override
    public void syncComplete(UUID uuid) {
        finish();
    }

    private void savePassword() {
        if (validatePassword()) {
            String password = mPasswordEditText.getText().toString();

            Vault vault = Vault.getInstance();
            vault.setPassword(password);

            save();
        }
    }

    private void save() {
        Vault vault = Vault.getInstance();

        SyncDialogFragment syncDialogFragment = new SyncDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("type", "write");
        args.putSerializable("password", vault.getPassword());

        syncDialogFragment.setArguments(args);
        syncDialogFragment.show(getFragmentManager(), "dialog");
    }

    private boolean validatePassword() {
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
