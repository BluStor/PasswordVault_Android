package co.blustor.pwv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.common.primitives.Chars;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import co.blustor.pwv.R;

public class PasswordGeneratorActivity extends LockingActivity {
    private static final char[] CHARS_UPPER = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final char[] CHARS_LOWER = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] CHARS_DIGITS = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    private static final char[] CHARS_SPECIAL = {'!', '@', '#', '$', '%', '^', '&', '*'};
    private static final char[] CHARS_BRACKETS = {'[', ']', '{', '}', '(', ')', '<', '>'};

    private final ArrayList<Character> mCharacters = new ArrayList<>();

    @Nullable
    private TextView mPasswordTextView = null;
    @Nullable
    private CheckBox mUpperCheckbox = null;
    @Nullable
    private CheckBox mLowerCheckbox = null;
    @Nullable
    private CheckBox mDigitsCheckbox = null;
    @Nullable
    private CheckBox mDashCheckbox = null;
    @Nullable
    private CheckBox mUnderscoreCheckbox = null;
    @Nullable
    private CheckBox mSpaceCheckbox = null;
    @Nullable
    private CheckBox mSpecialCheckbox = null;
    @Nullable
    private CheckBox mBracketsCheckbox = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwordgenerator);

        setTitle("Generate password");

        // Views

        mPasswordTextView = (TextView) findViewById(R.id.textview_password);
        mUpperCheckbox = (CheckBox) findViewById(R.id.checkbox_upper);
        mLowerCheckbox = (CheckBox) findViewById(R.id.checkbox_lower);
        mDigitsCheckbox = (CheckBox) findViewById(R.id.checkbox_digits);
        mDashCheckbox = (CheckBox) findViewById(R.id.checkbox_dash);
        mUnderscoreCheckbox = (CheckBox) findViewById(R.id.checkbox_underscore);
        mSpaceCheckbox = (CheckBox) findViewById(R.id.checkbox_space);
        mSpecialCheckbox = (CheckBox) findViewById(R.id.checkbox_special);
        mBracketsCheckbox = (CheckBox) findViewById(R.id.checkbox_brackets);

        mUpperCheckbox.setChecked(true);
        mLowerCheckbox.setChecked(true);
        mDigitsCheckbox.setChecked(true);

        generateCharacters();

        final TextView lengthTextView = (TextView) findViewById(R.id.textview_length);

        final SeekBar seekBarLength = (SeekBar) findViewById(R.id.seekbar_length);
        seekBarLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 10) {
                    seekBar.setProgress(10);
                } else if (progress > 200) {
                    seekBar.setProgress(200);
                }

                String password = generatePassword(seekBar.getProgress());
                mPasswordTextView.setText(password);
                lengthTextView.setText(String.format(Locale.getDefault(), "%d characters", seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarLength.setProgress(32);

        mPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = generatePassword(seekBarLength.getProgress());
                mPasswordTextView.setText(password);
            }
        });

        CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                generateCharacters();
                String passsword = generatePassword(seekBarLength.getProgress());
                mPasswordTextView.setText(passsword);
            }
        };

        mUpperCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mLowerCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mDigitsCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mDashCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mUnderscoreCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mSpaceCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mSpecialCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mBracketsCheckbox.setOnCheckedChangeListener(checkedChangeListener);

        TextView upperTextView = (TextView) findViewById(R.id.textview_upper);
        upperTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpperCheckbox.toggle();
            }
        });
        TextView lowerTextView = (TextView) findViewById(R.id.textview_lower);
        lowerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLowerCheckbox.toggle();
            }
        });
        TextView digitsTextView = (TextView) findViewById(R.id.textview_digits);
        digitsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDigitsCheckbox.toggle();
            }
        });
        TextView dashTextView = (TextView) findViewById(R.id.textview_dash);
        dashTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDashCheckbox.toggle();
            }
        });
        TextView underscoreTextView = (TextView) findViewById(R.id.textview_underscore);
        underscoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUnderscoreCheckbox.toggle();
            }
        });
        TextView spaceTextView = (TextView) findViewById(R.id.textview_space);
        spaceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpaceCheckbox.toggle();
            }
        });
        TextView specialTextView = (TextView) findViewById(R.id.textview_special);
        specialTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpecialCheckbox.toggle();
            }
        });
        TextView bracketsTextView = (TextView) findViewById(R.id.textview_brackets);
        bracketsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBracketsCheckbox.toggle();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_passwordgenerator, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, null);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            Intent data = new Intent();
            data.putExtra("password", mPasswordTextView.getText().toString());
            setResult(RESULT_OK, data);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void generateCharacters() {
        mCharacters.clear();

        if (mUpperCheckbox.isChecked()) {
            mCharacters.addAll(Chars.asList(CHARS_UPPER));
        }
        if (mLowerCheckbox.isChecked()) {
            mCharacters.addAll(Chars.asList(CHARS_LOWER));
        }
        if (mDigitsCheckbox.isChecked()) {
            mCharacters.addAll(Chars.asList(CHARS_DIGITS));
        }
        if (mDashCheckbox.isChecked()) {
            mCharacters.add('-');
        }
        if (mUnderscoreCheckbox.isChecked()) {
            mCharacters.add('_');
        }
        if (mSpaceCheckbox.isChecked()) {
            mCharacters.add(' ');
        }
        if (mSpecialCheckbox.isChecked()) {
            mCharacters.addAll(Chars.asList(CHARS_SPECIAL));
        }
        if (mBracketsCheckbox.isChecked()) {
            mCharacters.addAll(Chars.asList(CHARS_BRACKETS));
        }
    }

    private String generatePassword(int length) {
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int size = mCharacters.size();
            if (size > 0) {
                int item = random.nextInt(size);
                password.append(mCharacters.get(item));
            }
        }

        return password.toString();
    }
}
