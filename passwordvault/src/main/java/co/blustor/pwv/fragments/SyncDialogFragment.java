package co.blustor.pwv.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.MoreObjects;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;

import java.util.UUID;

import co.blustor.pwv.R;
import co.blustor.pwv.database.VaultGroup;
import co.blustor.pwv.sync.SyncManager;
import co.blustor.pwv.utils.AlertUtils;

import static co.blustor.pwv.sync.SyncManager.SyncManagerException;
import static co.blustor.pwv.sync.SyncManager.SyncStatus;
import static co.blustor.pwv.sync.SyncManager.SyncType;
import static co.blustor.pwv.sync.SyncManager.getRoot;
import static co.blustor.pwv.sync.SyncManager.setRoot;

public class SyncDialogFragment extends DialogFragment {
    private static final String TAG = "SyncDialogFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_sync, container, false);

        setCancelable(false);

        final TextView statusTextView = view.findViewById(R.id.textview_status);

        Bundle args = getArguments();

        final SyncType syncType = (SyncType) args.getSerializable("type");
        String password = MoreObjects.firstNonNull(args.getString("password"), "");

        Promise<VaultGroup, SyncManager.SyncManagerException, SyncManager.SyncStatus> promise;
        if (syncType == SyncType.READ) {
            promise = getRoot(getActivity(), password);
        } else {
            promise = setRoot(getActivity(), password);
        }

        statusTextView.setText(R.string.status_connecting);

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(promise).done(new DoneCallback<VaultGroup>() {
            @Override
            public void onDone(@NonNull VaultGroup result) {
                dismiss();
                SyncInterface syncInterface = (SyncInterface) getActivity();
                syncInterface.syncComplete(result.getUUID());
            }
        }).fail(new FailCallback<SyncManager.SyncManagerException>() {
            @Override
            public void onFail(@NonNull SyncManagerException result) {
                result.printStackTrace();
                AlertUtils.showError(getActivity(), result.getMessage());
                dismiss();
            }
        }).progress(new ProgressCallback<SyncManager.SyncStatus>() {
            @Override
            public void onProgress(@NonNull SyncStatus progress) {
                Log.d(TAG, progress.name());
                if (progress == SyncStatus.SAVING) {
                    statusTextView.setText(R.string.status_transferring);
                } else if (progress == SyncStatus.DECRYPTING) {
                    statusTextView.setText(R.string.status_decrypting);
                } else if (progress == SyncStatus.ENCRYPTING) {
                    statusTextView.setText(R.string.status_encrypting);
                }
            }
        });

        return view;
    }

    public interface SyncInterface {
        void syncComplete(UUID uuid);
    }
}
