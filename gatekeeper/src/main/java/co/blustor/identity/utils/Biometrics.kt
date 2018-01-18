package co.blustor.identity.utils

import android.content.Context
import android.content.Context.FINGERPRINT_SERVICE
import android.hardware.fingerprint.FingerprintManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class Biometrics(val context: Context) {

    companion object {
        const val keyStoreAlias = "gatekeeper"
        const val keyStoreProvider = "AndroidKeyStore"
        val keyStore: KeyStore = KeyStore.getInstance(keyStoreProvider)
    }

    private val fingerprintManager = context.getSystemService(FINGERPRINT_SERVICE) as FingerprintManager

    init {
        keyStore.load(null)
    }

    fun getFingerprint(): String {
        return ""
    }

    fun deleteFingerprint(): Boolean {
        return false
    }

    fun hasFingerprint(): Boolean {
        return false
    }

    fun isFingerprintAvailable(): Boolean {
        return fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()
    }

    fun setFingerprint(password: String) {
        // Generate key

        val keyPurposes = KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(keyStoreAlias, keyPurposes)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC).setUserAuthenticationRequired(true)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build()

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keyStoreProvider)
        keyGenerator.init(keyGenParameterSpec)

        val secretKey = keyGenerator.generateKey()

        val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val authenticationCallback = object : FingerprintManager.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {

            }

            override fun onAuthenticationFailed() {

            }

            override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {

            }

            override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {

            }
        }
    }

    fun getPalm(): String {
        return ""
    }
}