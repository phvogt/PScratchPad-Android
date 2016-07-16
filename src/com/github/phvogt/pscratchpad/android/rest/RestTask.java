package com.github.phvogt.pscratchpad.android.rest;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import com.github.phvogt.pscratchpad.android.IConstants;
import com.github.phvogt.pscratchpad.android.MainActivity;
import com.github.phvogt.pscratchpad.android.R;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * REST task.
 */
public abstract class RestTask extends AsyncTask<String, Void, RestData> {

    /** parent activity for reference. */
    protected final MainActivity _parentActivity;

    /** already authenticated. */
    protected boolean _alreadyAuthenticated = false;
    /** error in authentication. */
    protected boolean _authenticationError = false;

    /**
     * Constructor.
     */
    public RestTask(final MainActivity parentActivity) {
	_parentActivity = parentActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected abstract RestData doInBackground(String... params);

    /**
     * Set an authenticator, if the user configured a username and a password.
     */
    protected void setAuthenticator() {

	final String methodname = "setAuthenticator(): ";

	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_parentActivity);

	final String prefsUrl = prefs.getString(_parentActivity.getResources().getString(R.string.setting_url), null);
	final String prefsUsername = prefs
		.getString(_parentActivity.getResources().getString(R.string.setting_username), null);
	final String prefsPassword = prefs
		.getString(_parentActivity.getResources().getString(R.string.setting_password), null);

	Log.i(IConstants.LOG_TAG, methodname + "setAuthenticator. prefsUrl = " + prefsUrl + " prefsUsername = "
		+ prefsUsername + " prefsPassword.length = " + (prefsPassword == null ? 0 : prefsPassword.length()));

	if (!TextUtils.isEmpty(prefsUsername) && !TextUtils.isEmpty(prefsPassword)) {
	    final Authenticator authenticator = new Authenticator() {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
		    if (!_alreadyAuthenticated) {
			_alreadyAuthenticated = true;
			return new PasswordAuthentication(prefsUsername, prefsPassword.toCharArray());
		    } else {
			_authenticationError = true;
			return null;
		    }
		}

	    };
	    Authenticator.setDefault(authenticator);
	}

    }

}
