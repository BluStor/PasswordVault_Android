package com.zwsb.palmsdk.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.zwsb.palmsdk.R;
import com.zwsb.palmsdk.helpers.BaseUtil;

/**
 * Created by svyatozar on 27.05.17.
 */

public class CircleAnimationView extends View
{
	private int borderCircleSuccessColor;
	private int circleSuccessColor;

	private int borderCircleFailureColor;
	private int circleFailureColor;

	private Paint     paint;
	private Animation animation;

	private boolean isSuccess = true;

	public CircleAnimationView(Context context)
	{
		super(context);
		init();
	}

	public CircleAnimationView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public CircleAnimationView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		borderCircleSuccessColor = getResources().getColor(R.color.borderCircleSuccessColor);
		circleSuccessColor = getResources().getColor(R.color.circleSuccessColor);
		borderCircleFailureColor = getResources().getColor(R.color.borderCircleFailureColor);
		circleFailureColor = getResources().getColor(R.color.circleFailureColor);
		paint = new Paint();

		animation = AnimationUtils.loadAnimation(getContext(), R.anim.status_circle_animation);
		animation.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation) {
				setVisibility(VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				setVisibility(GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
		animation.setFillAfter(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float scanR = (getWidth() * 4 / 9);
		scanR = (scanR - (scanR * BaseUtil.decreaseCoefficient));

		float scanX = getWidth() / 2;
		float scanY = getHeight() / 2;
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(isSuccess ? circleSuccessColor : circleFailureColor);
		paint.setAntiAlias(true);
		canvas.drawCircle(scanX, scanY, scanR, paint);

		paint.setColor(isSuccess ? borderCircleSuccessColor : borderCircleFailureColor);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(8);
		canvas.drawCircle(scanX, scanY, scanR, paint);
	}

	public void setSuccess(boolean success) {
		isSuccess = success;
		setVisibility(VISIBLE);
		invalidate();
	}
}
