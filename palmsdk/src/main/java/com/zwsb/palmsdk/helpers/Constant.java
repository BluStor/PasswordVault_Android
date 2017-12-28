package com.zwsb.palmsdk.helpers;

import android.hardware.Camera;

public class Constant {

    public static final String VIDEO_STATUS = "video_status";

    public static final String VIDEO_ACTION = "video_action";

    /**
     * Specify camera: front or back
     */
    //CAMERA_FACING_BACK
    public static final int DEFAULT_CAMERA = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public static final int RESOLUTION_RATIO_HEIGHT = 640;

    public static final int RESOLUTION_RATIO_WIDTH = 480;

    public static final String ZWSB_SHARED_NAME = "zwsb_shared";

    public static final String IS_FIRST = "is_first";

    public static final String USER_EMAIL_KEY = "USER_EMAIL_KEY";

    public enum VideoStatus {

        SHOWUSERLIST, SHOWADMINLIST, SHOWVIDEO, SHOWSUCCESS
    }

    public enum VideoAction {

        NEWUSER, INVALIDATE, RETAKE
    }

    public enum ScreenSwitch {

        PORTRAIT, LANDSCAPE
    }

    public enum HandlerMode {

        LOADFRAME, BGLOADFRAME, VRLOADFRAME
    }
}
