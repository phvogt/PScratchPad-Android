package com.github.phvogt.pscratchpad.android;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Activity to settings.
 */
public class SettingsActivity extends Activity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    /**
     * Fragment for user settings.
     */
    public static class SettingsFragment extends PreferenceFragment {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Load the preferences from an XML resource
	    addPreferencesFromResource(R.xml.user_prefs);
	}
    }
}
