package com.zwsb.palmsdk.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.redrockbiometrics.palm.PalmModelID;
import com.zwsb.palmsdk.PalmSDK;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by svyatozar on 03.04.17.
 */

public class SharedPreferenceHelper {
    private final static String PREF_FILE = "PREF";

    public static String IS_RIGHT_PALM_ENABLED_KEY = "IS_RIGHT_PALM_ENABLED_KEY";
    public static String IS_LEFT_PALM_ENABLED_KEY = "IS_LEFT_PALM_ENABLED_KEY";
    public static String IS_LOCK_ENABLED_KEY = "IS_LOCK_ENABLED_KEY";

    public static String RIGHT_PALM_ID_KEY = "RIGHT_PALM_ID_KEY";
    public static String LEFT_PALM_ID_KEY = "LEFT_PALM_ID_KEY";

    public static String EMAIL_KEY = "EMAIL_KEY";
    public static String USER_NAMES_KEY = "USER_NAMES_KEY";
    public static String PIN_KEY = "PIN_KEY";

    /**
     * Set a string shared preference
     * @param key - Key to set shared preference
     * @param value - Value for the key
     */
    public static void setSharedPreferenceString(Context context, String key, String value){
        SharedPreferences        settings = context.getSharedPreferences(PREF_FILE, 0);
        SharedPreferences.Editor editor   = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setStringArray(Context context, String key, List<String> array){
        setSharedPreferenceString(context, key, new Gson().toJson(array, ArrayList.class));
    }

    public static List<String> getStringArray(Context context, String key) {
        String json = getSharedPreferenceString(context, key, "");

        if (!json.isEmpty()) {
            return new Gson().fromJson(json, ArrayList.class);
        }

        return new ArrayList();
    }

    /**
     * Set a integer shared preference
     * @param key - Key to set shared preference
     * @param value - Value for the key
     */
    public static void setSharedPreferenceInt(Context context, String key, int value){
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Set a Boolean shared preference
     * @param key - Key to set shared preference
     * @param value - Value for the key
     */
    public static void setSharedPreferenceBoolean(Context context, String key, boolean value){
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Get a string shared preference
     * @param key - Key to look up in shared preferences.
     * @param defValue - Default value to be returned if shared preference isn't found.
     * @return value - String containing value of the shared preference if found.
     */
    public static String getSharedPreferenceString(Context context, String key, String defValue){
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE, 0);
        return settings.getString(key, defValue);
    }

    /**
     * Get a integer shared preference
     * @param key - Key to look up in shared preferences.
     * @param defValue - Default value to be returned if shared preference isn't found.
     * @return value - String containing value of the shared preference if found.
     */
    public static int getSharedPreferenceInt(Context context, String key, int defValue){
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE, 0);
        return settings.getInt(key, defValue);
    }

    /**
     * Get a boolean shared preference
     * @param key - Key to look up in shared preferences.
     * @param defValue - Default value to be returned if shared preference isn't found.
     * @return value - String containing value of the shared preference if found.
     */
    public static boolean getSharedPreferenceBoolean(Context context, String key, boolean defValue){
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE, 0);
        return settings.getBoolean(key, defValue);
    }

    /**
     * Getters and setters for PalmSDK keys
     */
    public static boolean isRightPalmEnabled(Context context, String userName) {
        return getSharedPreferenceBoolean(context, SharedPreferenceHelper.IS_RIGHT_PALM_ENABLED_KEY+userName, false);
    }

    public static boolean isLeftPalmEnabled(Context context, String userName) {
        return getSharedPreferenceBoolean(context, SharedPreferenceHelper.IS_LEFT_PALM_ENABLED_KEY+userName, false);
    }

    public static boolean isLockEnabled() {
        return getSharedPreferenceBoolean(PalmSDK.context, SharedPreferenceHelper.IS_LOCK_ENABLED_KEY, true);
    }

    public static void setRightPalmEnabled(boolean isEnabled, String userName) {
        setSharedPreferenceBoolean(PalmSDK.context, SharedPreferenceHelper.IS_RIGHT_PALM_ENABLED_KEY+userName, isEnabled);
    }

    public static void setLeftPalmEnabled(boolean isEnabled, String userName) {
        setSharedPreferenceBoolean(PalmSDK.context, SharedPreferenceHelper.IS_LEFT_PALM_ENABLED_KEY+userName, isEnabled);
    }

    public static void setLockEnabled(boolean isEnabled) {
        setSharedPreferenceBoolean(PalmSDK.context, SharedPreferenceHelper.IS_LOCK_ENABLED_KEY, isEnabled);
    }

    public static void setSavedPalmId(PalmModelID id, String key, String userName) {
        setSharedPreferenceString(PalmSDK.context, key+userName, new Gson().toJson(id, PalmModelID.class));
    }

    public static PalmModelID getSavedPalmId(String key, String userName) {
        String json = getSharedPreferenceString(PalmSDK.context, key+userName, "");

        if (!json.isEmpty()) {
            return new Gson().fromJson(json, PalmModelID.class);
        }

        return new PalmModelID();
    }

    /**
     * Get the number of registered palm templates
     * @return value - Integer showing the number of registered palm templates.
     */
    public static int getNumberOfRegisteredPalms(Context context, String userName){
        int count = 0;
        if (isRightPalmEnabled(context, userName)) {count++;}
        if (isLeftPalmEnabled(context, userName)) {count++;}
        return count;
    }
}