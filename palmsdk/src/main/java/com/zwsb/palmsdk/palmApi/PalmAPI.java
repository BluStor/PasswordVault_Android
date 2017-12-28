package com.zwsb.palmsdk.palmApi;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.redrockbiometrics.palm.PalmBiometrics;
import com.redrockbiometrics.palm.PalmFrame;
import com.redrockbiometrics.palm.PalmImage;
import com.redrockbiometrics.palm.PalmMessage;
import com.redrockbiometrics.palm.PalmModelID;
import com.redrockbiometrics.palm.PalmStatus;
import com.zwsb.palmsdk.CryptoUtil;
import com.zwsb.palmsdk.PalmSDK;
import com.zwsb.palmsdk.camera.CameraWrapper;
import com.zwsb.palmsdk.helpers.BaseUtil;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyPair;

import io.reactivex.Single;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static com.redrockbiometrics.palm.PalmMessageEnum.None;
import static com.zwsb.palmsdk.camera.CameraWrapper.CameraEvent.ON_ERROR;

public class PalmAPI
{
    public static PalmBiometrics m_PalmBiometrics = null;
    public static byte[] m_Data = null;

    public static void init (String email) {
        m_PalmBiometrics = new PalmBiometrics(email);
        m_PalmBiometrics.SetCameraOrientation(2);
    }

    /**
     * Frame processing method
     * @param yuv
     * @param width
     * @param height
     * @return PalmFrame
     */
    public static PalmFrame loadFrameFromNV21(byte[] yuv, int width, int height) {
        if (BaseUtil.cameraFlipped) {
            int dataLength = width * height;
            if (m_Data == null || m_Data.length != dataLength) {
                m_Data = new byte[dataLength];
            }
            for (int i = 0; i < dataLength; i++) {
                m_Data[i] = yuv[dataLength - 1 - i];
            }
            return new PalmFrame(0, 0, 50, width, height, 8, m_Data);
        } else {
            return new PalmFrame(0, 0, 50, width, height, 8, yuv);
        }
    }

