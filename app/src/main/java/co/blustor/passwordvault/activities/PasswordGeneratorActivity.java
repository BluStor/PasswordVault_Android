package co.blustor.passwordvault.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.primitives.Chars;

import java.util.ArrayList;
import java.util.Random;

import co.blustor.passwordvault.R;

public class PasswordGeneratorActivity extends AppCompatActivity {
    private static final char[] CHARS_UPPER = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    private static final char[] CHARS_LOWER = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    private static final char[] CHARS_DIGITS = {'1','2','3','4','5','6','7','8','9','0'};
    private static final char[] CHARS_SPECIAL = {'!','@','#','$','%','^','&','*'};
    private static final char[] CHARS_BRACKETS = {'[', ']', '{', '}', '(', ')', '<', '>'};
    
    private TextView mPasswordTextView = null;
    private EditText mLengthEditText = null;
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

        // Views

        mPasswordTextView = (TextView) findViewById(R.id.textview_password);

        CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String passsword = generatePassword();
                mPasswordTextView.setText(passsword);
            }
        };

        mLengthEditText = (EditText) findViewById(R.id.edittext_length);
        mLengthEditText.setText("32");
        mLengthEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String password = generatePassword();
                mPasswordTextView.setText(password);
                return true;
            }
        });

        mUpperCheckbox = (CheckBox) findViewById(R.id.checkbox_upper);
        mUpperCheckbox.setChecked(true);
        mUpperCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mLowerCheckbox = (CheckBox) findViewById(R.id.checkbox_lower);
        mLowerCheckbox.setChecked(true);
        mLowerCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mDigitsCheckbox = (CheckBox) findViewById(R.id.checkbox_digits);
        mDigitsCheckbox.setChecked(true);
        mDigitsCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mDashCheckbox = (CheckBox) findViewById(R.id.checkbox_dash);
        mDashCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mUnderscoreCheckbox = (CheckBox) findViewById(R.id.checkbox_underscore);
        mUnderscoreCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mSpaceCheckbox = (CheckBox) findViewById(R.id.checkbox_space);
        mSpaceCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mSpecialCheckbox = (CheckBox) findViewById(R.id.checkbox_special);
        mSpecialCheckbox.setOnCheckedChangeListener(checkedChangeListener);
        mBracketsCheckbox = (CheckBox) findViewById(R.id.checkbox_brackets);
        mBracketsCheckbox.setOnCheckedChangeListener(checkedChangeListener);

        // Initial

        String password = generatePassword();
        mPasswordTextView.setText(password);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_passwordgenerator, menu);
        return super.onCreateOptionsMenu(menu);
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

    private String generatePassword() {
        ArrayList<Character> characters = new ArrayList<>();
        if (mUpperCheckbox.isChecked()) {
            characters.addAll(Chars.asList(CHARS_UPPER));
        }
        if (mLowerCheckbox.isChecked()) {
            characters.addAll(Chars.asList(CHARS_LOWER));
        }
        if (mDigitsCheckbox.isChecked()) {
            characters.addAll(Chars.asList(CHARS_DIGITS));
        }
        if (mDashCheckbox.isChecked()) {
            characters.add('-');
        }
        if (mUnderscoreCheckbox.isChecked()) {
            characters.add('_');
        }
        if (mSpaceCheckbox.isChecked()) {
            characters.add(' ');
        }
        if (mSpecialCheckbox.isChecked()) {
            characters.addAll(Chars.asList(CHARS_SPECIAL));
        }
        if (mBracketsCheckbox.isChecked()) {
            characters.addAll(Chars.asList(CHARS_BRACKETS));
        }

        int length;
        try {
            length = Integer.parseInt(mLengthEditText.getText().toString());
        } catch (NumberFormatException e) {
            length = 32;
        }

        Random random = new Random();
        String password = "";

        for (int i=0; i < length; i++) {
            int item = random.nextInt(characters.size());
            password += characters.get(item);
        }

        return password;
    }
}
