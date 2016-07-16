package com.github.phvogt.pscratchpad.android.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLHandshakeException;

import org.json.JSONException;

import com.github.phvogt.pscratchpad.android.IConstants;
import com.github.phvogt.pscratchpad.android.MainActivity;
import com.github.phvogt.pscratchpad.android.R;
import com.github.phvogt.pscratchpad.android.encryption.EncryptionHelper;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

/**
 * Rest task for get.
 */
public class RestSaveTask extends RestTask {

    /** contains errors. */
    private Exception _error = null;

    /**
     * Constructor.
     * 
     * @param parentActivity
     *            parent activity
     */
    public RestSaveTask(final MainActivity parentActivity) {
	super(parentActivity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RestData doInBackground(final String... arguments) {

	final String methodname = "doInBackground(): ";

	RestData result = null;

	final String data = arguments[0];
	final String lastChange = arguments[1];

	try {

	    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_parentActivity);

	    final String passphrase = prefs
		    .getString(_parentActivity.getResources().getString(R.string.setting_passphrase), null);

	    final String prefsUrl = prefs.getString(_parentActivity.getResources().getString(R.string.setting_url),
		    null);
	    if (prefsUrl == null) {
		_parentActivity.showMessage(_parentActivity.getResources().getString(R.string.message_url_not_set));
		return null;
	    }
	    final String prefsListname = prefs
		    .getString(_parentActivity.getResources().getString(R.string.setting_name), null);
	    if (prefsListname == null) {
		_parentActivity.showMessage(_parentActivity.getResources().getString(R.string.message_name_not_set));
		return null;
	    }

	    setAuthenticator();

	    result = sendData(prefsUrl, prefsListname, data, lastChange, passphrase);

	} catch (final Exception e) {
	    Log.i(IConstants.LOG_TAG, methodname + "error occurred", e);
	    _error = e;
	}

	return result;
    }

    /**
     * Sends the data to the URL.
     * 
     * @param url
     *            URL
     * @param listname
     *            name of list
     * @param data
     *            data
     * @param lastChange
     *            last change
     * @param passphrase
     *            optional passphrase
     * @return RestData
     * @throws IOException
     *             if an error occurred on sending
     * @throws UnsupportedEncodingException
     *             if an error with the encoding happened
     * @throws JSONException
     *             if the JSON could not be created
     * @throws GeneralSecurityException
     *             if an error encrypting data occurs
     */
    private RestData sendData(final String url, final String listname, final String data, final String lastChange,
	    final String passphrase)
	    throws IOException, UnsupportedEncodingException, JSONException, GeneralSecurityException {

	final String methodname = "sendData(): ";

	// prepare connection and data
	final URL restUrl = new URL(url + IConstants.REST_SAVE + listname + "/" + lastChange);
	final HttpURLConnection urlConnection = RestHelper.setupConnectionSave(restUrl);
	String processedData = data;
	if (!TextUtils.isEmpty(passphrase)) {
	    processedData = new EncryptionHelper().encryptData(processedData, passphrase);
	}
	final String saveData = processedData == null ? null : processedData.replaceAll("\\n", "\r\n");

	// send data
	RestHelper.sendData(urlConnection, saveData);
	urlConnection.connect();

	// process response
	Log.i(IConstants.LOG_TAG, methodname + "got response code = " + urlConnection.getResponseCode());
	final String serverResult = RestHelper.readResponse(urlConnection);
	Log.d(IConstants.LOG_TAG, methodname + "loaded: " + serverResult);
	final RestData result = RestData.parseRestData(serverResult);

	return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(final RestData result) {

	final EditText editor = (EditText) _parentActivity.findViewById(R.id.editTextEditor);
	final Button btnLoad = (Button) _parentActivity.findViewById(R.id.btn_load);
	final Button btnSave = (Button) _parentActivity.findViewById(R.id.btn_save);
	btnLoad.setEnabled(true);
	btnSave.setEnabled(true);

	// no error
	if (_error == null) {
	    if (IConstants.REST_STATUS_OK.equals(result.getStatus())) {
		_parentActivity.showMessage(_parentActivity.getResources().getString(R.string.message_rest_save_ok));
	    } else {
		_parentActivity
			.showMessage(_parentActivity.getResources().getString(R.string.message_rest_save_not_ok));
	    }
	    // convert any windows carriage return / linefeeds to linefeeds only
	    if (result != null && result.getData() != null) {
		final String editorText = result.getData().replaceAll("\\r\\n", "\n");
		editor.setText(editorText);
	    }
	    _parentActivity.setLastChange(result.getLastChange());
	} else {
	    // handle error
	    String errorMsg = null;
	    if (_error instanceof SSLHandshakeException) {
		errorMsg = _parentActivity.getResources().getString(R.string.error_rest_SSL);
	    } else if (_authenticationError) {
		errorMsg = _parentActivity.getResources().getString(R.string.error_rest_authentication);
	    } else {
		errorMsg = _parentActivity.getResources().getString(R.string.error_rest_general)
			+ _error.getLocalizedMessage();
	    }
	    _parentActivity.showMessage(errorMsg);
	}
    }

}
