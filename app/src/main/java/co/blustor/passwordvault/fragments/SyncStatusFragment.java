package co.blustor.passwordvault.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
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
    TextView mStatusTextView = null;
    AndroidDeferredManager mDeferredManager = new AndroidDeferredManager();

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
        if (syncStatus == SyncManager.SyncStatus.ENCRYPTING) {
            mStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.syncStatusEncrypting));
            mStatusTextView.setText("Encrypting");
        } else if (syncStatus == SyncManager.SyncStatus.TRANSFERRING) {
            mStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.syncStatusTransferring));
            mStatusTextView.setText("Transferring");
        } else if (syncStatus == SyncManager.SyncStatus.FAILED) {
            mStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.syncStatusFailed));
            mStatusTextView.setText("Failed");
        } else if (syncStatus == SyncManager.SyncStatus.SYNCED) {
            mStatusTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.syncStatusSynced));
            mStatusTextView.setText("Synced");
        }
    }
}
