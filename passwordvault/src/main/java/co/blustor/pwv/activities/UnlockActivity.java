package co.blustor.pwv.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;

import java.util.UUID;

import co.blustor.pwv.R;
import co.blustor.pwv.fragments.SyncDialogFragment;
import co.blustor.pwv.sync.SyncManager;

public class UnlockActivity extends AppCompatActivity implements SyncDialogFragment.SyncInterface {

    private EditText mPasswordEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        setTitle(getApplicationTitle());

        // Views

        mPasswordEditText = (EditText) findViewById(R.id.edittext_password);

        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submit();
                    return true;
                }
                return false;
            }
        });

        Button newButton = (Button) findViewById(R.id.button_new);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createActivity = new Intent(getApplicationContext(), CreateActivity.class);
                startActivity(createActivity);
            }
        });

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_unlock, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent aboutActivity = new Intent(this, AboutActivity.class);
            startActivity(aboutActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    private void submit() {
        Editable editable = mPasswordEditText.getText();
        openVault(editable.toString());
        editable.clear();
    }

    private void openVault(final String password) {
        SyncDialogFragment syncDialogFragment = new SyncDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("type", SyncManager.SyncType.READ);
        args.putSerializable("password", password);

        syncDialogFragment.setArguments(args);
        syncDialogFragment.show(getFragmentManager(), "dialog");
    }

    @NonNull
    private String getApplicationTitle() {
        PackageManager packageManager = getPackageManager();

        String name = getApplicationInfo().loadLabel(packageManager).toString();
        String version;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName + "." + packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            version = "?.?";
        }

        return name + " " + version;
    }

    @Override
    public void syncComplete(UUID uuid) {
        Intent groupActivity = new Intent(UnlockActivity.this, GroupActivity.class);
        groupActivity.putExtra("uuid", uuid);
        startActivity(groupActivity);
    }
}
