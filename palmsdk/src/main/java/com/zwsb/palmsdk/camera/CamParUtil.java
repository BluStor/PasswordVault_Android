package com.zwsb.palmsdk.camera;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;

import com.zwsb.palmsdk.helpers.Constant;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CamParUtil {
    private static final float MAX_EXPOSURE_COMPENSATION = 1.5f;
    private static final float MIN_EXPOSURE_COMPENSATION = 0.0f;

    private static final int AREA_PER_1000 = 400;
    private static final String TAG = "PALMID_TAG";

    public static void initCamPar(Camera camera) {
        if (camera != null) {
            Parameters params = camera.getParameters();
            Log.i("LOG", params.flatten());

            //set camera resolution
            List<Size> listPreviewSizes = params.getSupportedPreviewSizes();
            for (Size size : listPreviewSizes) {
                if (size.width == Constant.RESOLUTION_RATIO_HEIGHT && size.height == Constant.RESOLUTION_RATIO_WIDTH) {
                    Log.i("LOG", "set resolution");
                    params.setPreviewSize(Constant.RESOLUTION_RATIO_HEIGHT, Constant.RESOLUTION_RATIO_WIDTH);
                }
            }

            //set camera FPS to 30
            params.setPreviewFrameRate(30);

            /**
             * set camera parameters for better performance
             */
            setMetering(params);
            setFocusArea(params);
            setBestExposure(params, true);

            camera.setParameters(params);
        }
    }

    public static void setMetering(Camera.Parameters parameters) {
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> middleArea = buildMiddleArea(AREA_PER_1000);
            Log.i("PALMID_TAG", "Setting metering area to : " + toString(middleArea));
            parameters.setMeteringAreas(middleArea);
        } else {
            Log.i("PALMID_TAG", "Device does not support metering areas");
        }
    }

    public static void setFocusArea(Camera.Parameters parameters) {
        if (parameters.getMaxNumFocusAreas() > 0) {
            Log.i(TAG, "Old focus areas: " + toString(parameters.getFocusAreas()));
            List<Camera.Area> middleArea = buildMiddleArea(AREA_PER_1000);
            Log.i(TAG, "Setting focus area to : " + toString(middleArea));
            parameters.setFocusAreas(middleArea);
        } else {
            Log.i(TAG, "Device does not support focus areas");
        }
    }

    public static void setBestExposure(Camera.Parameters parameters, boolean lightOn) {
        int minExposure = parameters.getMinExposureCompensation();
        int maxExposure = parameters.getMaxExposureCompensation();
        float step = parameters.getExposureCompensationStep();
        if ((minExposure != 0 || maxExposure != 0) && step > 0.0f) {
            // Set low when light is on
            float targetCompensation = lightOn ? MIN_EXPOSURE_COMPENSATION : MAX_EXPOSURE_COMPENSATION;
            int compensationSteps = Math.round(targetCompensation / step);
            float actualCompensation = step * compensationSteps;
            // Clamp value:
            compensationSteps = Math.max(Math.min(compensationSteps, maxExposure), minExposure);
            if (parameters.getExposureCompensation() == compensationSteps) {
                Log.i(TAG, "Exposure compensation already set to " + compensationSteps + " / " + actualCompensation);
            } else {
                Log.i(TAG, "Setting exposure compensation to " + compensationSteps + " / " + actualCompensation);
                parameters.setExposureCompensation(compensationSteps);
            }
        } else {
            Log.i(TAG, "Camera does not support exposure compensation");
        }
    }

    private static List<Camera.Area> buildMiddleArea(int areaPer1000) {
        return Collections.singletonList(
                new Camera.Area(new Rect(-areaPer1000, -areaPer1000, areaPer1000, areaPer1000), 1));
    }

    private static String toString(Iterable<Camera.Area> areas) {
        if (areas == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (Camera.Area area : areas) {
            result.append(area.rect).append(':').append(area.weight).append(' ');
        }
        return result.toString();
    }

    private static String toString(Collection<int[]> arrays) {
        if (arrays == null || arrays.isEmpty()) {
            return "[]";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        Iterator<int[]> it = arrays.iterator();
        while (it.hasNext()) {
            buffer.append(Arrays.toString(it.next()));
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(']');
        return buffer.toString();
    }
}
