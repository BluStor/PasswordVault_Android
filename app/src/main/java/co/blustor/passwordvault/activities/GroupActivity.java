package co.blustor.passwordvault.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.comparators.VaultEntryComparator;
import co.blustor.passwordvault.comparators.VaultGroupComparator;
import co.blustor.passwordvault.constants.Intents;
import co.blustor.passwordvault.database.Vault;
import co.blustor.passwordvault.database.VaultEntry;
import co.blustor.passwordvault.database.VaultGroup;
import co.blustor.passwordvault.fragments.SearchFragment;
import co.blustor.passwordvault.fragments.SyncDialogFragment;
import co.blustor.passwordvault.services.NotificationService;
import co.blustor.passwordvault.sync.SyncManager;
import co.blustor.passwordvault.utils.MyApplication;

public class GroupActivity extends LockingActivity implements SyncDialogFragment.SyncInterface {
    private static final String TAG = "GroupActivity";
    private final ArrayList<String> mPath = new ArrayList<>();
    private VaultGroup mGroup = null;
    private GroupEntryAdapter mGroupEntryAdapter = new GroupEntryAdapter();
    private TextView mEmptyTextView = null;
    private SearchFragment mSearchFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // Load group

        Intent intent = getIntent();
        UUID uuid = (UUID) intent.getSerializableExtra("uuid");
        ArrayList<String> paths = intent.getStringArrayListExtra("path");

        Vault vault = Vault.getInstance();
        mGroup = vault.getGroupByUUID(uuid);

        mPath.addAll(paths);
        mPath.add(mGroup.getName());

        // Start notification service if necessary

        if (mGroup.getParentUUID() == null) {
            Intent notificationService = new Intent(this, NotificationService.class);
            startService(notificationService);
        }

        // Views

        mEmptyTextView = (TextView) findViewById(R.id.textview_empty);

        TextView pathTextView = (TextView) findViewById(R.id.textview_path);
        List<String> displayPath = getDisplayPath();
        if (displayPath.size() > 0) {
            String path = "in " + Joiner.on("/").join(getDisplayPath());
            pathTextView.setText(path);
        } else {
            pathTextView.setVisibility(View.GONE);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mGroupEntryAdapter);

        final FloatingActionMenu fam = (FloatingActionMenu) findViewById(R.id.fam);

        FloatingActionButton groupFloatingActionButton = new FloatingActionButton(this);
        groupFloatingActionButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        groupFloatingActionButton.setLabelText("Group");
        groupFloatingActionButton.setColorNormalResId(R.color.colorPrimaryDark);
        groupFloatingActionButton.setColorPressedResId(R.color.colorPrimaryDark);
        groupFloatingActionButton.setImageResource(R.drawable.vaultgroup_white);
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
        entryFloatingActionButton.setImageResource(R.drawable.vaultentry_white);
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

        // Fragments

        mSearchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_search);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        mSearchFragment.hide();

        getMenuInflater().inflate(R.menu.menu_group, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSearchFragment.show();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mSearchFragment.hide();
                return true;
            }
        });

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchFragment.search(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
        } else if (id == R.id.action_about) {
            Intent aboutActivity = new Intent(this, AboutActivity.class);
            startActivity(aboutActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mGroup.getParentUUID() == null) {
            Intent lockDatabase = new Intent(Intents.LOCK_DATABASE);
            sendBroadcast(lockDatabase);
        } else {
            super.onBackPressed();
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

    @Override
    public void syncComplete(UUID uuid) {
        finish();
    }

    private class GroupEntryAdapter extends RecyclerView.Adapter<GroupEntryAdapter.GroupEntryViewHolder> {
        private final List<VaultGroup> mGroups = new ArrayList<>();
        private final List<VaultEntry> mEntries = new ArrayList<>();

        GroupEntryAdapter() {
            updateData();
        }

        @Override
        public GroupEntryAdapter.GroupEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == 0) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry, parent, false);
            }
            return new GroupEntryViewHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position < mGroups.size()) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public void onBindViewHolder(GroupEntryAdapter.GroupEntryViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                VaultGroup group = mGroups.get(position);

                holder.subIconImageView.setImageResource(MyApplication.getIcons().get(group.getIconId()));
                holder.titleTextView.setText(group.getName());
            } else {
                VaultEntry entry = mEntries.get(position - mGroups.size());

                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), MyApplication.getIcons().get(entry.getIconId()));

                holder.iconImageView.setImageDrawable(drawable);
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

                Collections.sort(mGroups, new VaultGroupComparator());
                Collections.sort(mEntries, new VaultEntryComparator());

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

            final ImageView iconImageView;
            final ImageView subIconImageView;
            final TextView titleTextView;

            GroupEntryViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);

                iconImageView = (ImageView) itemView.findViewById(R.id.imageview_icon);
                subIconImageView = (ImageView) itemView.findViewById(R.id.imageview_subicon);
                titleTextView = (TextView) itemView.findViewById(R.id.textview_title);
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
                    editEntryActivity.putExtra("uuid", entry.getUUID());

                    ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(GroupActivity.this, iconImageView, "entry");
                    startActivity(editEntryActivity, activityOptions.toBundle());
                }
            }

            @Override
            public boolean onLongClick(View v) {
                int position = getAdapterPosition();
                if (position < mGroups.size()) {
                    final VaultGroup group = mGroups.get(position);
                    new BottomSheet.Builder(GroupActivity.this)
                            .title(group.getName())
                            .sheet(R.menu.menu_bottom_group)
                            .listener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "which = " + which);
                                    if (which == R.id.action_edit) {
                                        Intent editGroupActivity = new Intent(GroupActivity.this, EditGroupActivity.class);
                                        editGroupActivity.putExtra("uuid", group.getUUID());
                                        startActivity(editGroupActivity);
                                    } else if (which == R.id.action_delete) {
                                        mGroup.removeGroup(group.getUUID());
                                        updateData();

                                        Vault vault = Vault.getInstance();
                                        SyncManager.setRoot(GroupActivity.this, vault.getPassword());
                                    }
                                }
                            }).show();
                } else {
                    final VaultEntry entry = mEntries.get(position - mGroups.size());
                    new BottomSheet.Builder(GroupActivity.this)
                            .title(entry.getTitle())
                            .sheet(R.menu.menu_bottom_entry)
                            .listener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == R.id.action_delete) {
                                        mGroup.removeEntry(entry.getUUID());
                                        updateData();

                                        Vault vault = Vault.getInstance();
                                        SyncManager.setRoot(GroupActivity.this, vault.getPassword());
                                    }
                                }
                            }).show();
                }

                return true;
            }
        }
    }
}
