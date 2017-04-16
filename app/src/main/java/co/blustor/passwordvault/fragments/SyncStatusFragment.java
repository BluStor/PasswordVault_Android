package co.blustor.passwordvault.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jdeferred.ProgressCallback;
import org.jdeferred.android.AndroidDeferredManager;

import co.blustor.passwordvault.R;
import co.blustor.passwordvault.sync.SyncManager;

public class SyncStatusFragment extends Fragment {
    private final AndroidDeferredManager mDeferredManager = new AndroidDeferredManager();
    private TextView mStatusTextView = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_syncstatus, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStatusTextView = (TextView) view.findViewById(R.id.textview_status);

        setSyncStatus(SyncManager.getLastWriteStatus());

        mDeferredManager.when(SyncManager.getWriteStatusPromise()).progress(new ProgressCallback<SyncManager.SyncStatus>() {
            @Override
            public void onProgress(SyncManager.SyncStatus progress) {
                setSyncStatus(progress);
            }
        });
    }

    private void setSyncStatus(SyncManager.SyncStatus syncStatus) {
        Context context = getContext();
        if (context != null) {
            if (syncStatus == SyncManager.SyncStatus.ENCRYPTING) {
                mStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.statusEncrypting));
                mStatusTextView.setText(R.string.status_encrypting);
            } else if (syncStatus == SyncManager.SyncStatus.SAVING) {
                mStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.statusSaving));
                mStatusTextView.setText(R.string.status_saving);
            } else if (syncStatus == SyncManager.SyncStatus.FAILED) {
                mStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.statusFailed));
                mStatusTextView.setText(R.string.status_failed);
            } else if (syncStatus == SyncManager.SyncStatus.SYNCED) {
                mStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.statusSynced));
                mStatusTextView.setText(R.string.status_synced);
            }
        }
    }
}
