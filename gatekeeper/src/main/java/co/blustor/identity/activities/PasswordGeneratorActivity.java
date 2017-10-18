package co.blustor.identity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.common.primitives.Chars;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import co.blustor.identity.R;

public class PasswordGeneratorActivity extends LockingActivity {
    private static final char[] CHARS_UPPER = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final char[] CHARS_LOWER = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] CHARS_DIGITS = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    private static final char[] CHARS_SPECIAL = {'!', '@', '#', '$', '%', '^', '&', '*'};
    private static final char[] CHARS_BRACKETS = {'[', ']', '{', '}', '(', ')', '<', '>'};

    private final ArrayList<Character> mCharacters = new ArrayList<>();

    private TextView mPasswordTextView = null;
    private CheckBox mUpperCheckbox = null;
    private CheckBox mLowerCheckbox = null;
    private CheckBox mDigitsCheckbox = null;
    private CheckBox mDashCheckbox = null;
    private CheckBox mUnderscoreCheckbox = null;
    private CheckBox mSpaceCheckbox = null;
    private CheckBox mSpecialCheckbox = null;
    private CheckBox mBracketsCheckbox = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwordgenerator);

        setTitle("Generate password");

        // Views

        mPasswordTextView = findViewById(R.id.textview_password);
        mUpperCheckbox = findViewById(R.id.checkbox_upper);
        mLowerCheckbox = findViewById(R.id.checkbox_lower);
        mDigitsCheckbox = findViewById(R.id.checkbox_digits);
        mDashCheckbox = findViewById(R.id.checkbox_dash);
        mUnderscoreCheckbox = findViewById(R.id.checkbox_underscore);
        mSpaceCheckbox = findViewById(R.id.checkbox_space);
        mSpecialCheckbox = findViewById(R.id.checkbox_special);
        mBracketsCheckbox = findViewById(R.id.checkbox_brackets);

        mUpperCheckbox.setChecked(true);
        mLowerCheckbox.setChecked(true);
        mDigitsCheckbox.setChecked(true);

        generateCharacters();

        final TextView lengthTextView = findViewById(R.id.textview_length);

        final SeekBar seekBarLength = findViewById(R.id.seekbar_length);
        seekBarLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i < 10) {
                    seekBar.setProgress(10);
                } else if (i > 200) {
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

        mPasswordTextView.setOnClickListener(v -> {
            String password = generatePassword(seekBarLength.getProgress());
            mPasswordTextView.setText(password);
        });

        CompoundButton.OnCheckedChangeListener checkedChangeListener = (buttonView, isChecked) -> {
            generateCharacters();
            String passsword = generatePassword(seekBarLength.getProgress());
            mPasswordTextView.setText(passsword);
        };

        mUpperCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mLowerCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mDigitsCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mDashCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mUnderscoreCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mSpaceCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mSpecialCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mBracketsCheckbox.setOnCheckedChangeListener(checkedChangeListener);

        TextView upperTextView = findViewById(R.id.textview_upper);
        upperTextView.setOnClickListener(v -> mUpperCheckbox.toggle());
        TextView lowerTextView = findViewById(R.id.textview_lower);
        lowerTextView.setOnClickListener(v -> mLowerCheckbox.toggle());
        TextView digitsTextView = findViewById(R.id.textview_digits);
        digitsTextView.setOnClickListener(v -> mDigitsCheckbox.toggle());
        TextView dashTextView = findViewById(R.id.textview_dash);
        dashTextView.setOnClickListener(v -> mDashCheckbox.toggle());
        TextView underscoreTextView = findViewById(R.id.textview_underscore);
        underscoreTextView.setOnClickListener(v -> mUnderscoreCheckbox.toggle());
        TextView spaceTextView = findViewById(R.id.textview_space);
        spaceTextView.setOnClickListener(v -> mSpaceCheckbox.toggle());
        TextView specialTextView = findViewById(R.id.textview_special);
        specialTextView.setOnClickListener(v -> mSpecialCheckbox.toggle());
        TextView bracketsTextView = findViewById(R.id.textview_brackets);
        bracketsTextView.setOnClickListener(v -> mBracketsCheckbox.toggle());
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
    public boolean onOptionsItemSelected(MenuItem item) {
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
