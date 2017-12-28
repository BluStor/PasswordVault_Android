package com.zwsb.palmsdk.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zwsb.palmsdk.R;

/**
 * Created by svyatozar on 01.07.17.
 */

public class PinInputView extends FrameLayout
{
	TextView  button1;
	TextView  button2;
	TextView  button3;
	TextView  button4;
	TextView  button5;
	TextView  button6;
	TextView  button7;
	TextView  button8;
	TextView  button9;
	TextView  cap;
	TextView  button0;
	ImageView buttonRemove;

	private String pinString = "";
	private PinObserver pinObserver;

	public PinInputView(Context context)
	{
		super(context);
		init();
	}

	public PinInputView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public PinInputView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init()
	{
		View rootView = inflate(getContext(), R.layout.view_pin_input, this);
		button1 = (TextView) rootView.findViewById(R.id.button1);
		button2 = (TextView) rootView.findViewById(R.id.button2);
		button3 = (TextView) rootView.findViewById(R.id.button3);
		button4 = (TextView) rootView.findViewById(R.id.button4);
		button5 = (TextView) rootView.findViewById(R.id.button5);
		button6 = (TextView) rootView.findViewById(R.id.button6);
		button7 = (TextView) rootView.findViewById(R.id.button7);
		button8 = (TextView) rootView.findViewById(R.id.button8);
		button9 = (TextView) rootView.findViewById(R.id.button9);
		button0 = (TextView) rootView.findViewById(R.id.button0);

		cap = (TextView) rootView.findViewById(R.id.cap);
		buttonRemove = (ImageView) rootView.findViewById(R.id.buttonRemove);

		button1.setOnClickListener(onClickListener);
		button2.setOnClickListener(onClickListener);
		button3.setOnClickListener(onClickListener);
		button4.setOnClickListener(onClickListener);
		button5.setOnClickListener(onClickListener);
		button6.setOnClickListener(onClickListener);
		button7.setOnClickListener(onClickListener);
		button8.setOnClickListener(onClickListener);
		button9.setOnClickListener(onClickListener);
		button0.setOnClickListener(onClickListener);

		cap.setOnClickListener(onClickListener);
		buttonRemove.setOnClickListener(onClickListener);
	}

	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			onDigitClick(v);
		}
	};

	public void setPinObserver(PinObserver observer) {
		this.pinObserver = observer;
	}

	public void clear() {
		pinString = "";
	}

	public void onDigitClick(View view) {
		if (view.getId() == R.id.buttonRemove) {
			if (pinString.length() != 0) {
				pinString = pinString.substring(0, pinString.length()-1);
			}
		} else {
			pinString+= ((TextView)view).getText().toString();
		}

		if (pinObserver != null) {
			pinObserver.onPinChanged(pinString);
		}
	}

	public interface PinObserver {
		void onPinChanged(String pin);
	}
}
