package com.github.phvogt.pscratchpad.android.rest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.github.phvogt.pscratchpad.android.IConstants;

/**
 * Helper for REST calls.
 */
public class RestHelper {

    /**
     * Constructor.
     */
    private RestHelper() {
	// intentionally blank
    }

    /**
     * Set up the connection to GET data.
     * 
     * @param url
     *            URL
     * @return connection
     * @throws IOException
     *             if an error occurred
     */
    public static HttpURLConnection setupConnectionGet(final URL url) throws IOException {

	final HttpURLConnection urlConnection;

	if ("https".equalsIgnoreCase(url.getProtocol())) {
	    urlConnection = (HttpsURLConnection) url.openConnection();
	} else {
	    urlConnection = (HttpURLConnection) url.openConnection();
	}
	urlConnection.setConnectTimeout(5000);
	urlConnection.connect();

	return urlConnection;
    }

    /**
     * Sets up the connection.
     * 
     * @param url
     *            URL to connect to
     * @return connection
     * @throws IOException
     *             if an error occurred
     */
    public static HttpURLConnection setupConnectionSave(final URL url) throws IOException {

	final HttpURLConnection urlConnection;
	if ("https".equalsIgnoreCase(url.getProtocol())) {
	    urlConnection = (HttpsURLConnection) url.openConnection();
	} else {
	    urlConnection = (HttpURLConnection) url.openConnection();
	}
	urlConnection.setRequestMethod("POST");
	urlConnection.setDoInput(true);
	urlConnection.setDoOutput(true);
	urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
	urlConnection.setConnectTimeout(5000);

	return urlConnection;
    }

    /**
     * Reads the response from the connection.
     * 
     * @param urlConnection
     *            url connection to read from
     * @return the result as String
     * @throws IOException
     *             if an error occurs
     */
    public static String readResponse(final HttpURLConnection urlConnection) throws IOException {

	final BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

	final StringBuffer buf = new StringBuffer();

	final byte[] contents = new byte[1024];
	int bytesRead = 0;
	while ((bytesRead = inputStream.read(contents)) != -1) {
	    buf.append(new String(contents, 0, bytesRead, "UTF-8"));
	}
	inputStream.close();

	final String result = buf.toString();
	return result;
    }

    /**
     * Sends data to output stream of url connection.
     * 
     * @param urlConnection
     *            url connection
     * @param data
     *            data to send
     * @throws IOException
     *             if an error occurs
     * @throws UnsupportedEncodingException
     *             if an error occurs
     */
    public static void sendData(final HttpURLConnection urlConnection, final String data)
	    throws IOException, UnsupportedEncodingException {

	final OutputStream out = urlConnection.getOutputStream();

	final Map<String, String> dataMap = new HashMap<String, String>();
	dataMap.put(IConstants.REST_FIELD_DATA, data);
	final String json = new JSONObject(dataMap).toString();
	out.write(json.getBytes("UTF-8"));
	out.flush();
	out.close();
    }

}
