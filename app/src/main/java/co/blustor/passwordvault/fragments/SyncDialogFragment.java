package co.blustor.passwordvault.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.UUID;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.database.VaultGroup;
import co.blustor.passwordvault.sync.SyncManager;
import co.blustor.passwordvault.utils.AlertUtils;

import static co.blustor.passwordvault.sync.SyncManager.*;

public class SyncDialogFragment extends DialogFragment {
    private static final String TAG = "SyncDialogFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_sync, container, false);

        setCancelable(false);

        final TextView statusTextView = (TextView) view.findViewById(R.id.textview_status);

        Bundle args = getArguments();

        final SyncType syncType = (SyncType) args.getSerializable("type");
        String password = args.getString("password");

        Promise<VaultGroup, SyncManager.SyncManagerException, SyncManager.SyncStatus> promise;
        if (syncType == SyncType.READ) {
            promise = getRoot(getActivity(), password);
        } else {
            promise = setRoot(getActivity(), password);
        }

        statusTextView.setText("Connecting");

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(promise).done(new DoneCallback<VaultGroup>() {
            @Override
            public void onDone(VaultGroup result) {
                dismiss();
                SyncInterface syncInterface = (SyncInterface) getActivity();
                syncInterface.syncComplete(result.getUUID());
            }
        }).fail(new FailCallback<SyncManager.SyncManagerException>() {
            @Override
            public void onFail(SyncManagerException result) {
                result.printStackTrace();
                AlertUtils.showError(getActivity(), result.getMessage());
                dismiss();
            }
        }).progress(new ProgressCallback<SyncManager.SyncStatus>() {
            @Override
            public void onProgress(SyncStatus progress) {
                Log.d(TAG, progress.name());
                if (progress == SyncStatus.SAVING) {
                    statusTextView.setText("Transferring");
                } else if (progress == SyncStatus.DECRYPTING) {
                    statusTextView.setText("Decrypting");
                } else if (progress == SyncStatus.ENCRYPTING) {
                    statusTextView.setText("Encrypting");
                }
            }
        });

        return view;
    }

    public interface SyncInterface {
        void syncComplete(UUID uuid);
    }
}
