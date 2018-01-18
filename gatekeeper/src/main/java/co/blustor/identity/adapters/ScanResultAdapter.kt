package co.blustor.identity.adapters

import android.bluetooth.le.ScanResult
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.blustor.identity.R
import java.util.*

class ScanResultAdapter : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    private val scanResults = ArrayList<ScanResult>()
    private var onAdapterItemClickListener: OnAdapterItemClickListener? = null
    private val onClickListener = View.OnClickListener { v ->
        if (onAdapterItemClickListener != null) {
            onAdapterItemClickListener?.onAdapterViewClick(v)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scanresult, parent, false)
        view.setOnClickListener(onClickListener)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanResultAdapter.ViewHolder, position: Int) {
        val scanResult = scanResults[position]

        holder.textViewName.text = scanResult.device.name
        holder.textViewStatus.text = scanResult.rssi.toString()
    }

    override fun getItemCount(): Int {
        return scanResults.size
    }

    fun addScanResult(scanResult: ScanResult) {
        val resultAddress = scanResult.device.address.toLowerCase()

        for (i in scanResults.indices) {
            if (scanResults[i].device.address.toLowerCase() == resultAddress) {
                scanResults[i] = scanResult
                return
            }
        }

        scanResults.add(scanResult)

        Collections.sort(scanResults) { lhs, rhs ->
            lhs.rssi - rhs.rssi
        }

        notifyDataSetChanged()
    }

    fun clearScanResults() {
        scanResults.clear()
        notifyDataSetChanged()
    }

    fun getItemAtPosition(childAdapterPosition: Int): ScanResult {
        return scanResults[childAdapterPosition]
    }

    fun setOnAdapterItemClickListener(onAdapterItemClickListener: OnAdapterItemClickListener) {
        this.onAdapterItemClickListener = onAdapterItemClickListener
    }

    interface OnAdapterItemClickListener {
        fun onAdapterViewClick(view: View)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textview_name)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
    }
}
