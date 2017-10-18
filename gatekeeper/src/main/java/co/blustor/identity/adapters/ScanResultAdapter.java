package co.blustor.identity.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.polidea.rxandroidble.scan.ScanResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import co.blustor.identity.R;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ViewHolder> {

    private final Comparator<ScanResult> SORTING_COMPARATOR = (lhs, rhs) -> {
        return lhs.getRssi() - rhs.getRssi();
    };
    private List<ScanResult> scanResults = new ArrayList<>();
    private OnAdapterItemClickListener onAdapterItemClickListener;
    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onAdapterItemClickListener != null) {
                onAdapterItemClickListener.onAdapterViewClick(v);
            }
        }
    };

    @Override
    public ScanResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scanresult, parent, false);
        itemView.setOnClickListener(onClickListener);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ScanResultAdapter.ViewHolder holder, int position) {
        final ScanResult scanResult = scanResults.get(position);

        holder.mNameTextView.setText(scanResult.getBleDevice().getName());
        holder.mStatusTextView.setText(String.valueOf(scanResult.getRssi()));
    }

    @Override
    public int getItemCount() {
        return scanResults.size();
    }

    public void addScanResult(ScanResult scanResult) {
        scanResults.add(scanResult);

        for (ScanResult result : scanResults) {
            if (result.getBleDevice().getMacAddress().equals(scanResult.getBleDevice().getMacAddress())) {
                return;
            }
        }

        scanResults.add(scanResult);
        Collections.sort(scanResults, SORTING_COMPARATOR);
        notifyDataSetChanged();
    }

    public void clearScanResults() {
        scanResults.clear();
        notifyDataSetChanged();
    }

    public ScanResult getItemAtPosition(int childAdapterPosition) {
        return scanResults.get(childAdapterPosition);
    }

    public void setOnAdapterItemClickListener(OnAdapterItemClickListener onAdapterItemClickListener) {
        this.onAdapterItemClickListener = onAdapterItemClickListener;
    }

    public interface OnAdapterItemClickListener {
        void onAdapterViewClick(View view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView mNameTextView;
        final TextView mStatusTextView;

        ViewHolder(View itemView) {
            super(itemView);

            mNameTextView = itemView.findViewById(R.id.textview_name);
            mStatusTextView = itemView.findViewById(R.id.textview_status);
        }
    }
}