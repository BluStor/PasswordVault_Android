package co.blustor.identity.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.MoreObjects;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jdeferred.Promise;

import java.util.UUID;

import co.blustor.identity.R;
import co.blustor.identity.gatekeeper.GKCard;
import co.blustor.identity.sync.SyncManager;
import co.blustor.identity.sync.SyncStatus;
import co.blustor.identity.utils.AlertUtils;
import co.blustor.identity.vault.VaultGroup;

import static co.blustor.identity.sync.SyncManager.getRoot;
import static co.blustor.identity.sync.SyncManager.setRoot;

public class SyncDialogFragment extends DialogFragment {
    private static final String TAG = "SyncDialogFragment";

    private TextView mStatusTextView;
    @Nullable
    private SyncListener mSyncListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_sync, container, false);

        setCancelable(false);

        mStatusTextView = view.findViewById(R.id.textview_status);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        Bundle args = getArguments();

        String type = MoreObjects.firstNonNull(args.getString("type"), "read");
        String password = MoreObjects.firstNonNull(args.getString("password"), "");

        Promise<VaultGroup, Exception, Void> promise;
        if (type.equals("read")) {
            promise = getRoot(getActivity(), password);
        } else {
            promise = setRoot(getActivity(), password);
        }

        promise.done(result -> {
            Log.i(TAG, "done");
            if (mSyncListener != null) {
                Log.i(TAG, "syncComplete");
                mSyncListener.syncComplete(result.getUUID());
            } else {
                Log.i(TAG, "no sync listener");
            }
        }).always((state, resolved, rejected) ->
                dismiss()
        ).fail(result -> {
            if (result instanceof SyncManager.SyncException) {
                SyncManager.SyncException syncException = (SyncManager.SyncException) result;
                Log.i(TAG, syncException.getError().toString());
                getActivity().runOnUiThread(() -> {
                    switch (syncException.getError()) {
                        case CARD_NOT_CHOSEN:
                            AlertUtils.showError(getActivity(), "Card not chosen.");
                            break;
                        case DATABASE_UNREADABLE:
                            AlertUtils.showError(getActivity(), "Invalid password.");
                            break;
                        case VAULT_EMPTY:
                            AlertUtils.showError(getActivity(), "Vault is empty.");
                            break;
                    }
                });
            } else if (result instanceof GKCard.CardException) {
                GKCard.CardException cardException = (GKCard.CardException) result;
                Log.i(TAG, cardException.getError().toString());
                getActivity().runOnUiThread(() -> {
                    switch (cardException.getError()) {
                        case ARGUMENT_INVALID:
                            AlertUtils.showError(getActivity(), "Invalid argument.");
                            break;
                        case BLUETOOTH_NOT_AVAILABLE:
                            AlertUtils.showError(getActivity(), "Bluetooth not available.");
                            break;
                        case BLUETOOTH_ADAPTER_NOT_ENABLED:
                            AlertUtils.showError(getActivity(), "Bluetooth not enabled.");
                            break;
                        case CARD_NOT_PAIRED:
                            AlertUtils.showError(getActivity(), "Card is not paired. Pair the device starting with 'ID-' in your phone's Bluetooth settings.");
                            break;
                        case CONNECTION_FAILED:
                            AlertUtils.showError(getActivity(), "Connection failed.");
                            break;
                        case CHARACTERISTIC_READ_FAILURE:
                            AlertUtils.showError(getActivity(), "Card read failure.");
                            break;
                        case CHARACTERISTIC_WRITE_FAILURE:
                            AlertUtils.showError(getActivity(), "Card write failure.");
                            break;
                        case FILE_NOT_FOUND:
                            AlertUtils.showError(getActivity(), "File not found.");
                            break;
                        case FILE_READ_FAILED:
                            AlertUtils.showError(getActivity(), "File read failed.");
                            break;
                        case FILE_WRITE_FAILED:
                            AlertUtils.showError(getActivity(), "File write failed.");
                            break;
                        case MAKE_COMMAND_DATA_FAILED:
                            AlertUtils.showError(getActivity(), "Card command failure.");
                            break;
                        case INVALID_CHECKSUM:
                            AlertUtils.showError(getActivity(), "Transfer error.");
                            break;
                        case INVALID_RESPONSE:
                            AlertUtils.showError(getActivity(), "Invalid response.");
                            break;
                    }
                });
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSyncStatus(SyncStatus syncStatus) {
        switch (syncStatus) {
            case SYNCED:
                mStatusTextView.setText(R.string.status_synced);
                break;
            case CONNECTING:
                mStatusTextView.setText(R.string.status_connecting);
                break;
            case DECRYPTING:
                mStatusTextView.setText(R.string.status_decrypting);
                break;
            case ENCRYPTING:
                mStatusTextView.setText(R.string.status_encrypting);
                break;
            case TRANSFERRING:
                mStatusTextView.setText(R.string.status_transferring);
                break;
            case FAILED:
                mStatusTextView.setText(R.string.status_failed);
                break;
        }
    }

    public void setSyncListener(SyncListener syncListener) {
        mSyncListener = syncListener;
    }

    public interface SyncListener {
        void syncComplete(UUID uuid);
    }
}
