// (c) 2014 by Philipp Vogt
package com.github.phvogt.pscratchpad.android;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;

import com.github.phvogt.pscratchpad.android.utils.RestHelper;

/**
 * Rest task for get.
 */
public class RestSaveTask extends RestTask {

    /** contains errors. */
    private Exception _error = null;

    /**
     * Constructor.
     * @param parentActivity parent activity
     */
    public RestSaveTask(final MainActivity parentActivity) {
        super(parentActivity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String doInBackground(final String... arguments) {

        final String methodname = "doInBackground(): ";

        final String data = createEncryptedJSON(arguments[0]);

        String result = null;
        try {

            setAuthenticator();

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_parentActivity);
            final String prefsUrl = prefs.getString(_parentActivity.getResources().getString(R.string.setting_url), null);
            if (prefsUrl == null) {
                _parentActivity.showMessage(_parentActivity.getResources().getString(R.string.message_url_not_set));
                return null;
            }
            final String prefsListname = prefs.getString(_parentActivity.getResources().getString(R.string.setting_name), null);
            if (prefsListname == null) {
                _parentActivity.showMessage(_parentActivity.getResources().getString(R.string.message_name_not_set));
                return null;
            }

            final URL url = new URL(prefsUrl + IConstants.REST_SAVE + prefsListname);
            final HttpURLConnection urlConnection;
            if ("https".equalsIgnoreCase(url.getProtocol())) {
                urlConnection = (HttpsURLConnection) url.openConnection();
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            RestHelper.sendData(urlConnection, data);
            urlConnection.connect();

            Log.i(IConstants.LOG_TAG, methodname + "got response code = " + urlConnection.getResponseCode());
            final String serverResult = RestHelper.readResponse(urlConnection);

            Log.d(IConstants.LOG_TAG, methodname + "loaded: " + serverResult);
            final JSONObject resultJSON = new JSONObject(serverResult);
            result = resultJSON.getString(IConstants.REST_FIELD_STATUS);

        } catch (final Exception e) {
            Log.i(IConstants.LOG_TAG, methodname + "error occurred", e);
            _error = e;
        }

        return result;
    }

    /**
     * Creates a JSON with encrypted data.
     * @param data data
     * @return JSON as String
     */
    private String createEncryptedJSON(final String data) {

        final String methodname = "createEncryptedJSON(): ";

        String result = data;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_parentActivity);
        final String passphrase = prefs.getString(_parentActivity.getResources().getString(R.string.setting_passphrase), null);

        if (!TextUtils.isEmpty(passphrase)) {

            JSONObject encryptedData;
            try {
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

            } catch (final Exception e) {
                Log.i(IConstants.LOG_TAG, methodname + "error occurred", e);
                _error = e;
            }
        }

        Log.i(IConstants.LOG_TAG, methodname + "result = " + result);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(final String result) {

        final Button btnLoad = (Button) _parentActivity.findViewById(R.id.btn_load);
        final Button btnSave = (Button) _parentActivity.findViewById(R.id.btn_save);
        btnLoad.setEnabled(true);
        btnSave.setEnabled(true);
        // no error
        if (_error == null) {
            if (IConstants.REST_STATUS_OK.equals(result)) {
                _parentActivity.showMessage(_parentActivity.getResources().getString(R.string.message_rest_save_ok));
            } else {
                _parentActivity.showMessage(_parentActivity.getResources().getString(R.string.message_rest_save_not_ok));
            }
        } else {
            // handle error
            String errorMsg = null;
            if (_error instanceof SSLHandshakeException) {
                errorMsg = _parentActivity.getResources().getString(R.string.error_rest_SSL);
            } else if (_authenticationError) {
                errorMsg = _parentActivity.getResources().getString(R.string.error_rest_authentication);
            } else {
                errorMsg = _parentActivity.getResources().getString(R.string.error_rest_general) + _error.getLocalizedMessage();
            }
            _parentActivity.showMessage(errorMsg);
        }
    }

}
