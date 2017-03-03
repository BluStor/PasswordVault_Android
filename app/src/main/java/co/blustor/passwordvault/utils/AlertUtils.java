package co.blustor.passwordvault.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertUtils {
    private static String TAG = "AlertUtils";

    public static void showError(Context context, String message) {
        showMessage(context, "Error", message);
    }

    public static void showMessage(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        if (title != null) {
            builder.setTitle(title);
        }

        builder.create().show();
    }
}
