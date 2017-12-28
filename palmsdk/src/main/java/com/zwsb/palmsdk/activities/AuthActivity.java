package com.zwsb.palmsdk.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.redrockbiometrics.palm.PalmModelingResultMessage;
import com.zwsb.palmsdk.R;
import com.zwsb.palmsdk.camera.CameraWrapper;
import com.zwsb.palmsdk.customViews.CircleAnimationView;
import com.zwsb.palmsdk.customViews.GradientView;
import com.zwsb.palmsdk.customViews.PinInputView;
import com.zwsb.palmsdk.customViews.ScanCircleGradientView;
import com.zwsb.palmsdk.customViews.ScanView;
import com.zwsb.palmsdk.helpers.BaseUtil;
import com.zwsb.palmsdk.helpers.Constant;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;
import com.zwsb.palmsdk.palmApi.PalmAPI;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Svyatozar on 18.03.17.
 */

public class AuthActivity extends AppCompatActivity
{
    /**
     * Success scan result code
     */
    public static final int ON_SCAN_RESULT_OK = 42;
    public static final int ON_SCAN_RESULT_ERROR = 43;

    public static final String USER_ACTION_KEY          = "USER_ACTION_KEY";
    public static final String USER_NAME_KEY          = "USER_NAME_KEY";
    public static final String IS_RIGHT_PALM_NEEDED_KEY = "IS_RIGHT_PALM_NEEDED_KEY";
    public static final String IS_LOCK_NEEDED_KEY = "IS_LOCK_NEEDED_KEY";
    public static final String IS_PIN_NEEDED_KEY = "IS_PIN_NEEDED_KEY";

    public static final int NEW_USER_ACTION  = 0;
    public static final int READ_USER_ACTION = 1;
    public static final int TEST_ACTION      = 2;

    public static final int SCAN_FAILED_ANIMATION_DELAY = 1000;

    SurfaceView  surfaceView;
    ScanView     scanView;
    FrameLayout  scanRootLayout;
    ImageView    palmImageView;
    LinearLayout infoLayout;
    TextView     testCapTextView;
    TextView     palmTextView;
    ImageView    pinButton;
    ImageView closeButton;

    CircleAnimationView    circleAnimationView;
    ScanCircleGradientView circleGradientView;
    GradientView           gradientView;
    TextView               resultTextView;
    TextView               pinHint;
    FrameLayout            scanInfoLayout;
    LinearLayout           pinInfoLayout;
    FrameLayout            scanLayout;
    PinInputView           pinInputView;
    ImageView              palmButton;
    FrameLayout            pinLayout;
    RatingBar              ratingBar;
    RatingBar              errorRatingBar;

    private CameraWrapper cameraWrapper;

    private int     userAction  = NEW_USER_ACTION;
    private boolean isRightPalm = false;
    private String userName = "";
    private boolean isLockNeeded = false;
    private boolean isPinEnabled = false;

    private int failureAttempts = 0;

    /**
     * @param context current context
     * @param isLockNeeded is onBackPressed() lock needed
     * @param userName user id for image processing
     * @return intent for call startActivityForResult() with READ_USER_ACTION
     */
    public static Intent getIntent(Context context, String userName, boolean isLockNeeded, boolean isPinEnabled) {
        Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(AuthActivity.USER_ACTION_KEY, AuthActivity.READ_USER_ACTION);
        intent.putExtra(AuthActivity.USER_NAME_KEY, userName);
        intent.putExtra(AuthActivity.IS_LOCK_NEEDED_KEY, isLockNeeded);
        intent.putExtra(AuthActivity.IS_PIN_NEEDED_KEY, isPinEnabled);

        return intent;
    }

    public static Intent getIntentForRightPalm(Context context, String userName) {
        Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(AuthActivity.USER_ACTION_KEY, AuthActivity.NEW_USER_ACTION);
        intent.putExtra(AuthActivity.IS_RIGHT_PALM_NEEDED_KEY, true);
        intent.putExtra(AuthActivity.USER_NAME_KEY, userName);

        return intent;
    }

