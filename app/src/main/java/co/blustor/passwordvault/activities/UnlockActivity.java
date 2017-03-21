package co.blustor.passwordvault.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.clans.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.sync.SyncDialogFragment;
import co.blustor.passwordvault.sync.SyncManager;

public class UnlockActivity extends AppCompatActivity implements SyncDialogFragment.SyncInterface {
    private static final String TAG = "UnlockActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        // Views

        final EditText passwordTextView = (EditText) findViewById(R.id.edittext_password);

        Button newButton = (Button) findViewById(R.id.button_new);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent initializeActivity = new Intent(v.getContext(), InitializeActivity.class);
                startActivity(initializeActivity);
            }
        });

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = passwordTextView.getText();
                openVault(editable.toString());
                editable.clear();
            }
        });
    }

    void openVault(final String password) {
        SyncDialogFragment syncDialogFragment = new SyncDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("type", SyncManager.SyncType.READ);
        args.putSerializable("password", password);

        syncDialogFragment.setArguments(args);
        syncDialogFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void syncComplete(UUID uuid) {
        Intent groupActivity = new Intent(UnlockActivity.this, GroupActivity.class);
        groupActivity.putExtra("uuid", uuid);
        groupActivity.putExtra("path", new ArrayList<String>());
        startActivity(groupActivity);
    }
}
