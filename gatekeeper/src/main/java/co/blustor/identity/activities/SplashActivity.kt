package co.blustor.identity.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import co.blustor.identity.R
import co.blustor.identity.gatekeeper.GKCard
import co.blustor.identity.vault.Vault
import org.jdeferred.DonePipe

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Load

        checkIfDatabaseExists()
    }

    private fun startCreate() {
        val createActivity = Intent(this, CreateActivity::class.java)
        startActivity(createActivity)
        finish()
    }

    private fun startUnlock() {
        val unlockActivity = Intent(this, UnlockActivity::class.java)
        startActivity(unlockActivity)
        finish()
    }

    private fun startChoose() {
        val chooseActivity = Intent(this, ChooseActivity::class.java)
        startActivity(chooseActivity)
        finish()
    }

    private fun checkIfDatabaseExists() {
        val address = Vault.getCardAddress(this)

        if (address != null) {
            try {
                val card = GKCard(address)
                card.checkBluetoothState().then(DonePipe<Void, Void, GKCard.CardException, Void> {
                    card.connect(this)
                }).then(DonePipe<Void, Boolean, GKCard.CardException, Void> {
                    card.exists(Vault.dbPath)
                }).then({ result ->
                    if (result) {
                        startUnlock()
                    } else {
                        startCreate()
                    }
                }).always({ _, _, _ ->
                    card.disconnect()
                }).fail({
                    startChoose()
                })
            } catch (e: GKCard.CardException) {
                startChoose()
            }

        } else {
            startUnlock()
        }
    }
}
