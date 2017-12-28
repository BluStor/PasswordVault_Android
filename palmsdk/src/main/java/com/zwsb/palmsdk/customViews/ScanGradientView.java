package com.zwsb.palmsdk.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.zwsb.palmsdk.R;
import com.zwsb.palmsdk.helpers.BaseUtil;

public class ScanGradientView extends View {

	private Drawable gradientDrawable;
	private Path circlePath;

	public ScanGradientView(Context context)
	{
		super(context);
		init();
	}

	public ScanGradientView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public ScanGradientView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		gradientDrawable = getResources().getDrawable(R.drawable.top_preview_gradient);
		circlePath = new  Path();
		circlePath.setFillType(Path.FillType.INVERSE_WINDING);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		circlePath.reset();
		float radius = getWidth() * 4 / 9 * 1;
		radius = (radius - (radius * BaseUtil.decreaseCoefficient));
		circlePath.addCircle(getMeasuredWidth() / 2f, getMeasuredHeight() / 2f, radius, Path.Direction.CCW);
		gradientDrawable.setBounds(0, 0, getWidth(), getHeight());
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.save();
		canvas.clipPath(circlePath);
		gradientDrawable.draw(canvas);
		canvas.restore();
		super.onDraw(canvas);
	}
}
