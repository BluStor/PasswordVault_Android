package co.blustor.identity.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.blustor.identity.R
import co.blustor.identity.sync.SyncStatus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SyncStatusFragment : Fragment() {

    private var statusTextView: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_syncstatus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusTextView = view.findViewById(R.id.textViewStatus)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSyncStatus(syncStatus: SyncStatus) {
        setSyncStatus(syncStatus)
    }

    private fun setSyncStatus(syncStatus: SyncStatus) {
        val context = context
        if (context != null) {
            when (syncStatus) {
                SyncStatus.SYNCED -> {
                    statusTextView?.setTextColor(ContextCompat.getColor(context, R.color.statusSynced))
                    statusTextView?.setText(R.string.status_synced)
                }
                SyncStatus.CONNECTING -> {
                    statusTextView?.setTextColor(ContextCompat.getColor(context, R.color.statusBusy))
                    statusTextView?.setText(R.string.status_connecting)
                }
                SyncStatus.DECRYPTING -> {
                    statusTextView?.setTextColor(ContextCompat.getColor(context, R.color.statusBusy))
                    statusTextView?.setText(R.string.status_decrypting)
                }
                SyncStatus.ENCRYPTING -> {
                    statusTextView?.setTextColor(ContextCompat.getColor(context, R.color.statusBusy))
                    statusTextView?.setText(R.string.status_encrypting)
                }
                SyncStatus.TRANSFERRING -> {
                    statusTextView?.setTextColor(ContextCompat.getColor(context, R.color.statusBusy))
                    statusTextView?.setText(R.string.status_transferring)
                }
                SyncStatus.FAILED -> {
                    statusTextView?.setTextColor(ContextCompat.getColor(context, R.color.statusFailed))
                    statusTextView?.setText(R.string.status_failed)
                }
            }
        }
    }
}
