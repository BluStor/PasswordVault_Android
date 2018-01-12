package co.blustor.identity.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import co.blustor.identity.vault.Vault

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val address = Vault.getCardAddress(this)

        if (address == null) {
            val chooseActivity = Intent(this, ChooseActivity::class.java)
            startActivity(chooseActivity)
        } else {
            val splashActivity = Intent(this, SplashActivity::class.java)
            startActivity(splashActivity)
        }

        finish()
    }
}
