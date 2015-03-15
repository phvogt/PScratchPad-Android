// (c) 2014 by Philipp Vogt
package com.github.phvogt.pscratchpad.android;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.spec.KeySpec;

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
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.github.phvogt.pscratchpad.android.utils.RestHelper;

/**
 * Rest task for get.
 */
public class RestGetTask extends RestTask {

    /** contains errors. */
    private Exception _error = null;

    /**
     * Constructor.
     */
    public RestGetTask(final MainActivity parentActivity) {
        super(parentActivity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String doInBackground(final String... arguments) {

        final String methodname = "doInBackground(): ";

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


        String result = null;
        try {

            setAuthenticator();

            final URL url = new URL(prefsUrl + IConstants.REST_GET + prefsListname);
            final HttpURLConnection urlConnection;
            if ("https".equalsIgnoreCase(url.getProtocol())) {
                urlConnection = (HttpsURLConnection) url.openConnection();
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            urlConnection.connect();

            Log.i(IConstants.LOG_TAG, methodname + "got response code = " + urlConnection.getResponseCode());
            final String serverResult = RestHelper.readResponse(urlConnection);

            Log.d(IConstants.LOG_TAG, methodname + "loaded: " + serverResult);
            result = new JSONObject(serverResult).getString(IConstants.REST_FIELD_DATA);

            final String passphrase = prefs.getString(_parentActivity.getResources().getString(R.string.setting_passphrase), null);

            if (!TextUtils.isEmpty(passphrase)) {
                try {

                    final JSONObject encryptedData = new JSONObject(result);
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
                    result = new String(cipher.doFinal(msg), "UTF-8");
                    Log.i(IConstants.LOG_TAG, methodname + "result = " + result);

                } catch (final JSONException e) {
                    Log.i(IConstants.LOG_TAG, methodname + "could not parse as JSON: " + result);
                }
            }
        } catch (final Exception e) {
            Log.i(IConstants.LOG_TAG, methodname + "error occurred", e);
            _error = e;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(final String result) {
        final EditText editor = (EditText) _parentActivity.findViewById(R.id.editTextEditor);
        final Button btnLoad = (Button) _parentActivity.findViewById(R.id.btn_load);
        final Button btnSave = (Button) _parentActivity.findViewById(R.id.btn_save);
        btnLoad.setEnabled(true);
        btnSave.setEnabled(true);

        // no error
        if (_error == null) {
            editor.setText(result);
        } else {
            // handle error
            String errorMsg = null;
            if (_error instanceof SSLHandshakeException) {
                errorMsg = _parentActivity.getResources().getString(R.string.error_rest_SSL);
            } else if (_alreadyAuthenticated) {
                errorMsg = _parentActivity.getResources().getString(R.string.error_rest_authentication);
            } else {
                errorMsg = _parentActivity.getResources().getString(R.string.error_rest_general) + _error.getLocalizedMessage();
            }
            _parentActivity.showMessage(errorMsg);
        }
    }

}
