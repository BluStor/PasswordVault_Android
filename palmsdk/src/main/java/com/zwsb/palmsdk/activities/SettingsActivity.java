package com.zwsb.palmsdk.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.zwsb.palmsdk.R;
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper;

import java.util.List;
import java.util.Set;

@Deprecated
public class SettingsActivity extends AppCompatActivity
{
	public static String IS_BLOCK_NEEDED_KEY = "IS_BLOCK_NEEDED_KEY";
	public static String USER_NAME_KEY       = "USER_NAME_KEY";

	Switch            switchEnabled;
	FrameLayout       lockScreenEnabledLayout;
	AppCompatCheckBox leftPalmAction;
	FrameLayout       leftPalmLayout;
	AppCompatCheckBox rightPalmAction;
	FrameLayout       rightPalmLayout;
	AppCompatButton   doneButton;
	Toolbar toolbar;

	boolean isLeftPalmEnabled;
	boolean isRightPalmEnabled;

	boolean isLockEnabled = true;
	String  userName      = "";

	boolean isFirstBackPressedTime = true;

	/**
	 * @param context current context
	 * @return intent for start SettingsActivity
	 */
	public static Intent getIntent(Context context, String userName)
	{
		Intent intent = new Intent(context, SettingsActivity.class);
		intent.putExtra(USER_NAME_KEY, userName);
		return intent;
	}