    public static Intent getIntentForLeftPalm(Context context, String userName) {
        Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra(AuthActivity.USER_ACTION_KEY, AuthActivity.NEW_USER_ACTION);
        intent.putExtra(AuthActivity.IS_RIGHT_PALM_NEEDED_KEY, false);
        intent.putExtra(AuthActivity.USER_NAME_KEY, userName);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        scanView = (ScanView) findViewById(R.id.scanView);
        scanRootLayout = (FrameLayout) findViewById(R.id.scanRootLayout);
        palmImageView = (ImageView) findViewById(R.id.palmImageView);
        infoLayout = (LinearLayout) findViewById(R.id.infoLayout);
        testCapTextView = (TextView) findViewById(R.id.testCapTextView);
        palmTextView = (TextView) findViewById(R.id.palmTextView);
        circleAnimationView = (CircleAnimationView) findViewById(R.id.circleAnimationView);
        circleGradientView = (ScanCircleGradientView) findViewById(R.id.circleGradientView);
        gradientView = (GradientView) findViewById(R.id.gradientView);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        pinHint = (TextView) findViewById(R.id.pinHint);
        closeButton = (ImageView) findViewById(R.id.closeButton);

        pinButton = (ImageView) findViewById(R.id.pinButton);
        scanInfoLayout = (FrameLayout) findViewById(R.id.scanInfoLayout);
        pinInfoLayout = (LinearLayout) findViewById(R.id.pinInfoLayout);
        scanLayout = (FrameLayout) findViewById(R.id.scanLayout);
        pinInputView = (PinInputView) findViewById(R.id.pinInputView);
        palmButton = (ImageView) findViewById(R.id.palmButton);
        pinLayout = (FrameLayout) findViewById(R.id.pinLayout);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        errorRatingBar = (RatingBar) findViewById(R.id.errorRatingBar);

        pinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPinButtonClick();
            }
        });
        palmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPalmButtonClick();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, R.string.permissions_alert, Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            userAction = getIntent().getIntExtra(USER_ACTION_KEY, NEW_USER_ACTION);
            isRightPalm = getIntent().getBooleanExtra(IS_RIGHT_PALM_NEEDED_KEY, false);
            userName = getIntent().getStringExtra(USER_NAME_KEY);
            isLockNeeded = getIntent().getBooleanExtra(IS_LOCK_NEEDED_KEY, false);
            isPinEnabled = getIntent().getBooleanExtra(IS_PIN_NEEDED_KEY, false);

            if (userAction == READ_USER_ACTION && !SharedPreferenceHelper.isLockEnabled())
            {
                Intent intent = new Intent();
                setResult(ON_SCAN_RESULT_OK, intent);
                finish();
            }

            EventBus.getDefault().register(this);
            init();

            pinInputView.setPinObserver(new PinInputView.PinObserver()
            {
                @Override
                public void onPinChanged(String pin)
                {
                    ratingBar.setRating(pin.length());
                    if (pin.length() == 4) {
                        String correctPin = SharedPreferenceHelper.getSharedPreferenceString(getApplicationContext(), SharedPreferenceHelper.PIN_KEY+userName, "");
                        if (correctPin.equals(pin)) {
                            pinHint.setText(R.string.check_success);
                            pinHint.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));

                            new Handler(new Handler.Callback() {
                                @Override
                                public boolean handleMessage(Message msg) {
                                    finish();
                                    return true;
                                }
                            }).sendEmptyMessageDelayed(0, 1000);
                        } else {
                            pinHint.setText(R.string.check_failure);
                            pinHint.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));

                            errorRatingBar.setVisibility(View.VISIBLE);
                            pinInputView.clear();

                            new Handler(new Handler.Callback() {
                                @Override
                                public boolean handleMessage(Message msg) {
                                    pinHint.setText(R.string.enter_pin);
                                    pinHint.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

                                    ratingBar.setRating(0);
                                    errorRatingBar.setVisibility(View.GONE);

                                    return true;
                                }
                            }).sendEmptyMessageDelayed(0, 1000);
                        }
                    }
                }
            });

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    @Override
    protected void onDestroy()
    {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        initPalmAPI();
        if (null != cameraWrapper && null != cameraWrapper.camera)
        {
            cameraWrapper.camera.startPreview();
        }
    }

    private void init()
    {
        BaseUtil.initScreenSize(this);
        ViewGroup.LayoutParams params = scanRootLayout.getLayoutParams();
        params.width = (int) (Constant.RESOLUTION_RATIO_WIDTH * BaseUtil.matrixMin);
        params.height = (int) (Constant.RESOLUTION_RATIO_HEIGHT * BaseUtil.matrixMin);
        scanRootLayout.setLayoutParams(params);

        cameraWrapper = new CameraWrapper(AuthActivity.this, surfaceView, scanView, userName);
        scanView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                scanView.reDrawGesture(scanView.getWidth() / 2, scanView.getY() / 2, 1);
            }
        });
    }

    private void initPalmAPI()
    {
        switch (userAction)
        {
            case NEW_USER_ACTION:
                PalmAPI.testModel();

                palmTextView.setVisibility(View.VISIBLE);
                infoLayout.setVisibility(View.VISIBLE);
                closeButton.setVisibility(View.GONE);

                if (isRightPalm)
                {
                    BaseUtil.CURRENT_PALM_PATH = BaseUtil.RIGHT_PALM_PATH;
                    palmTextView.setText(getString(R.string.right_palm));
                    palmImageView.setImageResource(R.drawable.left_palm);
                }
                else
                {
                    BaseUtil.CURRENT_PALM_PATH = BaseUtil.LEFT_PALM_PATH;
                    palmTextView.setText(getString(R.string.left_palm));
                    palmImageView.setImageResource(R.drawable.right_palm);
                }
                break;
            case READ_USER_ACTION:
                scanView.reDrawGesture(0, 0, 0);
                infoLayout.setVisibility(View.VISIBLE);
                pinButton.setVisibility(isPinEnabled ? View.VISIBLE : View.GONE);
                infoLayout.setVisibility(View.GONE);
                closeButton.setVisibility(View.GONE);
                palmTextView.setText(userName.toUpperCase());

                new Thread()
                {
                    @Override
                    public void run()
                    {
                        PalmAPI.testMatch(AuthActivity.this, userName);
                    }
                }.start();
                break;
            case TEST_ACTION:
                scanView.reDrawGesture(0, 0, 0);
                infoLayout.setVisibility(View.VISIBLE);
                palmImageView.setImageResource(R.drawable.button_goto_palm);
                pinButton.setVisibility(isPinEnabled ? View.VISIBLE : View.GONE);
                infoLayout.setVisibility(View.GONE);
                closeButton.setVisibility(View.GONE);
                palmTextView.setText(userName.toUpperCase());

                new Thread()
                {
                    @Override
                    public void run()
                    {
                        PalmAPI.testMatch(AuthActivity.this, userName);
                    }
                }.start();

                break;
        }
    }

    @Subscribe
    public void processCameraEvent(final CameraWrapper.CameraEvent event)
    {
        switch (event)
        {
            case ON_ERROR:
                //String status = (event.message != null && event.message.status != null) ? event.message.status.toString() : null;
                //Toast.makeText(getApplicationContext(), "ERROR " + status, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                setResult(ON_SCAN_RESULT_ERROR, intent);

                finish();

                break;
            case ON_SCAN_FAILURE:
                failureAttempts++;
                if (failureAttempts > 4) {
                    scanView.clear(true);
                    circleAnimationView.setSuccess(false);
                    resultTextView.setText(R.string.check_failure);
                    resultTextView.animate().alpha(1).start();

                    new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            finish();
                            return true;
                        }
                    }).sendEmptyMessageDelayed(0, 2000);
                } else {
                    PalmAPI.testMatch(AuthActivity.this, userName);
                }
                break;

            case ON_SCAN_SUCCESS:
                scanView.clear(true);
                circleAnimationView.setSuccess(true);
                resultTextView.setText(R.string.check_success);
                resultTextView.animate().alpha(1).start();

                new Handler(new Handler.Callback()
                {
                    @Override
                    public boolean handleMessage(Message message)
                    {
                        Intent intent = new Intent();
                        setResult(ON_SCAN_RESULT_OK, intent);

                        finish();
                        return true;
                    }
                }).sendEmptyMessageDelayed(0, 2000);

                break;

            case ON_SAVE_PALM_SUCCESS:
                scanView.reDrawGesture(0, 0, 0);

                if (isRightPalm)
                {
                    SharedPreferenceHelper.setRightPalmEnabled(true, userName);
                    SharedPreferenceHelper.setSavedPalmId(((PalmModelingResultMessage)event.message).modelID, SharedPreferenceHelper.RIGHT_PALM_ID_KEY, userName);
                }
                else
                {
                    SharedPreferenceHelper.setLeftPalmEnabled(true, userName);
                    SharedPreferenceHelper.setSavedPalmId(((PalmModelingResultMessage)event.message).modelID, SharedPreferenceHelper.LEFT_PALM_ID_KEY, userName);
                }

                finish();
        }
    }

    public void onPinButtonClick()
    {
        scanInfoLayout.animate().alpha(0).start();
        pinLayout.animate().alpha(1).start();
        pinInfoLayout.animate().alpha(1).start();
        circleGradientView.animate().alpha(1).start();
        scanLayout.animate().alpha(0).start();
        gradientView.animate().alpha(0).start();

        palmButton.setVisibility(View.VISIBLE);
        pinButton.setVisibility(View.GONE);
    }

    public void onPalmButtonClick()
    {
        scanLayout.animate().alpha(1).start();
        gradientView.animate().alpha(1).start();
        scanInfoLayout.animate().alpha(1).start();
        pinInfoLayout.animate().alpha(0).start();
        pinLayout.animate().alpha(0).start();
        circleGradientView.animate().alpha(0).start();

        palmButton.setVisibility(View.GONE);
        pinButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed()
    {
        if (!isLockNeeded) {
            super.onBackPressed();
        }
    }
}
