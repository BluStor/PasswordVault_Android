package co.blustor.identity.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import co.blustor.identity.R
import co.blustor.identity.constants.Intents
import co.blustor.identity.fragments.SearchFragment
import co.blustor.identity.services.NotificationService
import co.blustor.identity.sync.SyncManager
import co.blustor.identity.utils.MyApplication
import co.blustor.identity.vault.Vault
import co.blustor.identity.vault.VaultEntry
import co.blustor.identity.vault.VaultGroup
import com.github.clans.fab.FloatingActionButton
import com.google.common.base.Joiner
import com.kennyc.bottomsheet.BottomSheet
import com.kennyc.bottomsheet.BottomSheetListener
import kotlinx.android.synthetic.main.activity_group.*
import java.util.*

class GroupActivity : LockingActivity() {

    private var group: VaultGroup? = null
    private val groupEntryAdapter = GroupEntryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        // Views

        val path = group?.path
        if (path != null) {
            if (path.isNotEmpty()) {
                val pathStr = Joiner.on("/").join(path)
                textViewPath.text = String.format(Locale.getDefault(), getString(R.string.path_in), pathStr)
            } else {
                textViewPath.visibility = View.GONE
            }
        }

        recyclerViewGroup.setHasFixedSize(true)

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerViewGroup.layoutManager = linearLayoutManager
        recyclerViewGroup.adapter = groupEntryAdapter

        val groupFloatingActionButton = FloatingActionButton(this)
        groupFloatingActionButton.buttonSize = FloatingActionButton.SIZE_MINI
        groupFloatingActionButton.labelText = "Group"
        groupFloatingActionButton.setColorNormalResId(R.color.colorPrimaryDark)
        groupFloatingActionButton.setColorPressedResId(R.color.colorPrimaryDark)
        groupFloatingActionButton.setImageResource(R.drawable.vaultgroup_white)
        groupFloatingActionButton.setOnClickListener {
            fam.close(false)
            val addGroupActivity = Intent(this, AddGroupActivity::class.java)
            addGroupActivity.putExtra("uuid", group?.uuid)
            startActivity(addGroupActivity)
        }

        val entryFloatingActionButton = FloatingActionButton(this)
        entryFloatingActionButton.buttonSize = FloatingActionButton.SIZE_MINI
        entryFloatingActionButton.labelText = "Entry"
        entryFloatingActionButton.setColorNormalResId(R.color.colorPrimaryDark)
        entryFloatingActionButton.setColorPressedResId(R.color.colorPrimaryDark)
        entryFloatingActionButton.setImageResource(R.drawable.vaultentry_white)
        entryFloatingActionButton.setOnClickListener {
            fam.close(false)
            val addEntryActivity = Intent(this, AddEntryActivity::class.java)
            addEntryActivity.putExtra("uuid", group?.uuid)
            startActivity(addEntryActivity)
        }

        fam.addMenuButton(groupFloatingActionButton)
        fam.addMenuButton(entryFloatingActionButton)
        fam.setClosedOnTouchOutside(true)

        (fragmentSearch as SearchFragment).hide()

        // Load group

        val intent = intent
        val uuid = intent.getSerializableExtra("uuid") as UUID

        group = Vault.instance.getGroupByUUID(uuid)

        // Start notification service if necessary

