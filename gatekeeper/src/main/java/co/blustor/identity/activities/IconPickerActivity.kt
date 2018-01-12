package co.blustor.identity.activities

import android.content.Intent
import android.os.Bundle

import co.blustor.identity.R
import co.blustor.identity.adapters.IconAdapter
import kotlinx.android.synthetic.main.activity_iconpicker.*

class IconPickerActivity : LockingActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iconpicker)

        title = "Select icon"

        gridView.adapter = IconAdapter(this)
        gridView.setOnItemClickListener { _, _, position, _ ->
            val data = Intent()
            data.putExtra("icon", position)
            setResult(RESULT_OK, data)
            finish()
        }
    }
}
