package com.zwsb.palmsdk.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.zwsb.palmsdk.helpers.BaseUtil;

public class GradientView extends ImageView
{
	private Path     circlePath;

	public GradientView(Context context)
	{
		super(context);
		init();
	}

	public GradientView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public GradientView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
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
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.save();
		canvas.clipPath(circlePath);
		super.onDraw(canvas);
		canvas.restore();
	}
}

