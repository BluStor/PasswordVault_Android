package co.blustor.identity.utils

import android.app.AlertDialog
import android.content.Context

object AlertUtils {

    fun showError(context: Context, message: String) {
        showMessage(context, message)
    }

    private fun showMessage(context: Context, message: String) {
        val builder = AlertDialog.Builder(context).setMessage(message)
            .setPositiveButton("Okay") { dialog, _ -> dialog.cancel() }

        builder.create().show()
    }
}
