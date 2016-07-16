package com.github.phvogt.pscratchpad.android.rest;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;

import org.apache.commons.codec.DecoderException;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.phvogt.pscratchpad.android.IConstants;
import com.github.phvogt.pscratchpad.android.encryption.EncryptionHelper;

import android.text.TextUtils;

/**
 * Data for REST.
 */
public class RestData {

    /** status. */
    private final String status;

    /** message. */
    private final String message;

    /** last change. */
    private final Date lastChange;

    /** data. */
    private final String data;

    /**
     * Constructor.
     * 
     * @param status
     *            status
     * @param message
     *            message
     * @param lastChange
     *            last change
     * @param data
     *            data
     */
    public RestData(final String status, final String message, final Date lastChange, final String data) {
	this.status = status;
	this.message = message;
	this.lastChange = lastChange;
	this.data = data;
    }

    /**
     * Get the status.
     * 
     * @return the status
     */
    public String getStatus() {
	return status;
    }

    /**
     * Get the message.
     * 
     * @return the message
     */
    public String getMessage() {
	return message;
    }

    /**
     * Get the lastChange.
     * 
     * @return the lastChange
     */
    public Date getLastChange() {
	return lastChange;
    }

    /**
     * Get the data.
     * 
     * @return the data
     */
    public String getData() {
	return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	final StringBuilder builder = new StringBuilder();
	builder.append("RestData [super = ");
	builder.append(super.toString());
	builder.append(" status=");
	builder.append(status);
	builder.append(", message=");
	builder.append(message);
	builder.append(", lastChange=");
	builder.append(lastChange);
	builder.append(", data=");
	builder.append(data);
	builder.append("]");
	return builder.toString();
    }

    /**
     * Parse the server result.
     * 
     * @param serverResult
     *            result
     * @return RestData
     * @throws JSONException
     *             if the JSON could not be created
     */
    public static RestData parseRestData(final String serverResult) throws JSONException {

	final JSONObject resultJSON = new JSONObject(serverResult);

	final String restStatus = resultJSON.getString(IConstants.REST_FIELD_STATUS);
	final String restMessage = resultJSON.getString(IConstants.REST_FIELD_MESSAGE);
	final String restLastChange = resultJSON.optString(IConstants.REST_FIELD_LASTCHANGE);
	final String restData = resultJSON.optString(IConstants.REST_FIELD_DATA);
	final Date restLastchangeDate = restLastChange.equals("") ? new Date(0)
		: new Date(Long.valueOf(restLastChange));

	final RestData result = new RestData(restStatus, restMessage, restLastchangeDate, restData);

	return result;
    }

    /**
     * Creates the REST data.
     * 
     * @param serverResult
     *            server result
     * @param passphrase
     *            optional passphrase to decode
     * @return RestData
     * @throws JSONException
     *             if creating the JSON failed
     * @throws GeneralSecurityException
     *             an error decrypting the data occurred
     * @throws DecoderException
     *             an error decrypting the data occurred
     * @throws UnsupportedEncodingException
     *             JSON encoding failed
     */
    public static RestData createRestData(final String serverResult, final String passphrase)
	    throws JSONException, GeneralSecurityException, DecoderException, UnsupportedEncodingException {

	String restData = new JSONObject(serverResult).optString(IConstants.REST_FIELD_DATA);
	if (!TextUtils.isEmpty(passphrase)) {
	    restData = new EncryptionHelper().decryptData(restData, passphrase);
	}
	final String restStatus = new JSONObject(serverResult).getString(IConstants.REST_FIELD_STATUS);
	final String restMessage = new JSONObject(serverResult).getString(IConstants.REST_FIELD_MESSAGE);
	final String restLastChange = new JSONObject(serverResult).optString(IConstants.REST_FIELD_LASTCHANGE);
	final Date restLastchangeDate = restLastChange.equals("") ? new Date(0)
		: new Date(Long.valueOf(restLastChange));
	final RestData result = new RestData(restStatus, restMessage, restLastchangeDate, restData);

	return result;
    }
}
