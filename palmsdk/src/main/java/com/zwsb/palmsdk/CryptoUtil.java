package com.zwsb.palmsdk;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

/**
 * Util for encrypt and decrypt palm data
 */
public class CryptoUtil {
	public static byte[] encrypt(byte[] data) {
		try {
//			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)PalmSDK.keystore.getEntry(PalmSDK.PALM_SDK_KEY_ALIAS, null);
//			PublicKey                publicKey       = privateKeyEntry.getCertificate().getPublicKey();
//
//			Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//			input.init(Cipher.ENCRYPT_MODE, publicKey);
//
//			return input.doFinal(data);


			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)PalmSDK.keystore.getEntry(PalmSDK.PALM_SDK_KEY_ALIAS, null);
			PublicKey                publicKey       = privateKeyEntry.getCertificate().getPublicKey();

			Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
			input.init(Cipher.ENCRYPT_MODE, publicKey);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, input);
			cipherOutputStream.write(data);
			cipherOutputStream.close();

			byte [] vals = outputStream.toByteArray();
			return vals;
		} catch (Exception e) {
			Log.e("PalmSDK", Log.getStackTraceString(e));
		}

		return new byte[0];
	}

	public static byte[] decrypt(byte[] data) {
		try {
//			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)PalmSDK.keystore.getEntry(PalmSDK.PALM_SDK_KEY_ALIAS, null);
//			PrivateKey               privateKey      = privateKeyEntry.getPrivateKey();
//
//			Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//			output.init(Cipher.DECRYPT_MODE, privateKey);
//
//			return output.doFinal(data);

			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)PalmSDK.keystore.getEntry(PalmSDK.PALM_SDK_KEY_ALIAS, null);
			PrivateKey               privateKey      = privateKeyEntry.getPrivateKey();

			Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
			output.init(Cipher.DECRYPT_MODE, privateKey);

			CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(data), output);
			ArrayList<Byte> values = new ArrayList<>();
			int nextByte;
			while ((nextByte = cipherInputStream.read()) != -1) {
				values.add((byte)nextByte);
			}

			byte[] bytes = new byte[values.size()];
			for(int i = 0; i < bytes.length; i++) {
				bytes[i] = values.get(i).byteValue();
			}

			return bytes;
		} catch (Exception e) {
			Log.e("PalmSDK", Log.getStackTraceString(e));
		}

		return new byte[0];
	}
}
