package com.zwsb.palmsdk.customViews;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.View;
import com.zwsb.palmsdk.helpers.BaseUtil;
import com.zwsb.palmsdk.helpers.Constant;

import com.zwsb.palmsdk.R;

public class ScanView extends View {

    private static final int DEFAULT_SCAN_RADIUS = 200;

    private static final int DEFAULT_TEXTSIZE = 30;

    private static final int LARGER_TEXTSIZE = 80;

    private Path circlePath;

    private Paint paint;

    private Paint txtPaint;

    private Paint backgroundPaint;

    private int frameColor;
    private int gestureColor;

    private int borderCircleSuccessColor;
    private int circleSuccessColor;

    private int textColor;

    private int textSize = DEFAULT_TEXTSIZE;

    private float scanX, scanY, scanR;

    private float gestureX, gestureY, gestureR;

    private Resources resource;

    public boolean isDraw;

    private boolean isSuccess = false;
    private boolean isClearNeeded = false;

    private boolean isMatchSuccess = false;

    private float mCircleScale = 1.f;
    public float centerX;
    public float centerY;

    public ScanView(Context context) {
        super(context);
        init();
    }

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new  Paint();
        txtPaint = new  Paint();
        resource = getResources();
        frameColor = resource.getColor(R.color.scan_green_out);
        gestureColor = resource.getColor(R.color.gesture_green);

        borderCircleSuccessColor = resource.getColor(R.color.borderCircleSuccessColor);
        circleSuccessColor = resource.getColor(R.color.circleSuccessColor);

        textColor = resource.getColor(R.color.white);
        txtPaint.setStyle(Paint.Style.FILL);
        txtPaint.setTextSize(textSize);
        txtPaint.setColor(textColor);
        txtPaint.setTextAlign(Paint.Align.CENTER);
        backgroundPaint = new  Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(getResources().getColor(R.color.white_blur_color));
        isDraw = true;
        circlePath = new  Path();
        circlePath.setFillType(Path.FillType.INVERSE_WINDING);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        circlePath.reset();
        float radius = getWidth() * 4 / 9 * mCircleScale;
        radius = (radius - (radius * BaseUtil.decreaseCoefficient));
        circlePath.addCircle(getMeasuredWidth() / 2f, getMeasuredHeight() / 2f, radius, Path.Direction.CCW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Constant.VideoAction action = Constant.VideoAction.NEWUSER;

        drawCenterScanDomain(canvas);

        if (!isClearNeeded) {
            if (isSuccess) {
                switch(action) {
                    case NEWUSER:
                        drawRegisterSuccess(canvas);
                    case RETAKE:
                        drawRegisterSuccess(canvas);
                        break;
                    case INVALIDATE:
                        if (isMatchSuccess) {
                            drawMatchSuccess(canvas);
                        } else {
                            drawMatchFailed(canvas);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                if (isDraw)
                    drawGestureDomain(canvas);
                else {
                    drawMatchFailed(canvas);
                }
            }
        }
    }

    public void clear(boolean isClearNeeded) {
        this.isClearNeeded = isClearNeeded;
        invalidate();
    }

    public void disableClear() {
        this.isClearNeeded = false;
    }

    private void drawCenterScanDomain(Canvas canvas) {
        if (isDraw) {
            scanX = getWidth() / 2;
            scanY = getHeight() / 2;
            scanR = getWidth() * 4 / 9 * mCircleScale;
            scanR = (scanR - (scanR * BaseUtil.decreaseCoefficient));
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(frameColor);
            paint.setStrokeWidth(8);
            paint.setAntiAlias(true);
            canvas.drawCircle(scanX, scanY, scanR, paint);
        }
    }

    private void drawGestureDomain(Canvas canvas) {
        if (gestureX > getWidth() || gestureY > getHeight()) {
            return;
        }

        if (gestureR != 0) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(gestureColor);
            paint.setAntiAlias(true);
            canvas.drawCircle(gestureX, gestureY, gestureR * mCircleScale, paint);
        }
    }

    private void drawRegisterSuccess(Canvas canvas) {
        float scanR = (getWidth() * 4 / 9 * mCircleScale) - getResources().getDimensionPixelSize(R.dimen.activityDoubleMargin);

        float scanX = getWidth() / 2;
        float scanY = getHeight() / 2;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(circleSuccessColor);
        paint.setAntiAlias(true);
        canvas.drawCircle(scanX, scanY, scanR, paint);

        paint.setColor(borderCircleSuccessColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        canvas.drawCircle(scanX, scanY, scanR, paint);
    }

    private void drawMatchSuccess(Canvas canvas) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.circle_positive);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, getWidth() * 2 / 3, getWidth() * 2 / 3);
        canvas.drawBitmap(bitmap, getWidth() / 2, (getHeight() / 2) + 200, paint);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private void drawMatchFailed(Canvas canvas) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.circle_negative);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, getWidth() * 2 / 3, getWidth() * 2 / 3);
        canvas.drawBitmap(bitmap, getWidth() / 2, getHeight() / 2, paint);
        txtPaint.setTextSize(LARGER_TEXTSIZE);
        canvas.drawText("SORRY", getWidth() / 2, getHeight() / 2, txtPaint);
        txtPaint.setTextSize(DEFAULT_TEXTSIZE);
        canvas.drawText("BAD USER", getWidth() / 2, getHeight() / 2 + 35, txtPaint);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public void reDrawGesture(float gestureX, float gestureY, float gestureR) {
        isDraw = true;
        float heightDifference = (BaseUtil.screenHeight - getHeight()) / 2;
        this.gestureX = gestureX;
        this.gestureY = gestureY - heightDifference;
        this.gestureR = gestureR;
        this.postInvalidate();
    }

    public void setSuccess(boolean flag) {
        isSuccess = flag;
        isDraw = flag;
        this.postInvalidate();
    }

    public void setSuccess(boolean flag, boolean isMatch) {
        isSuccess = flag;
        isMatchSuccess = isMatch;
        this.postInvalidate();
    }
}
