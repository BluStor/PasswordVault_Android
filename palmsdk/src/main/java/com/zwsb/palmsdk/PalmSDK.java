package com.zwsb.palmsdk;

import android.content.Context;
import android.content.Intent;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;

import com.redrockbiometrics.palm.PalmStatus;
import com.zwsb.palmsdk.palmApi.PalmAPI;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.zwsb.palmsdk.palmApi.PalmAPI.m_PalmBiometrics;

/**
 * SDK must be initialized with app context
 */
public class PalmSDK {
    protected static String PALM_SDK_KEY_ALIAS = "PALM_SDK_KEY_ALIAS";

    public static Context context;
    protected static KeyStore keystore;

    public static void init(final Context appContext, final String email, final InitSDKCallback callback) {
        context = appContext;

        Single.just(email)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(new Function<String, String>()
                {
                    @Override
                    public String apply(String s) throws Exception
                    {
                        PalmAPI.init(email);
                        return email;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<String>() {
                    @Override
                    public void onSuccess(String value) {
                        if (m_PalmBiometrics != null && m_PalmBiometrics.IsValid()) {
                            callback.onSuccess();
                        } else {
                            callback.onError();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("LOG", "INIT ERROR", e);
                        callback.onError();
                    }
                });
    }

    public static PalmStatus getStatus() {
        if (m_PalmBiometrics != null) {
            return m_PalmBiometrics.Status();
        } else {
            return null;
        }
    }

    /**
     * Loading all keys from keystore, generate a new one, if keystore is void
     */
    private static void initKeyStore() {
        List<String> keyAliases = new ArrayList<>();
        try {
            Enumeration<String> aliases = keystore.aliases();

            while (aliases.hasMoreElements()) {
                keyAliases.add(aliases.nextElement());
            }
        }
        catch(Exception e) {
            Log.e("PalmSDK", "KEYSTORE INIT ERROR", e);
        }

        if (keyAliases.size() == 0) {
            generateKey();
        }
    }

    private static void generateKey() {
        try {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 1);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(PALM_SDK_KEY_ALIAS)
                    .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();

            KeyPairGenerator generator = KeyPairGenerator.getInstance("AES", "AndroidKeyStore");
            generator.initialize(spec);
            generator.generateKeyPair();
        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public interface InitSDKCallback {
        void onSuccess();
        void onError();
    }
}
