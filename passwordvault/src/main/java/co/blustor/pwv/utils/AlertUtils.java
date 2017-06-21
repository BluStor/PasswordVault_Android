package co.blustor.pwv.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

public class AlertUtils {
    public static void showError(Context context, String message) {
        showMessage(context, message);
    }

    private static void showMessage(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }
}
