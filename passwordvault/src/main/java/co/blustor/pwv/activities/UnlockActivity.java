package co.blustor.pwv.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.github.clans.fab.FloatingActionButton;

import java.util.UUID;

import co.blustor.pwv.R;
import co.blustor.pwv.fragments.SyncDialogFragment;

public class UnlockActivity extends AppCompatActivity implements SyncDialogFragment.SyncListener {

    private static final String TAG = "UnlockActivity";
    private EditText mPasswordEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        setTitle(getApplicationTitle());

        // Views

        mPasswordEditText = findViewById(R.id.edittext_password);

        mPasswordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> submit());
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
            finish();
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
        openVault(editable.toString());
        editable.clear();
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
