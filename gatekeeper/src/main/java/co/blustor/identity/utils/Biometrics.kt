package co.blustor.identity.utils

import android.app.Activity
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import android.util.Base64
import android.util.Log
import co.blustor.identity.fragments.FingerprintDialogFragment
import com.zwsb.palmsdk.helpers.SharedPreferenceHelper
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.security.InvalidAlgorithmParameterException
import java.security.Key
import java.security.KeyStore
import java.security.KeyStoreException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class Biometrics(private val activity: Activity) {

    companion object {
        private const val tag = "Biometrics"
        private const val cipherTransformation = "$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$ENCRYPTION_PADDING_PKCS7"
        private const val keyAliasFingerprint = "gatekeeper-fingerprint"
        private const val keyAliasPalm = "gatekeeper-palm"
        private const val keyStoreProvider = "AndroidKeyStore"
        private const val preferenceAliasFingerprintEncoded = "fingerprint-encoded"
        private const val preferenceAliasFingerprintIv = "fingerprint-iv"
        private const val preferenceAliasPalmEncoded = "palm-encoded"
        private const val preferenceAliasPalmIv = "palm-iv"
        private val keyStore: KeyStore = KeyStore.getInstance(keyStoreProvider)
        const val palmUsername = "left"
    }

    enum class AuthType {
        NONE, FINGERPRINT, PALM
    }

    private val fingerprintManager = activity.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
    private val sharedPreferences = activity.getSharedPreferences("biometrics", Context.MODE_PRIVATE)

    val enrolledAuthType: AuthType
        get() = when {
            hasFingerprint() -> AuthType.FINGERPRINT
            hasPalm() -> AuthType.PALM
            else -> AuthType.NONE
        }

    init {
        keyStore.load(null)
    }

    private fun getEncryptCipher(key: Key): Cipher {
        val cipher = Cipher.getInstance(cipherTransformation)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    private fun getDecryptCipher(key: Key, ivParameterSpec: IvParameterSpec): Cipher {
        val cipher = Cipher.getInstance(cipherTransformation)
        cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec)
        return cipher
    }

    private fun createSecretKey(keyStoreAlias: String, userAuthenticationRequired: Boolean): SecretKey? {
        val keyPurposes = PURPOSE_DECRYPT or PURPOSE_ENCRYPT
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(keyStoreAlias, keyPurposes)
            .setBlockModes(BLOCK_MODE_CBC).setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(userAuthenticationRequired).build()

        return try {
            val keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, keyStoreProvider)
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        } catch (e: InvalidAlgorithmParameterException) {
            null
        }
    }

    private fun getKey(keyStoreAlias: String): Key? {
        return try {
            keyStore.getKey(keyStoreAlias, null)
        } catch (e: KeyStoreException) {
            null
        }
    }

    private fun createEncryptCryptoObject(key: Key): FingerprintManager.CryptoObject {
        val cipher = getEncryptCipher(key)
        return FingerprintManager.CryptoObject(cipher)
    }

    private fun createDecryptCryptoObject(key: Key, ivParameterSpec: IvParameterSpec): FingerprintManager.CryptoObject {
        val cipher = getDecryptCipher(key, ivParameterSpec)
        return FingerprintManager.CryptoObject(cipher)
    }

    private fun decrypt(encoded: String, cryptoObject: FingerprintManager.CryptoObject): String {
        val byteArrayInputStream = ByteArrayInputStream(b64Decode(encoded))
        val cipherInputStream = CipherInputStream(byteArrayInputStream, cryptoObject.cipher)

        val bufferedReader = BufferedReader(InputStreamReader(cipherInputStream))
        return bufferedReader.readLine()
    }

    private fun b64Decode(str: String): ByteArray {
        return Base64.decode(str, Base64.DEFAULT)
    }

    private fun base64Encode(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun encrypt(cryptoObject: FingerprintManager.CryptoObject, value: String): String {
        val encryptedBytes = cryptoObject.cipher.doFinal(value.toByteArray())
        return base64Encode(encryptedBytes)
    }

    fun getFingerprint(callback: (password: String?) -> Unit) {
        val iv = sharedPreferences.getString(preferenceAliasFingerprintIv, "")
        val encoded = sharedPreferences.getString(preferenceAliasFingerprintEncoded, "")

        val ivParameterSpec = IvParameterSpec(b64Decode(iv))

        val key = getKey(keyAliasFingerprint)

        if (key == null) {
            callback(null)
        } else {
            val cryptoObject = createDecryptCryptoObject(key, ivParameterSpec)

            val fingerprintDialogFragment = FingerprintDialogFragment()
            fingerprintDialogFragment.show(activity.fragmentManager, "fingerprintDialog")

            fingerprintManager.authenticate(cryptoObject, fingerprintDialogFragment.cancellationSignal, 0, object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    Log.d(tag, "onAuthenticationError: ($errorCode, $errString)")
                    fingerprintDialogFragment.dismiss()
                }

                override fun onAuthenticationFailed() {
                    Log.d(tag, "onAuthenticationFailed")
                    fingerprintDialogFragment.dismiss()
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                    Log.d(tag, "onAuthenticationHelp: ($helpCode, $helpString)")
                    fingerprintDialogFragment.dismiss()
                }

                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
                    Log.d(tag, "onAuthenticationSucceeded")
                    fingerprintDialogFragment.dismiss()
                    result?.let {
                        val password = decrypt(encoded, result.cryptoObject)
                        callback(password)
                    } ?: run {
                        Log.e(tag, "onAuthenticationSucceeded: result was null")
                    }
                }
            }, null)
        }
    }

    fun deleteFingerprint(): Boolean {
        val removedPreference = sharedPreferences.edit()
            .remove(preferenceAliasPalmIv)
            .remove(preferenceAliasFingerprintEncoded)
            .commit()

        keyStore.deleteEntry(keyAliasFingerprint)
        val removedKey = keyStore.containsAlias(keyAliasFingerprint)

        return removedPreference && removedKey
    }

    fun hasFingerprint(): Boolean {
        val hasIv = sharedPreferences.contains(preferenceAliasFingerprintIv)
        val hasEncoded = sharedPreferences.contains(preferenceAliasFingerprintEncoded)
        val hasKey = keyStore.containsAlias(keyAliasFingerprint)

        return hasIv && hasEncoded && hasKey
    }

    fun isFingerprintHardwareAvailable(): Boolean {
        return fingerprintManager.isHardwareDetected
    }

    fun isFingerprintUserEnrolled(): Boolean {
        return fingerprintManager.hasEnrolledFingerprints()
    }

    fun setFingerprint(password: String, callback: (e: Exception?) -> Unit) {
        val key = createSecretKey(keyAliasFingerprint, true)
        if (key == null) {
            callback(RuntimeException("Unable to generate secret key."))
        } else {
            val cryptoObject = createEncryptCryptoObject(key)

            val fingerprintDialogFragment = FingerprintDialogFragment()
            fingerprintDialogFragment.show(activity.fragmentManager, "fingerprintDialog")

            fingerprintManager.authenticate(cryptoObject, fingerprintDialogFragment.cancellationSignal, 0, object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    Log.d(tag, "onAuthenticationError: ($errorCode, $errString)")
                    fingerprintDialogFragment.dismiss()
                    callback(null)
                }

                override fun onAuthenticationFailed() {
                    Log.d(tag, "onAuthenticationFailed")
                    fingerprintDialogFragment.dismiss()
                    callback(null)
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                    Log.d(tag, "onAuthenticationHelp: ($helpCode, $helpString)")
                    fingerprintDialogFragment.dismiss()
                    callback(null)
                }

                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
                    Log.d(tag, "onAuthenticationSucceeded")
                    fingerprintDialogFragment.dismiss()
                    result?.let {
                        val encrypted = encrypt(it.cryptoObject, password)
                        val encodedIv = base64Encode(cryptoObject.cipher.iv)

                        sharedPreferences.edit()
                            .putString(preferenceAliasFingerprintEncoded, encrypted)
                            .putString(preferenceAliasFingerprintIv, encodedIv).apply()

                        Log.d(tag, "onAuthenticationSucceeded: stored")
                        callback(null)
                    } ?: run {
                        Log.e(tag, "onAuthenticationSucceeded: result was null")
                    }
                }
            }, null)
        }
    }

    fun deletePalm(): Boolean {
        val removedPreference = sharedPreferences.edit()
            .remove(preferenceAliasPalmIv)
            .remove(preferenceAliasPalmEncoded)
            .commit()

        keyStore.deleteEntry(keyAliasPalm)
        val removedKey = keyStore.containsAlias(keyAliasPalm)

        val users = SharedPreferenceHelper.getStringArray(activity, SharedPreferenceHelper.USER_NAMES_KEY)
        users.remove(Biometrics.palmUsername)

        SharedPreferenceHelper.setStringArray(activity, SharedPreferenceHelper.USER_NAMES_KEY, users)
        SharedPreferenceHelper.setLeftPalmEnabled(false, Biometrics.palmUsername)
        SharedPreferenceHelper.setRightPalmEnabled(false, Biometrics.palmUsername)

        return removedPreference && removedKey
    }

    fun getPalm(callback: (password: String?) -> Unit) {
        val iv = sharedPreferences.getString(preferenceAliasPalmIv, "")
        val encoded = sharedPreferences.getString(preferenceAliasPalmEncoded, "")

        val ivParameterSpec = IvParameterSpec(b64Decode(iv))

        val key = getKey(keyAliasPalm)
        if (key == null) {
            callback(null)
        } else {
            val cryptoObject = createDecryptCryptoObject(key, ivParameterSpec)

            val password = decrypt(encoded, cryptoObject)
            callback(password)
        }
    }

    fun hasPalm(): Boolean {
        val hasEnrolled = SharedPreferenceHelper.getNumberOfRegisteredPalms(activity, palmUsername) > 0
        val hasIv = sharedPreferences.contains(preferenceAliasPalmIv)
        val hasEncoded = sharedPreferences.contains(preferenceAliasPalmEncoded)
        val hasKey = keyStore.containsAlias(keyAliasPalm)

        return hasEnrolled && hasIv && hasEncoded && hasKey
    }

    fun setPalm(password: String, callback: (successful: Boolean) -> Unit) {
        val key = createSecretKey(keyAliasPalm, false)

        if (key == null) {
            callback(false)
        } else {
            val cryptoObject = createEncryptCryptoObject(key)
            val encoded = encrypt(cryptoObject, password)

            val iv = base64Encode(cryptoObject.cipher.iv)

            sharedPreferences.edit()
                .putString(preferenceAliasPalmIv, iv)
                .putString(preferenceAliasPalmEncoded, encoded)
                .apply()

            callback(true)
        }
    }
}