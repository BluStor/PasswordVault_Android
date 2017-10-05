package co.blustor.pwv.utils;

import android.app.AlertDialog;
import android.content.Context;

public class AlertUtils {
    public static void showError(Context context, String message) {
        showMessage(context, message);
    }

    private static void showMessage(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("Okay", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }
}
