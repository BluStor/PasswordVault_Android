package co.blustor.identity.fragments

import android.app.DialogFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.blustor.identity.R
import co.blustor.identity.gatekeeper.GKCard
import co.blustor.identity.sync.SyncManager
import co.blustor.identity.sync.SyncManager.getRoot
import co.blustor.identity.sync.SyncManager.setRoot
import co.blustor.identity.sync.SyncStatus
import co.blustor.identity.utils.AlertUtils
import com.google.common.base.MoreObjects
import kotlinx.android.synthetic.main.dialogfragment_sync.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class SyncDialogFragment : DialogFragment() {

    private var syncListener: SyncListener? = null

    override fun onCreateView(
        inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        isCancelable = false

        return if (inflater == null) {
            View.inflate(context, R.layout.dialogfragment_sync, container)
        } else {
            inflater.inflate(R.layout.dialogfragment_sync, container, false)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

        val args = arguments

        val type = MoreObjects.firstNonNull(args.getString("type"), "read")
        val password = MoreObjects.firstNonNull(args.getString("password"), "")

        val promise = if (type == "read") {
            getRoot(activity, password)
        } else {
            setRoot(activity, password)
        }

        promise.done({
            Log.i(tag, "done")
            if (syncListener != null) {
                Log.i(tag, "syncComplete")
                syncListener?.syncComplete(it.uuid)
            } else {
                Log.i(tag, "no sync listener")
            }
        }).always({ _, _, _ ->
            dismiss()
        }).fail({
            if (it is SyncManager.SyncException) {
                Log.i(tag, it.error.toString())
                activity.runOnUiThread {
                    when (it.error) {
                        SyncManager.SyncError.CARD_NOT_CHOSEN -> AlertUtils.showError(
                            activity, "Card not chosen."
                        )
                        SyncManager.SyncError.DATABASE_UNREADABLE -> AlertUtils.showError(
                            activity, "Invalid password."
                        )
                        SyncManager.SyncError.VAULT_EMPTY -> AlertUtils.showError(
                            activity, "Vault is empty."
                        )
                    }
                }
            } else if (it is GKCard.CardException) {
                Log.i(tag, it.error.toString())
                activity.runOnUiThread {
                    when (it.error) {
                        GKCard.CardError.ARGUMENT_INVALID -> AlertUtils.showError(
                            activity, "Invalid argument."
                        )
                        GKCard.CardError.BLUETOOTH_NOT_AVAILABLE -> AlertUtils.showError(
                            activity, "Bluetooth not available."
                        )
                        GKCard.CardError.BLUETOOTH_ADAPTER_NOT_ENABLED -> AlertUtils.showError(
                            activity, "Bluetooth not enabled."
                        )
                        GKCard.CardError.CARD_NOT_PAIRED -> AlertUtils.showError(
                            activity, "Card is not paired. Pair the device starting with 'ID-' in your phone's Bluetooth settings."
                        )
                        GKCard.CardError.CONNECTION_FAILED -> AlertUtils.showError(
                            activity, "Connection failed."
                        )
                        GKCard.CardError.CHARACTERISTIC_READ_FAILURE -> AlertUtils.showError(
                            activity, "Card read failure."
                        )
                        GKCard.CardError.CHARACTERISTIC_WRITE_FAILURE -> AlertUtils.showError(
                            activity, "Card write failure."
                        )
                        GKCard.CardError.FILE_NOT_FOUND -> AlertUtils.showError(
                            activity, "File not found."
                        )
                        GKCard.CardError.FILE_READ_FAILED -> AlertUtils.showError(
                            activity, "File read failed."
                        )
                        GKCard.CardError.FILE_WRITE_FAILED -> AlertUtils.showError(
                            activity, "File write failed."
                        )
                        GKCard.CardError.MAKE_COMMAND_DATA_FAILED -> AlertUtils.showError(
                            activity, "Card command failure."
                        )
                        GKCard.CardError.INVALID_CHECKSUM -> AlertUtils.showError(
                            activity, "Transfer error."
                        )
                        GKCard.CardError.INVALID_RESPONSE -> AlertUtils.showError(
                            activity, "Invalid response."
                        )
                    }
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSyncStatus(syncStatus: SyncStatus) {
        when (syncStatus) {
            SyncStatus.SYNCED -> textViewStatus.setText(R.string.status_synced)
            SyncStatus.CONNECTING -> textViewStatus.setText(R.string.status_connecting)
            SyncStatus.DECRYPTING -> textViewStatus.setText(R.string.status_decrypting)
            SyncStatus.ENCRYPTING -> textViewStatus.setText(R.string.status_encrypting)
            SyncStatus.TRANSFERRING -> textViewStatus.setText(R.string.status_transferring)
            SyncStatus.FAILED -> textViewStatus.setText(R.string.status_failed)
        }
    }

    fun setSyncListener(syncListener: SyncListener) {
        this.syncListener = syncListener
    }

    interface SyncListener {
        fun syncComplete(uuid: UUID)
    }
}
