package co.blustor.passwordvault.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.database.VaultEntry;
import co.blustor.passwordvault.database.VaultGroup;
import co.blustor.passwordvault.services.NotificationService;

public class GroupActivity extends AppCompatActivity {
    private static final String TAG = "GroupActivity";

    private VaultGroup mGroup = null;
    private GroupEntryAdapter mGroupEntryAdapter = null;
    private TextView mEmptyTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // Views

        mEmptyTextView = (TextView)findViewById(R.id.textview_empty);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_groupsentries);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        mGroupEntryAdapter = new GroupEntryAdapter();
        recyclerView.setAdapter(mGroupEntryAdapter);

        final FloatingActionMenu fam = (FloatingActionMenu)findViewById(R.id.fam);

        FloatingActionButton groupFloatingActionButton = new FloatingActionButton(this);
        groupFloatingActionButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        groupFloatingActionButton.setLabelText("Group");
        groupFloatingActionButton.setColorNormalResId(R.color.colorPrimaryDark);
        groupFloatingActionButton.setColorPressedResId(R.color.colorPrimaryDark);
        groupFloatingActionButton.setImageResource(R.drawable.fab_add);
        groupFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.close(false);
                Intent addGroupActivity = new Intent(GroupActivity.this, AddGroupActivity.class);
                addGroupActivity.putExtra("uuid", mGroup.getUUID());
                startActivity(addGroupActivity);
            }
        });

        final FloatingActionButton entryFloatingActionButton = new FloatingActionButton(this);
        entryFloatingActionButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        entryFloatingActionButton.setLabelText("Entry");
        entryFloatingActionButton.setColorNormalResId(R.color.colorPrimaryDark);
        entryFloatingActionButton.setColorPressedResId(R.color.colorPrimaryDark);
        entryFloatingActionButton.setImageResource(R.drawable.fab_add);
        entryFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.close(false);
                Intent addEntryActivity = new Intent(GroupActivity.this, AddEntryActivity.class);
                addEntryActivity.putExtra("uuid", mGroup.getUUID());
                startActivity(addEntryActivity);
            }
        });

        fam.addMenuButton(groupFloatingActionButton);
        fam.addMenuButton(entryFloatingActionButton);

        // Load

        UUID uuid = (UUID)getIntent().getSerializableExtra("uuid");

        try {
            Vault vault = Vault.getInstance(this);
            mGroup = vault.getGroupByUUID(uuid);
        } catch (Vault.GroupNotFoundException e) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Log.d(TAG, "Edit selected");
            Intent editGroupActivity = new Intent(this, EditGroupActivity.class);
            editGroupActivity.putExtra("uuid", mGroup.getUUID());

            startActivity(editGroupActivity);
        } else if (id == R.id.action_delete) {
            if (mGroup.getParentUUID() == null) {
                Toast.makeText(this, "Cannot delete root group.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Vault vault = Vault.getInstance(this);
                    vault.getGroupByUUID(mGroup.getParentUUID()).removeGroup(mGroup.getUUID());

                    Toast.makeText(this, "Group deleted.", Toast.LENGTH_SHORT).show();

                    finish();
                } catch (Vault.GroupNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (mGroup.getParentUUID() == null) {
            Intent notificationService = new Intent(this, NotificationService.class);
            stopService(notificationService);
        }
    }

    @Override
    protected void onResume() {
        mGroupEntryAdapter.updateData();
        mGroupEntryAdapter.notifyDataSetChanged();
        super.onResume();
    }

    private class GroupEntryAdapter extends RecyclerView.Adapter<GroupEntryAdapter.GroupEntryViewHolder> {

        private List<VaultGroup> mGroups = new ArrayList<>();
        private List<VaultEntry> mEntries = new ArrayList<>();

        GroupEntryAdapter() {
            updateData();
        }

        @Override
        public GroupEntryAdapter.GroupEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_groupentry, parent, false);
            return new GroupEntryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(GroupEntryAdapter.GroupEntryViewHolder holder, int position) {
            if (position < mGroups.size()) {
                holder.iconImageView.setImageResource(R.drawable.group);

                VaultGroup group = mGroups.get(position);
                holder.titleTextView.setText(group.getName());
            } else {
                holder.iconImageView.setImageResource(R.drawable.entry);

                VaultEntry entry = mEntries.get(position - mGroups.size());
                holder.titleTextView.setText(entry.getTitle());
            }
        }

        @Override
        public int getItemCount() {
            return mGroups.size() + mEntries.size();
        }

        void updateData() {
            if (mGroup != null) {
                mGroups.clear();
                mGroups.addAll(mGroup.getGroups());
                mEntries.clear();
                mEntries.addAll(mGroup.getEntries());

                if (mGroups.size() > 0 || mEntries.size() > 0) {
                    mEmptyTextView.setVisibility(View.INVISIBLE);
                } else {
                    mEmptyTextView.setVisibility(View.VISIBLE);
                }

                setTitle(mGroup.getName());

                notifyDataSetChanged();
            }
        }

        class GroupEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

            ImageView iconImageView;
            TextView titleTextView;

            GroupEntryViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);

                iconImageView = (ImageView)itemView.findViewById(R.id.imageview_icon);
                titleTextView = (TextView)itemView.findViewById(R.id.textview_title);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (position < mGroups.size()) {
                    VaultGroup group = mGroups.get(position);

                    Intent groupActivity = new Intent(GroupActivity.this, GroupActivity.class);
                    groupActivity.putExtra("uuid", group.getUUID());

                    startActivity(groupActivity);
                } else {
                    VaultEntry entry = mEntries.get(position - mGroups.size());

                    Intent editEntryActivity = new Intent(GroupActivity.this, EditEntryActivity.class);
                    editEntryActivity.putExtra("groupUUID", mGroup.getUUID());
                    editEntryActivity.putExtra("uuid", entry.getUUID());

                    startActivity(editEntryActivity);
                }
            }

            @Override
            public boolean onLongClick(View v) {
                int position = getAdapterPosition();
                if (position < mGroups.size()) {
                    VaultGroup group = mGroups.get(position);

                    Intent editGroupActivity = new Intent(GroupActivity.this, EditGroupActivity.class);
                    editGroupActivity.putExtra("uuid", group.getUUID());

                    startActivity(editGroupActivity);
                }

                return true;
            }
        }
    }
}
