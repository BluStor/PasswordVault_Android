package co.blustor.pwv.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import co.blustor.pwv.R;
import co.blustor.pwv.sync.SyncStatus;

public class SyncStatusFragment extends Fragment {
    private Context mContext = null;
    private TextView mStatusTextView = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_syncstatus, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getContext();
        mStatusTextView = view.findViewById(R.id.textview_status);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSyncStatus(SyncStatus syncStatus) {
        setSyncStatus(syncStatus);
    }

    private void setSyncStatus(SyncStatus syncStatus) {
        switch (syncStatus) {
            case SYNCED:
                mStatusTextView.setTextColor(ContextCompat.getColor(mContext, R.color.statusSynced));
                mStatusTextView.setText(R.string.status_synced);
                break;
            case CONNECTING:
                mStatusTextView.setTextColor(ContextCompat.getColor(mContext, R.color.statusBusy));
                mStatusTextView.setText(R.string.status_connecting);
                break;
            case DECRYPTING:
                mStatusTextView.setTextColor(ContextCompat.getColor(mContext, R.color.statusBusy));
                mStatusTextView.setText(R.string.status_decrypting);
                break;
            case ENCRYPTING:
                mStatusTextView.setTextColor(ContextCompat.getColor(mContext, R.color.statusBusy));
                mStatusTextView.setText(R.string.status_encrypting);
                break;
            case TRANSFERRING:
                mStatusTextView.setTextColor(ContextCompat.getColor(mContext, R.color.statusBusy));
                mStatusTextView.setText(R.string.status_transferring);
                break;
            case FAILED:
                mStatusTextView.setTextColor(ContextCompat.getColor(mContext, R.color.statusFailed));
                mStatusTextView.setText(R.string.status_failed);
                break;
        }
    }
}
