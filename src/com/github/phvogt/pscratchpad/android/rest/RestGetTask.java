package com.github.phvogt.pscratchpad.android.rest;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.SSLHandshakeException;

import com.github.phvogt.pscratchpad.android.IConstants;
import com.github.phvogt.pscratchpad.android.MainActivity;
import com.github.phvogt.pscratchpad.android.R;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

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
    protected RestData doInBackground(final String... arguments) {

	// get preferences
	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_parentActivity);
	final String prefsUrl = prefs.getString(_parentActivity.getResources().getString(R.string.setting_url), null);
	if (prefsUrl == null) {
	    _parentActivity.showMessage(_parentActivity.getResources().getString(R.string.message_url_not_set));
	    return null;
	}
	final String prefsListname = prefs.getString(_parentActivity.getResources().getString(R.string.setting_name),
		null);
	if (prefsListname == null) {
	    _parentActivity.showMessage(_parentActivity.getResources().getString(R.string.message_name_not_set));
	    return null;
	}
	final String passphrase = prefs.getString(_parentActivity.getResources().getString(R.string.setting_passphrase),
		null);

	// read data
	final RestData result = readData(prefsUrl, prefsListname, passphrase);

	return result;
    }

    /**
     * Read data from URL.
     * 
     * @param url
     *            URL
     * @param listname
     *            list name
     * @param passphrase
     *            passphrase
     * @return RestData
     */
    private RestData readData(final String url, final String listname, final String passphrase) {

	final String methodname = "readData(): ";

	RestData result = null;
	try {

	    // setup connection
	    setAuthenticator();
	    final URL restUrl = new URL(url + IConstants.REST_GET + listname);
	    final HttpURLConnection urlConnection = RestHelper.setupConnectionGet(restUrl);

	    // read and process data
	    Log.i(IConstants.LOG_TAG, methodname + "got response code = " + urlConnection.getResponseCode());
	    final String serverResult = RestHelper.readResponse(urlConnection);
	    Log.d(IConstants.LOG_TAG, methodname + "loaded: " + serverResult);
	    result = RestData.createRestData(serverResult, passphrase);

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
    protected void onPostExecute(final RestData result) {

	final EditText editor = (EditText) _parentActivity.findViewById(R.id.editTextEditor);
	final Button btnLoad = (Button) _parentActivity.findViewById(R.id.btn_load);
	final Button btnSave = (Button) _parentActivity.findViewById(R.id.btn_save);
	btnLoad.setEnabled(true);
	btnSave.setEnabled(true);

	// no error
	if (_error == null) {
	    if (result != null && result.getData() != null) {
		// convert any windows carriage return / linefeeds to linefeeds
		// only
		final String editorText = result.getData().replaceAll("\\r\\n", "\n");
		editor.setText(editorText);
	    }
	    _parentActivity.setLastChange(result.getLastChange());
	} else {
	    // handle error
	    String errorMsg = null;
	    if (_error instanceof SSLHandshakeException) {
		errorMsg = _parentActivity.getResources().getString(R.string.error_rest_SSL);
	    } else if (_alreadyAuthenticated) {
		errorMsg = _parentActivity.getResources().getString(R.string.error_rest_authentication);
	    } else {
		errorMsg = _parentActivity.getResources().getString(R.string.error_rest_general) + " "
			+ _error.getLocalizedMessage();
	    }
	    _parentActivity.showMessage(errorMsg);
	}
    }

}
