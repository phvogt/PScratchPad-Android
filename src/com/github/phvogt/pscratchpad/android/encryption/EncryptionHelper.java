package com.github.phvogt.pscratchpad.android.encryption;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.phvogt.pscratchpad.android.IConstants;

import android.util.Log;

/**
 * Helper for encryption.
 */
public class EncryptionHelper {

    /**
     * Decrypt data.
     * 
     * @param data
     *            encryptedData
     * @param passphrase
     *            passphrase to decrypt
     * @return
     * @throws GeneralSecurityException
     * @throws DecoderException
     * @throws UnsupportedEncodingException
     */
    public String decryptData(String data, final String passphrase)
	    throws GeneralSecurityException, DecoderException, UnsupportedEncodingException {

	final String methodname = "encryptData(): ";

	try {

	    final JSONObject encryptedData = new JSONObject(data);
	    final JSONObject keyData = ((JSONObject) encryptedData.get("key"));
	    final String keySaltHex = (String) keyData.get("salt");
	    final int keySize = (Integer) keyData.get("size");
	    final int keyIter = (Integer) keyData.get("iter");
	    final String ivHex = (String) encryptedData.get("iv");
	    final String msg64 = (String) encryptedData.get("ciphertext");
	    // Log.d(IConstants.LOG_TAG, methodname + "passphrase = " +
	    // passphrase);
	    Log.d(IConstants.LOG_TAG, methodname + "keySize    = " + keySize);
	    Log.d(IConstants.LOG_TAG, methodname + "keyIter    = " + keyIter);
	    Log.d(IConstants.LOG_TAG, methodname + "ivHex      = " + ivHex);
	    Log.d(IConstants.LOG_TAG, methodname + "msg64      = " + msg64);

	    final byte[] iv = Hex.decodeHex(ivHex.toCharArray());
	    final byte[] keySalt = Hex.decodeHex(keySaltHex.toCharArray());
	    final byte[] msg = Base64.decodeBase64(msg64.getBytes());

	    final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	    final KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), keySalt, keyIter, keySize);
	    final SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
	    cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
	    data = new String(cipher.doFinal(msg), "UTF-8");
	    Log.i(IConstants.LOG_TAG, methodname + "result = " + data);

	} catch (final JSONException e) {
	    Log.i(IConstants.LOG_TAG, methodname + "could not parse as JSON: " + data);
	}
	return data;
    }

    /**
     * Creates a JSON with encrypted data.
     * 
     * @param data
     *            data
     * @param passphrase
     *            passphrase to use
     * @return JSON as String
     * @throws GeneralSecurityException
     *             encryption error
     * @throws UnsupportedEncodingException
     *             if data encoding fails
     * @throws JSONException
     *             if creating JSON fails
     */
    public String encryptData(final String data, final String passphrase)
	    throws GeneralSecurityException, UnsupportedEncodingException, JSONException {

	final String methodname = "encryptData(): ";

	final String result;
	if (data != null) {

	    JSONObject encryptedData;
	    encryptedData = new JSONObject();

	    final byte[] iv = new byte[16];
	    new Random().nextBytes(iv);
	    final byte[] keySalt = new byte[16];
	    new Random().nextBytes(keySalt);

	    final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	    final KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), keySalt, IConstants.ENCRYPT_KEY_ITER,
		    IConstants.ENCRYPT_KEY_SIZE);
	    final SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
	    cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
	    final byte[] ciphertext = cipher.doFinal(data.getBytes("UTF-8"));

	    encryptedData.put("iv", new String(Hex.encodeHex(iv)));
	    encryptedData.put("ciphertext", new String(Base64.encodeBase64(ciphertext), "UTF-8"));
	    final JSONObject encryptedDataKey = new JSONObject();
	    encryptedDataKey.put("size", IConstants.ENCRYPT_KEY_SIZE);
	    encryptedDataKey.put("iter", IConstants.ENCRYPT_KEY_ITER);
	    encryptedDataKey.put("salt", new String(Hex.encodeHex(keySalt)));

	    encryptedData.put("key", encryptedDataKey);

	    result = encryptedData.toString();
	} else {
	    result = null;
	}

	Log.i(IConstants.LOG_TAG, methodname + "result = " + result);

	return result;
    }

}
