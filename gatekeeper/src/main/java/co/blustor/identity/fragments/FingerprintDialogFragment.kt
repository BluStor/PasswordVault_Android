package co.blustor.identity.fragments

import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.os.CancellationSignal
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.blustor.identity.R

class FingerprintDialogFragment: DialogFragment() {

    val cancellationSignal = CancellationSignal()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return if (inflater == null) {
            View.inflate(context, R.layout.dialogfragment_fingerprint, container)
        } else {
            inflater.inflate(R.layout.dialogfragment_fingerprint, container, false)
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        cancellationSignal.cancel()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        cancellationSignal.cancel()
    }
}