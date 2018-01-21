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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
            setRoot(activity)
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
            Log.i(tag, it.toString())

            if (it is SyncManager.SyncException) {
                val message = when (it.error) {
                    SyncManager.SyncError.CARD_NOT_CHOSEN -> "Card not chosen."
                    SyncManager.SyncError.DATABASE_UNREADABLE -> "Invalid password."
                    SyncManager.SyncError.VAULT_EMPTY -> "Vault is empty."
                }

                activity.runOnUiThread {
                    AlertUtils.showError(activity, message)
                }
            } else if (it is GKCard.CardException) {
                val message = when (it.error) {
                    GKCard.CardError.ARGUMENT_INVALID -> "Invalid argument."
                    GKCard.CardError.BLUETOOTH_NOT_AVAILABLE -> "Bluetooth not available."
                    GKCard.CardError.BLUETOOTH_ADAPTER_NOT_ENABLED -> "Bluetooth not enabled."
                    GKCard.CardError.CARD_NOT_CONNECTED -> "Connection failed."
                    GKCard.CardError.CARD_NOT_PAIRED -> "Card is not paired. Pair your card in your phone's Bluetooth settings."
                    GKCard.CardError.CONNECTION_FAILED -> "Connection failed."
                    GKCard.CardError.CHARACTERISTIC_READ_FAILURE -> "Card read failure."
                    GKCard.CardError.CHARACTERISTIC_WRITE_FAILURE -> "Card write failure."
                    GKCard.CardError.FILE_NOT_FOUND -> "File not found."
                    GKCard.CardError.FILE_READ_FAILED -> "File read failed."
                    GKCard.CardError.FILE_WRITE_FAILED -> "File write failed."
                    GKCard.CardError.INVALID_CHECKSUM -> "Transfer error."
                    GKCard.CardError.INVALID_RESPONSE -> "Invalid response."
                    GKCard.CardError.MAKE_COMMAND_DATA_FAILED -> "Card command failure."
                    GKCard.CardError.OPERATION_TIMEOUT -> "Operation timed out."
                }

                activity.runOnUiThread {
                    AlertUtils.showError(activity, message)
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
        val text = when (syncStatus) {
            SyncStatus.SYNCED -> R.string.status_synced
            SyncStatus.CONNECTING -> R.string.status_connecting
            SyncStatus.DECRYPTING -> R.string.status_decrypting
            SyncStatus.ENCRYPTING -> R.string.status_encrypting
            SyncStatus.TRANSFERRING -> R.string.status_transferring
            SyncStatus.FAILED -> R.string.status_failed
        }

        textViewStatus.setText(text)
    }

    fun setSyncListener(syncListener: SyncListener) {
        this.syncListener = syncListener
    }

    interface SyncListener {
        fun syncComplete(uuid: UUID)
    }
}
