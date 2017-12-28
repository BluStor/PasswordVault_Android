package com.zwsb.palmsdk.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.redrockbiometrics.palm.PalmFrame;
import com.redrockbiometrics.palm.PalmMatchingResultMessage;
import com.redrockbiometrics.palm.PalmMessage;
import com.redrockbiometrics.palm.PalmMessageEnum;
import com.redrockbiometrics.palm.PalmModelingResultMessage;
import com.redrockbiometrics.palm.PalmQuad;
import com.redrockbiometrics.palm.PalmStatus;
import com.redrockbiometrics.palm.PalmsDetectedMessage;
import com.zwsb.palmsdk.customViews.ScanView;
import com.zwsb.palmsdk.helpers.BaseUtil;
import com.zwsb.palmsdk.helpers.Constant;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;
import com.zwsb.palmsdk.palmApi.PalmAPI;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import static com.zwsb.palmsdk.camera.CameraWrapper.CameraEvent.ON_ERROR;
import static com.zwsb.palmsdk.camera.CameraWrapper.CameraEvent.ON_SAVE_PALM_SUCCESS;
import static com.zwsb.palmsdk.camera.CameraWrapper.CameraEvent.ON_SCAN_FAILURE;
import static com.zwsb.palmsdk.camera.CameraWrapper.CameraEvent.ON_SCAN_SUCCESS;

/**
 * Contains camera object, and manage surface refresh events
 */
public class CameraWrapper implements Callback
{
    public enum CameraEvent {
        ON_SCAN_SUCCESS, ON_SCAN_FAILURE, ON_SAVE_PALM_SUCCESS, ON_ERROR;

        public PalmMessage message;
        public CameraEvent setData(PalmMessage palmMessage) {
            message = palmMessage;
            return this;
        }
    }

    private static final String HANDLER_THREAD_NAME = "HANDLER_THREAD_NAME";

    private Context mContext = null;

    private SurfaceView surface = null;
    private ScanView scanView = null;
    private String userName;

    private int counter = 0;
    private boolean atleastOneMatched = false;

    float centerX;
    float centerY;

    public Camera camera;
    private HandlerThread handlerThread;
    private FrameHandler frameHandler;

    public CameraWrapper(Context context, SurfaceView surface, ScanView view, String userName) {
        this.mContext = context;
        this.surface = surface;
        this.scanView = view;
        this.userName = userName;

        SurfaceHolder surfaceHolder = surface.getHolder();
        surfaceHolder.setFixedSize(BaseUtil.screenWidth, BaseUtil.screenHeight);
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        handlerThread = new HandlerThread(HANDLER_THREAD_NAME);
        handlerThread.start();
        frameHandler = new FrameHandler(handlerThread.getLooper());

        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @SuppressWarnings("deprecation")
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        try
        {
            /**
             * Init camera
             */
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();

            for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
                Camera.getCameraInfo( camIdx, cameraInfo );
                if(cameraInfo.facing == Constant.DEFAULT_CAMERA) {
                    camera =Camera.open(camIdx);
                }
            }

            /**
             * Nexus 6 fixes, need Camera2 API
             */
            if (Build.MODEL.equals("Nexus 6P")) {
                BaseUtil.cameraFlipped = true;
                camera.setDisplayOrientation(270);
            } else {
                BaseUtil.cameraFlipped = false;
                camera.setDisplayOrientation(90);
            }
            CamParUtil.initCamPar(camera);
            //setCameraDisplayOrientation(((Activity) mContext), cameraId, camera);

            camera.setPreviewDisplay(surface.getHolder());
            camera.startPreview();

            /**
             * Callback for receive frames, and process them
             */
            camera.setPreviewCallback(previewCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * callback for process data from camera
     */
    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, final Camera camera) {
            Message frameDataMessage = frameHandler.obtainMessage();
            frameDataMessage.obj = bytes;
            frameHandler.sendMessage(frameDataMessage);
        }
    };

    /**
     * Handler for process single frame on non-UI thread
     */
    class FrameHandler extends Handler {
        public FrameHandler(Looper myLooper) {
            super(myLooper);
        }

