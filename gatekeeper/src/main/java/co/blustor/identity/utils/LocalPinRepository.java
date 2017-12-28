package co.blustor.identity.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by mbrooks on 12/20/17.
 */

public class LocalPinRepository {
    private static final String TAG = "LocalPinRepository";
    private static final String PATH = "securepin/";
    private static final String PIN_FILE_NAME = "pin.txt";

    private File mStorage;

    public LocalPinRepository() {
        File externalStorage = Environment.getExternalStorageDirectory();
        mStorage = new File(externalStorage, PATH);

        if (!mStorage.exists()) {
            if (!mStorage.mkdirs()) {
                Log.e(TAG, "Could not create storage directory: " + mStorage.getAbsolutePath());
            }
        }
    }

    public void savePin(String password, Context context) {
        Log.d(TAG, "Save pin to file = " + password);

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(PIN_FILE_NAME, Context.MODE_PRIVATE));
            outputStreamWriter.write(password);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void deletePin() {
        File file = new File(PIN_FILE_NAME );
        file.delete();
    }

    public String getPin(Context context) {
        String ret = "";
        try {
            InputStream inputStream = context.openFileInput(PIN_FILE_NAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public boolean isPinSet() {
        File file = new File(PIN_FILE_NAME);
        boolean isExistingFile = file.exists();
        Log.d(TAG, "isExistingFile = " + isExistingFile);
        return isExistingFile;
    }

}