        if (group?.parentUUID == null) {
            val notificationService = Intent(this, NotificationService::class.java)
            startService(notificationService)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()

        menuInflater.inflate(R.menu.menu_group, menu)

        val menuItem = menu.findItem(R.id.action_search)
        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                (fragmentSearch as SearchFragment).show()
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                (fragmentSearch as SearchFragment).hide()
                return true
            }
        })

        val searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                (fragmentSearch as SearchFragment).search(newText)
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            val settingsActivity = Intent(this, SettingsActivity::class.java)
            startActivity(settingsActivity)
        } else if (id == R.id.action_about) {
            val aboutActivity = Intent(this, AboutActivity::class.java)
            startActivity(aboutActivity)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (group?.parentUUID == null) {
            val lockDatabase = Intent(Intents.lockDatabase)
            sendBroadcast(lockDatabase)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        groupEntryAdapter.updateData()
    }

    private inner class GroupEntryAdapter internal constructor() : RecyclerView.Adapter<GroupEntryAdapter.GroupEntryViewHolder>() {
        private val mGroups = ArrayList<VaultGroup>()
        private val mEntries = ArrayList<VaultEntry>()

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): GroupEntryAdapter.GroupEntryViewHolder {
            val view = if (viewType == 0) {
                LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
            } else {
                LayoutInflater.from(parent.context).inflate(R.layout.item_entry, parent, false)
            }
            return GroupEntryViewHolder(view)
        }

        override fun getItemViewType(position: Int): Int {
            return if (position < mGroups.size) {
                0
            } else {
                1
            }
        }

        override fun onBindViewHolder(
            holder: GroupEntryAdapter.GroupEntryViewHolder, position: Int
        ) {
            if (holder.itemViewType == 0) {
                val group = mGroups[position]

                holder.iconImageView.setImageResource(MyApplication.icons.get(group.iconId))
                holder.titleTextView.text = group.name
            } else {
                val entry = mEntries[position - mGroups.size]

                val iconId = group?.iconId ?: 49
                val drawable = ContextCompat.getDrawable(applicationContext, MyApplication.icons.get(iconId))

                holder.iconImageView.setImageDrawable(drawable)
                holder.titleTextView.text = entry.title
            }
        }

        override fun getItemCount(): Int {
            return mGroups.size + mEntries.size
        }

        internal fun updateData() {
            val groups = group?.groups ?: emptyList()
            val entries = group?.entries ?: emptyList()

            mGroups.clear()
            mGroups.addAll(groups)
            mEntries.clear()
            mEntries.addAll(entries)

            Collections.sort(mGroups, { leftGroup, rightGroup ->
                leftGroup.name.compareTo(rightGroup.name)
            })

            Collections.sort(mEntries, { leftEntry, rightEntry ->
                leftEntry.title.compareTo(rightEntry.title)
            })

            if (mGroups.size > 0 || mEntries.size > 0) {
                textViewEmpty.visibility = View.INVISIBLE
            } else {
                textViewEmpty.visibility = View.VISIBLE
            }

            title = group?.name

            notifyDataSetChanged()
        }

        inner class GroupEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

            val iconImageView: ImageView
            val titleTextView: TextView

            init {

                itemView.setOnClickListener(this)
                itemView.setOnLongClickListener(this)

                iconImageView = itemView.findViewById(R.id.imageViewIcon)
                titleTextView = itemView.findViewById(R.id.textview_title)
            }

            override fun onClick(view: View) {
                val position = adapterPosition
                if (position < mGroups.size) {
                    val group = mGroups[position]

                    val groupActivity = Intent(view.context, GroupActivity::class.java)
                    groupActivity.putExtra("uuid", group.uuid)

                    startActivity(groupActivity)
                } else {
                    val entry = mEntries[position - mGroups.size]

                    val editEntryActivity = Intent(view.context, EditEntryActivity::class.java)
                    editEntryActivity.putExtra("uuid", entry.uuid)

                    startActivity(editEntryActivity)
                }
            }

            override fun onLongClick(v: View): Boolean {
                val position = adapterPosition
                if (position < mGroups.size) {
                    val group = mGroups[position]
                    BottomSheet.Builder(this@GroupActivity).setTitle(group.name)
                        .setSheet(R.menu.menu_bottom_group)
                        .setListener(object : BottomSheetListener {
                            override fun onSheetShown(bottomSheet: BottomSheet) {

                            }

                            override fun onSheetItemSelected(
                                bottomSheet: BottomSheet, menuItem: MenuItem
                            ) {
                                val itemId = menuItem.itemId

                                if (itemId == R.id.action_delete) {
                                    this@GroupActivity.group?.removeGroup(group.uuid)
                                    updateData()

                                    SyncManager.setRoot(this@GroupActivity)
                                } else if (itemId == R.id.action_edit) {
                                    val editGroupActivity = Intent(this@GroupActivity, EditGroupActivity::class.java)
                                    editGroupActivity.putExtra("uuid", group.uuid)
                                    startActivity(editGroupActivity)
                                }
                            }

                            override fun onSheetDismissed(bottomSheet: BottomSheet, i: Int) {

                            }
                        }).show()
                } else {
                    val entry = mEntries[position - mGroups.size]
                    BottomSheet.Builder(this@GroupActivity).setTitle(entry.title)
                        .setSheet(R.menu.menu_bottom_entry)
                        .setListener(object : BottomSheetListener {
                            override fun onSheetShown(bottomSheet: BottomSheet) {

                            }

                            override fun onSheetItemSelected(
                                bottomSheet: BottomSheet, menuItem: MenuItem
                            ) {
                                val itemId = menuItem.itemId
                                if (itemId == R.id.action_delete) {
                                    group?.removeEntry(entry.uuid)
                                    updateData()

                                    SyncManager.setRoot(this@GroupActivity)
                                } else if (itemId == R.id.action_edit) {
                                    val editEntryActivity = Intent(this@GroupActivity, EditEntryActivity::class.java)
                                    editEntryActivity.putExtra("uuid", entry.uuid)
                                    startActivity(editEntryActivity)
                                }
                            }

                            override fun onSheetDismissed(bottomSheet: BottomSheet, i: Int) {

                            }
                        }).show()
                }

                return true
            }
        }
    }
}