	public static Intent getIntent(Context context, boolean isBlockNeeded)
	{
		Intent intent = new Intent(context, SettingsActivity.class);
		intent.putExtra(IS_BLOCK_NEEDED_KEY, isBlockNeeded);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		switchEnabled = (Switch)findViewById(R.id.switchEnabled);

		lockScreenEnabledLayout = (FrameLayout)findViewById(R.id.lockScreenEnabledLayout);
		leftPalmAction = (AppCompatCheckBox)findViewById(R.id.leftPalmAction);
		leftPalmLayout = (FrameLayout)findViewById(R.id.leftPalmLayout);
		rightPalmAction = (AppCompatCheckBox)findViewById(R.id.rightPalmAction);
		rightPalmLayout = (FrameLayout)findViewById(R.id.rightPalmLayout);
		doneButton = (AppCompatButton)findViewById(R.id.doneButton);
		toolbar = (Toolbar)findViewById(R.id.toolbar);

		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onDoneButtonClick();
			}
		});
		rightPalmLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onRightPalmLayoutClick();
			}
		});
		leftPalmLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLeftPalmLayoutClick();
			}
		});

		userName = getIntent().getStringExtra(USER_NAME_KEY);
		isLockEnabled = getIntent().getBooleanExtra(IS_BLOCK_NEEDED_KEY, true);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
		}

		toolbar.setTitle(userName);
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				onBackPressed();
			}
		});
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (SharedPreferenceHelper.getNumberOfRegisteredPalms(SettingsActivity.this, userName) == 2) {
			List<String> users = SharedPreferenceHelper.getStringArray(this, SharedPreferenceHelper.USER_NAMES_KEY);
			if (!users.contains(userName)) {
				users.add(userName);
				SharedPreferenceHelper.setStringArray(this, SharedPreferenceHelper.USER_NAMES_KEY, users);
			}
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		/**
		 * Init controls with palm models availability
		 */

		refreshPalmAvailability();

		switchEnabled.setChecked(SharedPreferenceHelper.isLockEnabled());
		switchEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				SharedPreferenceHelper.setLockEnabled(isChecked);
				isLockEnabled = isChecked;
				checkDoneButton();
			}
		});

		leftPalmAction.setChecked(isLeftPalmEnabled);
		leftPalmAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
				{
					startActivityForTakeLeftPalm();
				}
				else
				{
					SharedPreferenceHelper.setLeftPalmEnabled(false, userName);
				}

				checkDoneButton();
			}
		});
		rightPalmAction.setChecked(isRightPalmEnabled);
		rightPalmAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if (isChecked)
				{
					startActivityForTakeRightPalm();
				}
				else
				{
					SharedPreferenceHelper.setRightPalmEnabled(false, userName);
				}

				checkDoneButton();
			}
		});

		checkDoneButton();
	}

	private void refreshPalmAvailability() {
		isLeftPalmEnabled = SharedPreferenceHelper.isLeftPalmEnabled(SettingsActivity.this, userName);
		isRightPalmEnabled = SharedPreferenceHelper.isRightPalmEnabled(SettingsActivity.this, userName);
	}

	public void onDoneButtonClick()
	{
		if (SharedPreferenceHelper.getNumberOfRegisteredPalms(SettingsActivity.this, userName) == 2)
		{
			finish();
		}
		else
		{
			Toast.makeText(this, getString(R.string.palm_alert), Toast.LENGTH_SHORT).show();
		}
	}

	public void onRightPalmLayoutClick()
	{
		refreshPalmAvailability();

		if (isRightPalmEnabled)
		{
			Intent intent = new Intent(this, PalmActivity.class);
			intent.putExtra(PalmActivity.IS_RIGHT_PALM_NEEDED_KEY, true);
			intent.putExtra(PalmActivity.USER_NAME_KEY, userName);

			startActivity(intent);

		}
		else
		{
			startActivityForTakeRightPalm();
		}
	}

	public void onLeftPalmLayoutClick()
	{
		refreshPalmAvailability();

		if (isLeftPalmEnabled)
		{
			Intent intent = new Intent(this, PalmActivity.class);
			intent.putExtra(PalmActivity.IS_RIGHT_PALM_NEEDED_KEY, false);
			intent.putExtra(PalmActivity.USER_NAME_KEY, userName);

			startActivity(intent);
		}
		else
		{
			startActivityForTakeLeftPalm();
		}
	}

	private void startActivityForTakeLeftPalm()
	{
		Intent intent = new Intent(this, AuthActivity.class);
		intent.putExtra(AuthActivity.USER_ACTION_KEY, AuthActivity.NEW_USER_ACTION);
		intent.putExtra(AuthActivity.IS_RIGHT_PALM_NEEDED_KEY, false);
		intent.putExtra(AuthActivity.USER_NAME_KEY, userName);

		startActivity(intent);
	}

	private void startActivityForTakeRightPalm()
	{
		Intent intent = new Intent(this, AuthActivity.class);
		intent.putExtra(AuthActivity.USER_ACTION_KEY, AuthActivity.NEW_USER_ACTION);
		intent.putExtra(AuthActivity.IS_RIGHT_PALM_NEEDED_KEY, true);
		intent.putExtra(AuthActivity.USER_NAME_KEY, userName);

		startActivity(intent);
	}

	private void checkDoneButton()
	{
		if (SharedPreferenceHelper.getNumberOfRegisteredPalms(SettingsActivity.this, userName) == 2)
		{
			doneButton.getBackground().setColorFilter(Color.parseColor("#3F51B5"), PorterDuff.Mode.MULTIPLY);
		}
		else
		{
			doneButton.getBackground().setColorFilter(Color.parseColor("#E6E6E6"), PorterDuff.Mode.MULTIPLY);
		}
		doneButton.setVisibility(isLockEnabled ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		switch (requestCode)
		{
			case 0:
			{
				if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					//DO SOMETHING
				}
				else
				{
					Toast.makeText(this, R.string.permissions_alert, Toast.LENGTH_SHORT).show();
					finish();
				}
				return;
			}
		}
	}

	@Override
	public void onBackPressed()
	{
		if (isLockEnabled)
		{
			if (SharedPreferenceHelper.getNumberOfRegisteredPalms(SettingsActivity.this, userName) == 2)
			{
				super.onBackPressed();
			}
			else
			{
				if (isFirstBackPressedTime) {
					isFirstBackPressedTime = false;
					Toast.makeText(this, getString(R.string.palm_alert), Toast.LENGTH_SHORT).show();
				} else {
					super.onBackPressed();
				}
			}
		}
		else
		{
			super.onBackPressed();
		}
	}
}
