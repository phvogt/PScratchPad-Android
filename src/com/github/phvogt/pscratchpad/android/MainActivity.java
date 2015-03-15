package com.github.phvogt.pscratchpad.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        final String methodname = "onCreate(): ";

        Log.i(IConstants.LOG_TAG, methodname + "start");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        final MainActivity mainActivity = this;

        final EditText editor = (EditText) findViewById(R.id.editTextEditor);
        editor.setTextIsSelectable(true);

        final Button btnLoad = (Button) findViewById(R.id.btn_load);
        final Button btnSave = (Button) findViewById(R.id.btn_save);

        btnLoad.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                btnLoad.setEnabled(false);
                btnSave.setEnabled(false);
                final RestGetTask task = new RestGetTask(mainActivity);
                task.execute();
            }
        });

        btnSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                btnLoad.setEnabled(false);
                btnSave.setEnabled(false);
                final RestSaveTask task = new RestSaveTask(mainActivity);
                task.execute(editor.getEditableText().toString());
            }
        });

        // always show keyboard if editor is clicked
        editor.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                final InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editor, InputMethodManager.SHOW_FORCED);
            }
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {

        // save text editor data in preferences
        final EditText editor = (EditText) findViewById(R.id.editTextEditor);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final Editor prefsEditor = prefs.edit();
        prefsEditor.putString(IConstants.PREFS_DATA_KEY, editor.getEditableText().toString());
        final int selStart = editor.getSelectionStart();
        prefsEditor.putInt(IConstants.PREFS_CURSOR_POS_KEY, selStart);
        prefsEditor.commit();

        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String storedData = prefs.getString(IConstants.PREFS_DATA_KEY, null);
        if (storedData == null) {
            // no stored data so use REST-call
            final Button btnLoad = (Button) findViewById(R.id.btn_load);
            final Button btnSave = (Button) findViewById(R.id.btn_save);
            final MainActivity mainActivity = this;

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // pre load data
                    btnLoad.setEnabled(false);
                    btnSave.setEnabled(false);

                    final RestGetTask task = new RestGetTask(mainActivity);
                    task.execute();
                }
            }, 1000);
        } else {
            // data stored, so load it
            final EditText editor = (EditText) findViewById(R.id.editTextEditor);
            editor.setText(storedData);

            editor.setTextIsSelectable(true);
            editor.setEnabled(true);

            final int cursorPos = prefs.getInt(IConstants.PREFS_CURSOR_POS_KEY, 0);
            editor.setSelection(cursorPos);
        }

        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final String methodname = "onCreateOptionsMenu(): ";

        Log.i(IConstants.LOG_TAG, methodname + "start");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, 0);
            return true;
        case R.id.action_about:
            final Intent intentAbout = new Intent(this, AboutActivity.class);
            startActivity(intentAbout);
            return true;
        default:
            return false;
        }
    }

    /**
     * Shows a message.
     * @param msg message to show.
     */
    public void showMessage(final String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

}