        public void handleMessage(Message msg) {
            byte[] bytes = (byte[])msg.obj;

            Camera.Size size    = camera.getParameters().getPreviewSize();
            PalmFrame   frame   = PalmAPI.loadFrameFromNV21(bytes, size.width, size.height);
            PalmAPI.m_PalmBiometrics.ProcessFrame(frame);
            PalmMessage message = PalmAPI.m_PalmBiometrics.WaitMessage();

            Message resultMessage = palmMessageHandler.obtainMessage();

            if (message != null) {
                resultMessage.obj = message;
            } else {
                resultMessage.obj = new PalmMessage(PalmMessageEnum.None);
            }

            palmMessageHandler.sendMessage(resultMessage);
        }
    }

    /**
     * Handler for process PalmMessage on UI Thread
     */
    Handler palmMessageHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            PalmMessage message = (PalmMessage) msg.obj;

            if (message != null) {
                if (message.status != PalmStatus.Success) {
                    if (message.status != null) {
                        CameraEvent event = ON_ERROR;
                        event.message = message;
                        EventBus.getDefault().post(event);
                    }
                } else {
                    switch (message.type) {
                        /**
                         * Just palm detected event, draw circle where the palm is
                         */
                        case None:
                            scanView.clear(true);
                            break;
                        case PalmsDetected:
                            PalmQuad quad = ((PalmsDetectedMessage) message).palms[0].quad;

                            float newCenterX = (quad.cx + quad.ax) / 2;
                            float newCenterY = (quad.cy + quad.ay) / 2;

                            if (centerX != newCenterX && centerY != newCenterY) {
                                double diag2 = (quad.cx - quad.ax) * (quad.cx - quad.ax) + (quad.cy - quad.ay) * (quad.cy - quad.ay);
                                double halfEdge = Math.sqrt(diag2) / 2.0;

                                centerX = newCenterX;
                                centerY = newCenterY;

                                scanView.centerX = centerX;
                                scanView.centerY = centerY;
                                scanView.disableClear();
                                scanView.reDrawGesture((Constant.RESOLUTION_RATIO_WIDTH - centerY) * BaseUtil.matrixWidth,
                                        (Constant.RESOLUTION_RATIO_HEIGHT - centerX) * BaseUtil.matrixHeight,
                                        ((float)halfEdge) * (BaseUtil.matrixWidth - 0.2f));

                                clearHandler.removeMessages(0);
                            }

                            clearHandler.sendEmptyMessageDelayed(0, 300);

                            break;
                        /**
                         * Compare received data with saved user profile
                         */
                        case MatchingResult:
                            boolean isMatch = ((PalmMatchingResultMessage) message).result;
                            Log.i("LOG", "matching result " + isMatch);

                            atleastOneMatched = isMatch || atleastOneMatched;
                            if (++counter == SharedPreferenceHelper.getNumberOfRegisteredPalms(mContext, userName)) {
                                if (atleastOneMatched) {
                                    EventBus.getDefault().post(ON_SCAN_SUCCESS);
                                } else {
                                    EventBus.getDefault().post(ON_SCAN_FAILURE);
                                }
                                counter = 0;
                                atleastOneMatched = false;
                            }
                            break;
                        /**
                         * Save new user data
                         */
                        case ModelingResult:
                            System.out.println("modeling result");
                            byte[] modelResultData = ((PalmModelingResultMessage) message).data;
                            PalmAPI.saveModel(mContext, modelResultData, userName);
                            System.out.println(modelResultData.toString());

                            EventBus.getDefault().post(ON_SAVE_PALM_SUCCESS.setData(message));
                            break;
                        default:
                            scanView.reDrawGesture(0, 0, 0);
                            break;
                    }
                }
            }
            return true;
        }
    });

    Handler clearHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (centerX == scanView.centerX && centerY == scanView.centerY) {
                scanView.clear(true);
            }
            return true;
        }
    });

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }

        if (handlerThread != null) {
            handlerThread.quitSafely();
        }
    }
}
