package com.zwsb.palmsdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.redrockbiometrics.palm.PalmImage;
import com.redrockbiometrics.palm.PalmMessage;
import com.redrockbiometrics.palm.PalmModelMaskMessage;
import com.zwsb.palmsdk.PalmSDK;
import com.zwsb.palmsdk.R;
import com.zwsb.palmsdk.helpers.BaseUtil;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;
import com.zwsb.palmsdk.palmApi.PalmAPI;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.redrockbiometrics.palm.PalmMessageEnum.ModelMask;

/**
 * Created by svyatozar on 03.04.17.
 */

public class PalmActivity extends AppCompatActivity
{
    public static final int ON_CLOSE_RESULT_CODE = 1;

    public static final String IS_RIGHT_PALM_NEEDED_KEY  = "USER_PALM_KEY";
    public static final String IS_NAV_NEEDED_KEY  = "IS_NAV_NEEDED_KEY";
    public static final String USER_NAME_KEY  = "USER_NAME_KEY";
    public static final String INTENT_TO_START_KEY  = "INTENT_TO_START_KEY";

    CircleImageView palmImageView;
    AppCompatButton clearButton;
    AppCompatButton bigNextButton;
    ProgressBar progressBar;
    FrameLayout toolbarLayout;
    ImageView closeButton;
    ImageView nextButton;
    TextView titleTextView;

    private boolean isRightPalm = false;
    private boolean isNavNeeded = false;
    private String userName;

    private Intent intentToStart;

    public static Intent getIntent(Context context, String userName, boolean isRightPalm, boolean isNavigationNeeded)
    {
        Intent intent = new Intent(context, PalmActivity.class);
        intent.putExtra(IS_RIGHT_PALM_NEEDED_KEY, isRightPalm);
        intent.putExtra(IS_NAV_NEEDED_KEY, isNavigationNeeded);
        intent.putExtra(USER_NAME_KEY, userName);

        return intent;
    }

    public static Intent getIntent(Context context, Intent intentToStart,  String userName, boolean isRightPalm, boolean isNavigationNeeded)
    {
        Intent intent = new Intent(context, PalmActivity.class);
        intent.putExtra(IS_RIGHT_PALM_NEEDED_KEY, isRightPalm);
        intent.putExtra(IS_NAV_NEEDED_KEY, isNavigationNeeded);
        intent.putExtra(USER_NAME_KEY, userName);
        intent.putExtra(INTENT_TO_START_KEY, intentToStart);

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palm);

        palmImageView = (CircleImageView)findViewById(R.id.palmImageView);
        clearButton = (AppCompatButton)findViewById(R.id.clearButton);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        toolbarLayout = (FrameLayout)findViewById(R.id.toolbarLayout);
        closeButton = (ImageView)findViewById(R.id.closeButton);
        nextButton = (ImageView)findViewById(R.id.nextButton);
        bigNextButton = (AppCompatButton)findViewById(R.id.bigNextButton);
        titleTextView = (TextView)findViewById(R.id.titleTextView);

