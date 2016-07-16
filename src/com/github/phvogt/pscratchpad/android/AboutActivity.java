package com.github.phvogt.pscratchpad.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity for about information.
 */
public class AboutActivity extends Activity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

	final String methodname = "onCreate(): ";

	Log.i(IConstants.LOG_TAG, methodname + "start");

	super.onCreate(savedInstanceState);

	setContentView(R.layout.activity_about);

    }

}
