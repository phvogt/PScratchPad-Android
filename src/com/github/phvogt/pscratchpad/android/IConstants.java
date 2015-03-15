// (c) 2014 by Philipp Vogt
package com.github.phvogt.pscratchpad.android;

/**
 * Constants.
 */
public interface IConstants {

    /** tag for logging. */
    String LOG_TAG = "PScratchPad";

    /** REST field for data. */
    String REST_FIELD_DATA = "data";
    /** REST field for status. */
    String REST_FIELD_STATUS = "status";
    /** REST field for message. */
    String REST_FIELD_MESSAGE = "message";

    /** REST-call for get. */
    String REST_GET = "/rest/";

    /** REST-call for save. */
    String REST_SAVE = "/rest/";

    /** Status ok. */
    String REST_STATUS_OK = "OK";
    /** Status error. */
    String REST_STATUS_ERROR = "ERROR";

    /** message. */
    String REST_MESSAGE_OK = "OK";

    /** key for data in preferences */
    String PREFS_DATA_KEY = "data";
    /** key for cursor position in preferences */
    String PREFS_CURSOR_POS_KEY = "cursor_pos";

    /** key size for encryption. */
    int ENCRYPT_KEY_SIZE = 128;
    /** number of iterations for key encryption. */
    int ENCRYPT_KEY_ITER = 5000;
}
