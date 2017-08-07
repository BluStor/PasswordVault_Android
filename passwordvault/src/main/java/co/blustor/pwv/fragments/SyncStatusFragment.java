package co.blustor.pwv.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jdeferred.ProgressCallback;
import org.jdeferred.android.AndroidDeferredManager;

import co.blustor.pwv.R;
import co.blustor.pwv.sync.SyncManager;

public class SyncStatusFragment extends Fragment {
    private final AndroidDeferredManager mDeferredManager = new AndroidDeferredManager();
    private TextView mStatusTextView = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_syncstatus, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStatusTextView = view.findViewById(R.id.textview_status);

        setSyncStatus(SyncManager.getLastWriteStatus());

        mDeferredManager.when(SyncManager.getWriteStatusPromise()).progress(new ProgressCallback<SyncManager.SyncStatus>() {
            @Override
            public void onProgress(SyncManager.SyncStatus progress) {
                setSyncStatus(progress);
            }
        });
    }

    private void setSyncStatus(SyncManager.SyncStatus syncStatus) {
        Context context = getActivity().getApplicationContext();
        if (syncStatus == SyncManager.SyncStatus.ENCRYPTING) {
            mStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.statusEncrypting));
            mStatusTextView.setText(R.string.status_encrypting);
        } else if (syncStatus == SyncManager.SyncStatus.SAVING) {
            mStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.statusSaving));
            mStatusTextView.setText(R.string.status_saving);
        } else if (syncStatus == SyncManager.SyncStatus.FAILED) {
            mStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.statusFailed));
            mStatusTextView.setText(R.string.status_failed);
        } else if (syncStatus == SyncManager.SyncStatus.SYNCED) {
            mStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.statusSynced));
            mStatusTextView.setText(R.string.status_synced);
        }
    }
}
