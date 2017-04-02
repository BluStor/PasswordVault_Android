package co.blustor.passwordvault.utils;

import android.app.Application;
import android.content.Context;

import co.blustor.gatekeepersdk.devices.GKBluetoothCard;

public class MyApplication extends Application {
    private static final String DEVICE_NAME = "CYBERGATE";
    private static GKBluetoothCard sCard;

    public static GKBluetoothCard getCard(Context context) {
        if (sCard == null) {
            sCard = new GKBluetoothCard(DEVICE_NAME, context.getCacheDir());
        }

        return sCard;
    }
}
