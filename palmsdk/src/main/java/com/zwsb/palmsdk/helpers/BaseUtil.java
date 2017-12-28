package com.zwsb.palmsdk.helpers;

import android.content.Context;
import android.graphics.Point;
import android.os.Environment;
import android.view.Display;
import android.view.WindowManager;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseUtil {

    public static String CURRENT_PALM_PATH = "";

    public static String LEFT_PALM_PATH = "LEFT_PALM_USER_PATH.bin";

    public static String RIGHT_PALM_PATH = "RIGHT_PALM_USER_PATH.bin";

    public static int screenWidth = 0;
    public static int screenHeight = 0;

    public static float decreaseCoefficient = 0.125f;

    public static boolean cameraFlipped = false;

    /**
     * Constant.RESOLUTION_RATIO_HEIGHT
     */
    public static float matrixHeight;

    /**
     * Constant.RESOLUTION_RATIO_WIDTH
     */
    public static float matrixWidth;

    /**
     * min of Constant.RESOLUTION_RATIO_HEIGHT and Constant.RESOLUTION_RATIO_WIDTH
     */
    public static float matrixMin;

    public static String UER_GESTURE_IMAGE_PATH = getUerGestureImgPath();

    public static String USER_GESTURE_PATH = "";

    public static String USER_GESTURE_INVALIDATE_PATH = getUserGestureInvalidatePath();

    public static void initScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point outSize = new  Point();
        display.getSize(outSize);
        screenWidth = outSize.x;
        screenHeight = outSize.y;
        matrixWidth = (float) (1.0 * screenWidth / Constant.RESOLUTION_RATIO_WIDTH);
        matrixHeight = (float) (1.0 * screenHeight / Constant.RESOLUTION_RATIO_HEIGHT);
        matrixMin = matrixHeight > matrixWidth ? matrixWidth : matrixHeight;
    }

    public static String getUserGesturePath() {
        String path = getRootPath();
        if (path != null) {
            path = getRootPath() + "/palmidsdk/gesture/";
        } else {
            path = "/data/data/com.zwsb.palmsdk/zwsb_r/gesture/";
        }
        mkdirsIfNotExists(path);
        return path;
    }

    public static String getUerGestureImgPath() {
        String path = getRootPath();
        if (path != null) {
            path = getRootPath() + "/palmidsdk/gesture_img/";
        } else {
            path = "/data/data/com.zwsb.palmsdk/zwsb_r/gesture_img/";
        }
        mkdirsIfNotExists(path);
        return path;
    }

    public static String getUserGestureInvalidatePath() {
        String path = getRootPath();
        if (path != null) {
            path = getRootPath() + "/palmidsdk/gesture_invalidate/";
        } else {
            path = "/data/data/com.zwsb.palmsdk/zwsb_r/gesture_invalidate/";
        }
        mkdirsIfNotExists(path);
        return path;
    }

    public static String getRootPath() {
        String path = null;
        boolean sdcardOK = false;
        sdcardOK = existSDCard();
        if (sdcardOK) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else if (isStorageOK("/storage/sdcard0")) {
            path = "/storage/sdcard0";
        } else if (isStorageOK("/storage/sdcard1")) {
            path = "/storage/sdcard1";
        }
        return path;
    }

    public static boolean existSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isStorageOK(String path) {
        boolean ok = false;
        File file = new  File(path + "/temp.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
                ok = true;
                file.delete();
            } catch (IOException e) {
            }
        }
        return ok;
    }

    private static void mkdirsIfNotExists(String path) {
        File file = new  File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void deleteAllFile() {
        File file = new  File(getRootPath() + "/zwsb");
        if (file.exists()) {
            deleteFile(file);
        }
    }

    private static void deleteFile(File file) {
        if (!file.isFile()) {
            for (File f : file.listFiles()) {
                deleteFile(f);
            }
        } else {
            file.delete();
        }
    }

    public static void deleteImageFiles() {
        File file = new  File(UER_GESTURE_IMAGE_PATH);
        if (file.exists()) {
            deleteFile(file);
        }
    }

    public static void renameFile(String srcFileName, String desFileName) {
        File srcFile = new  File(USER_GESTURE_PATH + srcFileName);
        if (!srcFile.exists()) {
            try {
                srcFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        srcFile.renameTo(new  File(USER_GESTURE_PATH + desFileName));
    }

    public static void deleteFile(String fileName) {
        File file = new  File(USER_GESTURE_PATH + fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static String ellipsizeStr(String str) {
        if (str != null) {
            // str contains hello
            if (str.length() > 15) {
                return str.substring(0, 10) + "..." + str.substring(str.length() - 4, str.length());
            } else {
                return str;
            }
        } else {
            return null;
        }
    }

    public static String getDateFormatTime(Date date) {
        SimpleDateFormat format = new  SimpleDateFormat("yy:MM:dd hh:mm:ss");
        return format.format(date);
    }
}
