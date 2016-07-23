package com.github.phvogt.pscratchpad.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.text.InputType;

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
    public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Load the preferences from an XML resource
	    addPreferencesFromResource(R.xml.user_prefs);
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	    initSummary(getPreferenceScreen());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
	    updatePrefSummary(findPreference(key));
	}

	/**
	 * Set value of all summaries.
	 * 
	 * @param p
	 *            preference
	 */
	private void initSummary(final Preference p) {
	    if (p instanceof PreferenceGroup) {
		final PreferenceGroup pGrp = (PreferenceGroup) p;
		for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
		    initSummary(pGrp.getPreference(i));
		}
	    } else {
		updatePrefSummary(p);
	    }
	}

	/**
	 * Update the summary of the preference.
	 * 
	 * @param p
	 *            preference
	 */
	private void updatePrefSummary(final Preference p) {
	    if (p instanceof ListPreference) {
		final ListPreference listPref = (ListPreference) p;
		p.setSummary(listPref.getEntry());
	    }
	    if (p instanceof EditTextPreference) {
		final EditTextPreference editTextPref = (EditTextPreference) p;
		if ((((EditTextPreference) p).getEditText().getInputType()
			& InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
		    final String value = editTextPref.getText();
		    String summary = getResources().getString(R.string.label_settings_summary_password);
		    if (value == null || "".equals(value)) {
			summary = getResources().getString(R.string.label_settings_summary_empty);
		    }
		    p.setSummary(summary);
		} else {
		    final String value = editTextPref.getText();
		    String summary = value;
		    if (value == null || "".equals(value)) {
			summary = getResources().getString(R.string.label_settings_summary_empty);
		    }
		    p.setSummary(summary);
		}
	    }
	    if (p instanceof MultiSelectListPreference) {
		final EditTextPreference editTextPref = (EditTextPreference) p;
		p.setSummary(editTextPref.getText());
	    }
	}

    }
}
