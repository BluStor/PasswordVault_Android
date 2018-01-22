package co.blustor.identity.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.SeekBar
import co.blustor.identity.R
import kotlinx.android.synthetic.main.activity_passwordgenerator.*
import java.util.*

class PasswordGeneratorActivity : LockingActivity() {

    private val characters = ArrayList<Char>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passwordgenerator)

        title = "Generate password"

        // Views

        checkBoxUpper.isChecked = true
        checkBoxLower.isChecked = true
        checkBoxDigits.isChecked = true

        generateCharacters()

        seekBarLength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (i < 10) {
                    seekBar.progress = 10
                } else if (i > 200) {
                    seekBar.progress = 200
                }

                val password = generatePassword(seekBar.progress)
                textViewPassword.text = password
                textViewLength.text = String.format(Locale.getDefault(), "%d characters", seekBar.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        seekBarLength.progress = 32

        textViewPassword.setOnClickListener {
            val password = generatePassword(seekBarLength.progress)
            textViewPassword.text = password
        }

        val checkedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ ->
            generateCharacters()
            val password = generatePassword(seekBarLength.progress)
            textViewPassword.text = password
        }

        checkBoxUpper.setOnCheckedChangeListener(checkedChangeListener)
        checkBoxLower.setOnCheckedChangeListener(checkedChangeListener)
        checkBoxDigits.setOnCheckedChangeListener(checkedChangeListener)
        checkBoxDash.setOnCheckedChangeListener(checkedChangeListener)
        checkBoxUnderscore.setOnCheckedChangeListener(checkedChangeListener)
        checkBoxSpace.setOnCheckedChangeListener(checkedChangeListener)
        checkBoxSpecial.setOnCheckedChangeListener(checkedChangeListener)
        checkBoxBrackets.setOnCheckedChangeListener(checkedChangeListener)

        textViewUpper.setOnClickListener { checkBoxUpper.toggle() }
        textViewLower.setOnClickListener { checkBoxLower.toggle() }
        textViewDigits.setOnClickListener { checkBoxDigits.toggle() }
        textViewDash.setOnClickListener { checkBoxDash.toggle() }
        textViewUnderscore.setOnClickListener { checkBoxUnderscore.toggle() }
        textViewSpace.setOnClickListener { checkBoxSpace.toggle() }
        textViewSpecial.setOnClickListener { checkBoxSpecial.toggle() }
        textViewBrackets.setOnClickListener { checkBoxBrackets.toggle() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_passwordgenerator, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED, null)
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_save -> {
                val data = Intent()
                data.putExtra("password", textViewPassword.text.toString())
                setResult(RESULT_OK, data)
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun generateCharacters() {
        characters.clear()

        if (checkBoxUpper.isChecked) {
            characters.addAll(charsUpper)
        }
        if (checkBoxLower.isChecked) {
            characters.addAll(charsLower)
        }
        if (checkBoxDigits.isChecked) {
            characters.addAll(charsDigits)
        }
        if (checkBoxDash.isChecked) {
            characters.add('-')
        }
        if (checkBoxUnderscore.isChecked) {
            characters.add('_')
        }
        if (checkBoxSpace.isChecked) {
            characters.add(' ')
        }
        if (checkBoxSpecial.isChecked) {
            characters.addAll(charsSpecial)
        }
        if (checkBoxBrackets.isChecked) {
            characters.addAll(charsBrackets)
        }
    }

    private fun generatePassword(length: Int): String {
        val random = Random()
        val password = StringBuilder()

        for (i in 0 until length) {
            val size = characters.size
            if (size > 0) {
                val item = random.nextInt(size)
                password.append(characters[item])
            }
        }

        return password.toString()
    }

    companion object {
        private val charsUpper = listOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
        private val charsLower = listOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
        private val charsDigits = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
        private val charsSpecial = listOf('!', '@', '#', '$', '%', '^', '&', '*')
        private val charsBrackets = listOf('[', ']', '{', '}', '(', ')', '<', '>')
    }
}
