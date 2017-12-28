package co.blustor.identity.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.github.clans.fab.FloatingActionButton;

import java.util.List;
import java.util.UUID;

import co.blustor.identity.R;
import co.blustor.identity.fragments.SyncDialogFragment;
import co.blustor.identity.utils.LocalPinRepository;

import com.zwsb.palmsdk.activities.AuthActivity;
import com.zwsb.palmsdk.activities.PalmActivity;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;
import static com.zwsb.palmsdk.activities.AuthActivity.NEW_USER_ACTION;
import static com.zwsb.palmsdk.activities.AuthActivity.ON_SCAN_RESULT_OK;
import static com.zwsb.palmsdk.activities.AuthActivity.ON_SCAN_RESULT_ERROR;

public class UnlockActivity extends AppCompatActivity implements SyncDialogFragment.SyncListener {

    private static final String TAG = "UnlockActivity";
    private EditText mPasswordEditText;
    private final static String mPalm_user = "Palm Authentication";
    private static final int AUTH_REQUEST_CODE = 7;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        setTitle(getApplicationTitle());

        mPasswordEditText = findViewById(R.id.edittext_password);

        mPasswordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggBtn);
        if (isPalmUserRegistered()) {
            toggle.setChecked(true);

            // Log.d(TAG, "Attempt to enroll user");
            // Enrollment.  Based on PDF, this looks like enrollment, but it authenticates the user.
            // Seems to be working, I will leave it like this
            Intent intent = AuthActivity.getIntent(UnlockActivity.this, mPalm_user, false, false);
            startActivityForResult(intent, AuthActivity.NEW_USER_ACTION);

            // Authenticate user
            // Log.d(TAG, "Authenticate user with palm");
            // toggle.setChecked(true);
            // startActivityForResult(PalmActivity.getIntent(UnlockActivity.this, mPalm_user, false, true), 0);

        } else {
            Log.d(TAG, "Set state of toggle to false, user not enrolled");
            toggle.setChecked(false);
        }

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkPermissions();

                if (isChecked) {
                    Log.d(TAG, "Biometic palm enabled");

                    if (isPalmUserRegistered() == false) {
                        Log.d(TAG, "User is NOT registered, start enrollment activity");
                        startActivityForTakeLeftPalm();
                        toggle.setChecked(true);
                    }
                } else {
                    Log.d(TAG, "Disable palm");
                    deletePalmCredentials();
                }
            }
        });

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> submit());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called");

        Log.d(TAG, "requestCode = " + requestCode);

        if (data == null) {return;}

        if (requestCode == AuthActivity.NEW_USER_ACTION) {
            switch (resultCode) {
                case ON_SCAN_RESULT_OK:
                    Log.d(TAG, "User authenticated");
                    LocalPinRepository repo = new LocalPinRepository();
                    String pin = repo.getPin(this);
                    if (pin !=null && pin.length() > 0) {
                        mPasswordEditText.setText(pin);
                        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
                        floatingActionButton.performClick();
                    }
                    break;
                case ON_SCAN_RESULT_ERROR:
                    Log.d(TAG, "Error");
                    break;
            }
        }
    }

    private void deletePalmCredentials() {
        List<String> users = SharedPreferenceHelper.getStringArray(UnlockActivity.this, SharedPreferenceHelper.USER_NAMES_KEY);
        users.remove(mPalm_user);
        SharedPreferenceHelper.setStringArray(UnlockActivity.this, SharedPreferenceHelper.USER_NAMES_KEY, users);
        SharedPreferenceHelper.setLeftPalmEnabled(false, mPalm_user);
        SharedPreferenceHelper.setRightPalmEnabled(false, mPalm_user);
    }

    private boolean isPalmUserRegistered() {
        if (SharedPreferenceHelper.getNumberOfRegisteredPalms(UnlockActivity.this, mPalm_user) >= 1) {
            return true;
        } else {
            return false;
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
        }

    }
    public void startActivityForTakeLeftPalm()
    {
        Log.d(TAG, "Enroll left palm");
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra(AuthActivity.USER_ACTION_KEY, NEW_USER_ACTION);
        intent.putExtra(AuthActivity.IS_RIGHT_PALM_NEEDED_KEY, false);
        intent.putExtra(AuthActivity.USER_NAME_KEY, mPalm_user);

        startActivityForResult(intent, NEW_USER_ACTION);
    }

    public void startActivityForTakeRightPalm()
    {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra(AuthActivity.USER_ACTION_KEY, NEW_USER_ACTION);
        intent.putExtra(AuthActivity.IS_RIGHT_PALM_NEEDED_KEY, true);
        intent.putExtra(AuthActivity.USER_NAME_KEY, mPalm_user);

        startActivityForResult(intent, NEW_USER_ACTION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_unlock, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent aboutActivity = new Intent(this, AboutActivity.class);
            startActivity(aboutActivity);
        } else if (id == R.id.action_choose) {
            Intent chooseActivity = new Intent(this, ChooseActivity.class);
            startActivity(chooseActivity);
        } else if (id == R.id.action_new) {
            Intent createActivity = new Intent(getApplicationContext(), CreateActivity.class);
            startActivity(createActivity);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void syncComplete(UUID uuid) {
        Log.i(TAG, "syncComplete: " + uuid.toString());
        Intent groupActivity = new Intent(UnlockActivity.this, GroupActivity.class);
        groupActivity.putExtra("uuid", uuid);
        startActivity(groupActivity);
    }

    private void submit() {
        Editable editable = mPasswordEditText.getText();
        if (this.isPalmUserRegistered()) {
            savePin(editable);
        } else {
            deletePin();
        }
        openVault(editable.toString());
    }

    private void savePin(Editable editable) {
        LocalPinRepository repo = new LocalPinRepository();
        try {
            repo.savePin(editable.toString(), this);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void deletePin() {
        LocalPinRepository repo = new LocalPinRepository();
        try {
            repo.deletePin();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void openVault(final String password) {
        SyncDialogFragment syncDialogFragment = new SyncDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("type", "read");
        args.putSerializable("password", password);

        syncDialogFragment.setArguments(args);
        syncDialogFragment.setSyncListener(this);
        syncDialogFragment.show(getFragmentManager(), "dialog");
    }

    private String getApplicationTitle() {
        PackageManager packageManager = getPackageManager();

        String name = getApplicationInfo().loadLabel(packageManager).toString();
        String version;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "?.?";
        }

        return name + " " + version;
    }
}