        palmImageView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
        palmImageView.setBorderWidth(getResources().getDimensionPixelSize(R.dimen.border_width));

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearClick();
            }
        });

        isRightPalm = getIntent().getBooleanExtra(IS_RIGHT_PALM_NEEDED_KEY, false);
        isNavNeeded = getIntent().getBooleanExtra(IS_NAV_NEEDED_KEY, false);
        userName = getIntent().getStringExtra(USER_NAME_KEY);
        intentToStart = getIntent().getParcelableExtra(INTENT_TO_START_KEY);

        if (android.os.Build.VERSION.SDK_INT < 21) {
            tintViewBackground(clearButton, Color.parseColor("#FF1846"));
            tintViewBackground(bigNextButton, Color.parseColor("#2778FF"));
        } else {
            clearButton.getBackground().setColorFilter(Color.parseColor("#FF1846"), PorterDuff.Mode.SRC_IN);
            bigNextButton.getBackground().setColorFilter(Color.parseColor("#2778FF"), PorterDuff.Mode.SRC_IN);
        }

        View.OnClickListener onCloseListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(ON_CLOSE_RESULT_CODE, intent);
                finish();
            }
        };
        closeButton.setOnClickListener(onCloseListener);

        bigNextButton.setVisibility(isNavNeeded ? View.VISIBLE : View.GONE);
        nextButton.setVisibility(isNavNeeded ? View.VISIBLE : View.GONE);

        View.OnClickListener onNextListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != intentToStart) {
                    startActivity(intentToStart);
                } else {
                    if (SharedPreferenceHelper.isLeftPalmEnabled(PalmActivity.this, userName)) {
                        if (SharedPreferenceHelper.isRightPalmEnabled(PalmActivity.this, userName)) {
                            /** finish(); **/
                        } else {
                            startActivity(AuthActivity.getIntentForRightPalm(PalmActivity.this, userName));
                        }
                    } else {
                        startActivity(AuthActivity.getIntentForLeftPalm(PalmActivity.this, userName));
                    }
                }

                finish();
            }
        };
        nextButton.setOnClickListener(onNextListener);
        bigNextButton.setOnClickListener(onNextListener);

        View rootView = findViewById(R.id.rootView);
        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                float radius = v.getWidth() * 4 / 9 * 1;
                radius = (radius - (radius * BaseUtil.decreaseCoefficient));

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)palmImageView.getLayoutParams();
                params.width = (int)radius * 2;
                params.height = (int)radius * 2;
                palmImageView.setLayoutParams(params);
            }
        });
    }

    public static void tintViewBackground(View view, int tintColor) {
        Drawable wrapDrawable = DrawableCompat.wrap(view.getBackground());
        DrawableCompat.setTint(wrapDrawable, tintColor);
        view.setBackground(wrapDrawable);
    }

    @Override
    protected void onResume() {
        super.onResume();

        PalmAPI.testMatch(this, userName);

        if (isRightPalm) {
            PalmAPI.m_PalmBiometrics.ExtractModelMask(SharedPreferenceHelper.getSavedPalmId(SharedPreferenceHelper.RIGHT_PALM_ID_KEY, userName));
            titleTextView.setText(R.string.right_palm_title);

        } else {
            PalmAPI.m_PalmBiometrics.ExtractModelMask(SharedPreferenceHelper.getSavedPalmId(SharedPreferenceHelper.LEFT_PALM_ID_KEY, userName));
            titleTextView.setText(R.string.left_palm_title);
        }

        Single.just(new PalmModelMaskMessage())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap(new Function<PalmMessage, SingleSource<Bitmap>>()
                {
                    @Override
                    public SingleSource<Bitmap> apply(PalmMessage message) throws Exception
                    {
                        PalmMessage tempMessage;
                        for (;;) {
                            tempMessage = PalmAPI.m_PalmBiometrics.WaitMessage();
                            if (tempMessage instanceof PalmModelMaskMessage) {
                                break;
                            }
                        }

                        return Single.just(getBitmap(((PalmModelMaskMessage)tempMessage).image));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap value) {
                        palmImageView.setImageDrawable(new BitmapDrawable(getResources(), value));
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("LOG", "INIT ERROR", e);
                        Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Bitmap getBitmap(PalmImage image) {
        Bitmap                 img  = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888);
        byte[] data = image.data;
        int    i    = 0;
        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                int r = data[i++];
                int g = data[i++];
                int b = data[i++];
                int rgb = 0xFF000000 | (r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8;
                img.setPixel(x, y, rgb);
            }
        }

        return img;
    }

    public void onClearClick() {
        if (isRightPalm) {
            SharedPreferenceHelper.setRightPalmEnabled(false, userName);
            startActivity(AuthActivity.getIntentForRightPalm(this, userName));
        } else {
            SharedPreferenceHelper.setLeftPalmEnabled(false, userName);
            startActivity(AuthActivity.getIntentForLeftPalm(this, userName));
        }
    }

    @Override
    public void onBackPressed()
    {
        if (isNavNeeded) {
            SharedPreferenceHelper.setRightPalmEnabled(false, userName);
            SharedPreferenceHelper.setLeftPalmEnabled(false, userName);

            Intent intent = new Intent();
            setResult(ON_CLOSE_RESULT_CODE, intent);

            finish();
        }
    }
}
