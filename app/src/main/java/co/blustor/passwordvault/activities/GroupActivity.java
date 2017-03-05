package co.blustor.passwordvault.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
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
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.database.VaultEntry;
import co.blustor.passwordvault.database.VaultGroup;
import co.blustor.passwordvault.extensions.LockingActivity;
import co.blustor.passwordvault.services.NotificationService;

public class GroupActivity extends LockingActivity {
    private static final String TAG = "GroupActivity";

    private VaultGroup mGroup = null;
    private GroupEntryAdapter mGroupEntryAdapter = null;
    private TextView mEmptyTextView = null;
    private final ArrayList<String> mPath = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // Load

        Intent intent = getIntent();
        UUID uuid = (UUID)intent.getSerializableExtra("uuid");

        try {
            Vault vault = Vault.getInstance(this);
            mGroup = vault.getGroupByUUID(uuid);
        } catch (Vault.GroupNotFoundException e) {
            finish();
        }

        mPath.addAll(intent.getStringArrayListExtra("path"));
        mPath.add(mGroup.getName());

        // Views

        mEmptyTextView = (TextView)findViewById(R.id.textview_empty);

        TextView pathTextView = (TextView)findViewById(R.id.textview_path);
        List<String> displayPath = getDisplayPath();
        if (displayPath.size() > 0) {
            String path = "in " + Joiner.on("/").join(getDisplayPath());
            pathTextView.setText(path);
        } else {
            pathTextView.setVisibility(View.GONE);
        }

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
        groupFloatingActionButton.setImageResource(R.drawable.group_white);
        groupFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.close(false);
                Intent addGroupActivity = new Intent(v.getContext(), AddGroupActivity.class);
                addGroupActivity.putExtra("uuid", mGroup.getUUID());
                startActivity(addGroupActivity);
            }
        });

        final FloatingActionButton entryFloatingActionButton = new FloatingActionButton(this);
        entryFloatingActionButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        entryFloatingActionButton.setLabelText("Entry");
        entryFloatingActionButton.setColorNormalResId(R.color.colorPrimaryDark);
        entryFloatingActionButton.setColorPressedResId(R.color.colorPrimaryDark);
        entryFloatingActionButton.setImageResource(R.drawable.entry_white);
        entryFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.close(false);
                Intent addEntryActivity = new Intent(v.getContext(), AddEntryActivity.class);
                addEntryActivity.putExtra("uuid", mGroup.getUUID());
                startActivity(addEntryActivity);
            }
        });

        fam.addMenuButton(groupFloatingActionButton);
        fam.addMenuButton(entryFloatingActionButton);
        fam.setClosedOnTouchOutside(true);
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
            editGroupActivity.putStringArrayListExtra("path", mPath);

            startActivity(editGroupActivity);
        } else if (id == R.id.action_delete) {
            final Context context = this;

            if (mGroup.getParentUUID() == null) {
                Toast.makeText(this, "Cannot delete root group.", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to delete this group?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Vault vault = Vault.getInstance(getParent());
                                    vault.getGroupByUUID(mGroup.getParentUUID()).removeGroup(mGroup.getUUID());

                                    Toast.makeText(context, "Group deleted.", Toast.LENGTH_SHORT).show();

                                    finish();
                                } catch (Vault.GroupNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show();
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
        super.onResume();

        mGroupEntryAdapter.updateData();
    }

    private List<String> getDisplayPath() {
        if (mPath.size() > 1) {
            return mPath.subList(0, mPath.size() - 1);
        } else {
            return new ArrayList<>();
        }
    }

    private class GroupEntryAdapter extends RecyclerView.Adapter<GroupEntryAdapter.GroupEntryViewHolder> {

        private final List<VaultGroup> mGroups = new ArrayList<>();
        private final List<VaultEntry> mEntries = new ArrayList<>();

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
                holder.iconImageView.setImageResource(R.drawable.group_orange);

                VaultGroup group = mGroups.get(position);
                holder.titleTextView.setText(group.getName());
            } else {
                holder.iconImageView.setImageResource(R.drawable.entry_gray);

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

        class GroupEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            final ImageView iconImageView;
            final TextView titleTextView;

            GroupEntryViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                iconImageView = (ImageView)itemView.findViewById(R.id.imageview_icon);
                titleTextView = (TextView)itemView.findViewById(R.id.textview_title);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (position < mGroups.size()) {
                    VaultGroup group = mGroups.get(position);

                    Intent groupActivity = new Intent(v.getContext(), GroupActivity.class);
                    groupActivity.putExtra("uuid", group.getUUID());
                    groupActivity.putExtra("path", mPath);

                    startActivity(groupActivity);
                } else {
                    VaultEntry entry = mEntries.get(position - mGroups.size());

                    Intent editEntryActivity = new Intent(v.getContext(), EditEntryActivity.class);
                    editEntryActivity.putExtra("groupUUID", mGroup.getUUID());
                    editEntryActivity.putExtra("uuid", entry.getUUID());

                    ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(GroupActivity.this, iconImageView, "entry");
                    startActivity(editEntryActivity, activityOptions.toBundle());
                }
            }
        }
    }
}
