package co.blustor.identity.utils

import android.app.AlertDialog
import android.content.Context

object AlertUtils {

    private fun showMessage(context: Context, title: String, message: String) {
        val builder = AlertDialog.Builder(context).setMessage(message)
            .setTitle(title)
            .setPositiveButton("Okay") { dialog, _ ->
                dialog.cancel()
            }

        builder.create().show()
    }

    fun showError(context: Context, message: String) {
        showMessage(context, "Error", message)
    }
}
