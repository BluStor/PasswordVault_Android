package co.blustor.identity.activities

import android.app.AlertDialog
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

        Vault.instance.getGroupByUUID(uuid)?.let {
            group = it

            if (it.path.isNotEmpty()) {
                val pathStr = Joiner.on("/").join(it.path)
                textViewPath.text =  getString(R.string.path_in, pathStr)
                textViewPath.visibility = View.VISIBLE
            } else {
                textViewPath.visibility = View.GONE
            }
        }

        // Start notification service if necessary

        if (group?.parentUUID == null) {
            val notificationService = Intent(this, NotificationService::class.java)
            startService(notificationService)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            it.clear()

            menuInflater.inflate(R.menu.menu_group, it)

            val menuItem = it.findItem(R.id.action_search)
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
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_settings -> {
                val settingsActivity = Intent(this, SettingsActivity::class.java)
                startActivity(settingsActivity)
            }
            R.id.action_about -> {
                val aboutActivity = Intent(this, AboutActivity::class.java)
                startActivity(aboutActivity)
            }
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
        private val groups = mutableListOf<VaultGroup>()
        private val entries = mutableListOf<VaultEntry>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupEntryAdapter.GroupEntryViewHolder {
            val view = if (viewType == 0) {
                LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
            } else {
                LayoutInflater.from(parent.context).inflate(R.layout.item_entry, parent, false)
            }
            return GroupEntryViewHolder(view)
        }

        override fun getItemViewType(position: Int): Int {
            return if (position < groups.size) {
                0
            } else {
                1
            }
        }

        override fun onBindViewHolder(holder: GroupEntryAdapter.GroupEntryViewHolder, position: Int) {
            if (holder.itemViewType == 0) {
                val group = groups[position]

                holder.iconImageView.setImageResource(MyApplication.icons.get(group.iconId))
                holder.titleTextView.text = group.name
            } else {
                val entry = entries[position - groups.size]

                val drawable = ContextCompat.getDrawable(applicationContext, MyApplication.icons.get(entry.iconId))

                holder.iconImageView.setImageDrawable(drawable)
                holder.titleTextView.text = entry.title
            }
        }

        override fun getItemCount(): Int {
            return groups.size + entries.size
        }

        internal fun updateData() {
            groups.clear()
            entries.clear()

            group?.let {
                groups.addAll(it.groups)
                entries.addAll(it.entries)

                groups.sortWith(Comparator { leftGroup, rightGroup ->
                    leftGroup.name.compareTo(rightGroup.name)
                })

                entries.sortWith(Comparator { leftEntry, rightEntry ->
                    leftEntry.title.compareTo(rightEntry.title)
                })

                title = it.name
            }

            if (itemCount > 0) {
                textViewEmpty.visibility = View.GONE
            } else {
                textViewEmpty.visibility = View.VISIBLE
            }

            notifyDataSetChanged()
        }

        inner class GroupEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

            val iconImageView = itemView.findViewById(R.id.imageViewIcon) as ImageView
            val titleTextView = itemView.findViewById(R.id.textview_title) as TextView

            init {
                itemView.setOnClickListener(this)
                itemView.setOnLongClickListener(this)
            }


            override fun onClick(view: View?) {
                view?.let {
                    val position = adapterPosition
                    if (position < groups.size) {
                        val group = groups[position]

                        val groupActivity = Intent(it.context, GroupActivity::class.java)
                        groupActivity.putExtra("uuid", group.uuid)

                        startActivity(groupActivity)
                    } else {
                        val entry = entries[position - groups.size]

                        val editEntryActivity = Intent(it.context, EditEntryActivity::class.java)
                        editEntryActivity.putExtra("uuid", entry.uuid)

                        startActivity(editEntryActivity)
                    }
                }
            }

            override fun onLongClick(v: View?): Boolean {
                val position = adapterPosition
                if (position < groups.size) {
                    val group = groups[position]
                    BottomSheet.Builder(this@GroupActivity).setTitle(group.name)
                        .setSheet(R.menu.menu_bottom_group)
                        .setListener(object : BottomSheetListener {
                            override fun onSheetShown(bottomSheet: BottomSheet) {

                            }

                            override fun onSheetItemSelected(bottomSheet: BottomSheet, menuItem: MenuItem?) {
                                when (menuItem?.itemId) {
                                    R.id.action_delete -> {
                                        this@GroupActivity.group?.removeGroup(group.uuid)
                                        updateData()

                                        SyncManager.setRoot(this@GroupActivity)
                                    }
                                    R.id.action_edit -> {
                                        val editGroupActivity = Intent(this@GroupActivity, EditGroupActivity::class.java)
                                        editGroupActivity.putExtra("uuid", group.uuid)
                                        startActivity(editGroupActivity)
                                    }
                                }
                            }

                            override fun onSheetDismissed(bottomSheet: BottomSheet, i: Int) {

                            }
                        }).show()
                } else {
                    val entry = entries[position - groups.size]
                    BottomSheet.Builder(this@GroupActivity).setTitle(entry.title)
                        .setSheet(R.menu.menu_bottom_entry)
                        .setListener(object : BottomSheetListener {
                            override fun onSheetShown(bottomSheet: BottomSheet) {

                            }

                            override fun onSheetItemSelected(bottomSheet: BottomSheet, menuItem: MenuItem?) {
                                when (menuItem?.itemId) {
                                    R.id.action_delete -> {
                                        group?.removeEntry(entry.uuid)
                                        updateData()

                                        SyncManager.setRoot(this@GroupActivity)
                                    }
                                    R.id.action_edit -> {
                                        val editEntryActivity = Intent(this@GroupActivity, EditEntryActivity::class.java)
                                        editEntryActivity.putExtra("uuid", entry.uuid)
                                        startActivity(editEntryActivity)
                                    }
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