    /**
     * Loading palm model, and decrypt it for matching
     * /storage/emulated/0/zwsb_r/gesture/USER_NAME.bin
     *
     * @param filename
     * @return
     */
    public static byte[] loadModel(Context context, String filename) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(filename)));
            StringBuffer dataString = new StringBuffer();
            String dataPiece;
            while ((dataPiece = br.readLine()) != null) {
                dataString.append(dataPiece);
            }

            //return CryptoUtil.decrypt(ret);
            //Log.d("PALM_LOG", "READED: " + dataString);
            return Base64.decode(dataString.toString(), Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    /**
     * Encrypt user palm model, and save it to memory
     * @param data
     */
    public static void saveModel(Context context, byte[] data, String userName) {
        //byte[] encryptedData = CryptoUtil.encrypt(data);

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(BaseUtil.USER_GESTURE_PATH + BaseUtil.CURRENT_PALM_PATH + userName, MODE_PRIVATE)));
            bw.write(Base64.encodeToString(data, Base64.DEFAULT));
            bw.close();
            //Log.d("PALM_LOG", "WRITE SUCCESS: " + Base64.encodeToString(data, Base64.DEFAULT));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save user's palm image
     * /storage/emulated/0/zwsb_r/gesture_invalidate/model.png
     *
     * @param filename
     * @param image
     */
    public static void saveImage(final String filename, PalmImage image) {
        Single.just(image)
                .observeOn(Schedulers.io())
                .subscribe(new DisposableSingleObserver<PalmImage>()
                {
                    @Override
                    public void onSuccess(PalmImage image)
                    {
                        Bitmap img = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888);
                        byte[] data = image.data;
                        int i = 0;
                        for (int y = 0; y < image.height; y++) {
                            for (int x = 0; x < image.width; x++) {
                                int r = data[i++];
                                int g = data[i++];
                                int b = data[i++];
                                int rgb = 0xFF000000 | (r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8;
                                img.setPixel(x, y, rgb);
                            }
                        }
                        try {
                            FileOutputStream file = new FileOutputStream(filename);
                            img.compress(Bitmap.CompressFormat.PNG, 100, file);
                            Log.i("LOG", "save image succeeded " + filename + " " + Integer.toString(img.getWidth()) + " x " + Integer.toString(img.getHeight()));
                        } catch (Exception e) {
                            Log.i("LOG", "save image failed" + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Throwable e)
                    {}
                });
    }

    /**
     * Call this to make a new user
     */
    public static void testModel() {
        // set mode to model
        PalmStatus status = m_PalmBiometrics.Model();
        Log.i("LOG", "model() called, returned " + Integer.toString(status.value));

        if (null != status && status != PalmStatus.Success) {
            CameraWrapper.CameraEvent event = ON_ERROR;
            PalmMessage message = new PalmMessage(None);
            message.status = status;
            event.message = message;
            EventBus.getDefault().post(event);
        }
    }

    /**
     * Call this to match palm with saved profile(s)
     */
    public static void testMatch(Context context, String userName) {
        boolean isLeftPalmEnabled = SharedPreferenceHelper.isLeftPalmEnabled(context, userName);
        boolean isRightPalmEnabled = SharedPreferenceHelper.isRightPalmEnabled(context, userName);

        PalmStatus status = null;
        if (isLeftPalmEnabled & isRightPalmEnabled) {
            byte[] modelLeftData = loadModel(context, BaseUtil.USER_GESTURE_PATH + BaseUtil.LEFT_PALM_PATH + userName);
            byte[] modelRightData = loadModel(context, BaseUtil.USER_GESTURE_PATH + BaseUtil.RIGHT_PALM_PATH + userName);

            PalmModelID[] modelID = new PalmModelID[2];
            modelID[0] = new PalmModelID();
            modelID[1] = new PalmModelID();

            m_PalmBiometrics.LoadModel(modelLeftData, modelID[0]);
            m_PalmBiometrics.LoadModel(modelRightData, modelID[1]);
            Log.i("PalmIDLog", "add model id " + byteArrayToHex(modelID[0].id) + " AND SECOND PALM " + byteArrayToHex(modelID[1].id));

            m_PalmBiometrics.AddModel(modelID[0]);
            m_PalmBiometrics.AddModel(modelID[1]);

            status = m_PalmBiometrics.Match(modelID);
        } else if (isLeftPalmEnabled) {
            byte[] modelLeftData = loadModel(context, BaseUtil.USER_GESTURE_PATH + BaseUtil.LEFT_PALM_PATH + userName);
            PalmModelID[] modelID = new PalmModelID[1];
            modelID[0] = new PalmModelID();

            m_PalmBiometrics.LoadModel(modelLeftData, modelID[0]);
            Log.i("PalmIDLog", "add model id " + byteArrayToHex(modelID[0].id));

            m_PalmBiometrics.AddModel(modelID[0]);
            status = m_PalmBiometrics.Match(modelID);
        } else if (isRightPalmEnabled) {
            byte[] modelRightData = loadModel(context, BaseUtil.USER_GESTURE_PATH + BaseUtil.RIGHT_PALM_PATH + userName);
            PalmModelID[] modelID = new PalmModelID[1];
            modelID[0] = new PalmModelID();

            m_PalmBiometrics.LoadModel(modelRightData, modelID[0]);
            Log.i("PalmIDLog", "add model id " + byteArrayToHex(modelID[0].id));

            m_PalmBiometrics.AddModel(modelID[0]);
            status = m_PalmBiometrics.Match(modelID);
        }

        if (null != status && status != PalmStatus.Success) {
            CameraWrapper.CameraEvent event = ON_ERROR;
            PalmMessage message = new PalmMessage(None);
            message.status = status;
            event.message = message;
            EventBus.getDefault().post(event);
        }
    }

    private static String byteArrayToHex(byte[] arr) {
        StringBuilder builder = new StringBuilder(arr.length * 2);
        for (byte b : arr)
            builder.append(String.format("%02x", b));
        return builder.toString();
    }
}
