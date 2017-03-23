package co.blustor.passwordvault.sync;

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
import co.blustor.passwordvault.utils.AlertUtils;

public class SyncDialogFragment extends DialogFragment {
    private static final String TAG = "SyncDialogFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_sync, container, false);

        setCancelable(false);

        final TextView statusTextView = (TextView) view.findViewById(R.id.textview_status);

        Bundle args = getArguments();

        final SyncManager.SyncType syncType = (SyncManager.SyncType) args.getSerializable("type");
        String password = args.getString("password");

        Promise<VaultGroup, Exception, SyncManager.SyncStatus> promise;
        if (syncType == SyncManager.SyncType.READ) {
            promise = SyncManager.getRoot(getActivity(), password);
        } else {
            promise = SyncManager.setRoot(getActivity(), password);
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
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                result.printStackTrace();
                AlertUtils.showError(getActivity(), result.getMessage());
                dismiss();
            }
        }).progress(new ProgressCallback<SyncManager.SyncStatus>() {
            @Override
            public void onProgress(SyncManager.SyncStatus progress) {
                Log.d(TAG, progress.name());
                if (progress == SyncManager.SyncStatus.TRANSFERRING) {
                    statusTextView.setText("Transferring");
                } else if (progress == SyncManager.SyncStatus.DECRYPTING) {
                    statusTextView.setText("Decrypting");
                } else if (progress == SyncManager.SyncStatus.ENCRYPTING) {
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
