// (c) 2014 by Philipp Vogt
package com.github.phvogt.pscratchpad.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.github.phvogt.pscratchpad.android.IConstants;

/**
 * Helper for REST calls.
 */
public final class RestHelper {

    /**
     * Constructor.
     */
    private RestHelper() {
        // intentionally blank
    }

    /**
     * Reads the response from url connection.
     * @param urlConnection url connection to read from
     * @return the result as String
     * @throws IOException if an error occurs
     */
    public static String readResponse(final HttpURLConnection urlConnection) throws IOException {

        final InputStream inputStream = urlConnection.getInputStream();
        final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        String readLine;
        StringBuffer buf = new StringBuffer();

        while ((readLine = in.readLine()) != null) {
            buf.append(readLine);
        }
        if (null != in) {
            in.close();
        }

        final String result = buf.toString();
        return result;
    }

    /**
     * Sends data to output stream of url connection.
     * @param urlConnection url connection
     * @param data data to send
     * @throws IOException if an error occurs
     * @throws UnsupportedEncodingException if an error occurs
     */
    public static void sendData(final HttpURLConnection urlConnection, final String data) throws IOException,
            UnsupportedEncodingException {

        final OutputStream out = urlConnection.getOutputStream();

        final Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put(IConstants.REST_FIELD_DATA, data);
        final String json = new JSONObject(dataMap).toString();
        out.write(json.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

}
