package com.example.quakereport;

import android.util.Log;

import com.example.quakereport.Earthquake;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Return a list of {@link Earthquake} objects that has been built up from
     * parsing a JSON response.
     */
    public static ArrayList<Earthquake> extractEarthquakes(String jsonResponseString) {

        // Create an empty ArrayList that we can start adding earthquakes to
        ArrayList<Earthquake> earthquakes = new ArrayList<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // TODO: Parse the response given by the SAMPLE_JSON_RESPONSE string and
            // build up a list of Earthquake objects with the corresponding data.

            JSONObject earthquakeJsonObject = new JSONObject(jsonResponseString);
            JSONArray featuresArray = earthquakeJsonObject.getJSONArray("features");

            for (int i = 0; i < featuresArray.length(); i++) {
                JSONObject featuresObject = featuresArray.getJSONObject(i);
                JSONObject propertiesObject = featuresObject.getJSONObject("properties");

                float magnitude = (float) propertiesObject.getDouble("mag");
                String place = propertiesObject.getString("place");
                long time = propertiesObject.getLong("time");
                String url = propertiesObject.getString("url");

                earthquakes.add(new Earthquake(magnitude, place, time, url));
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }

    public static URL createUrl(String str) {

        /** If input is null, exit early */
        if (str == null)
            return null;

        /** If input is not null, String is converted to a URL using constructor of URL class.
         *  MalformedURLException is handled in the catch block.
         */
        URL requestUrl = null;
        try {
            requestUrl = new URL(str);
        } catch (MalformedURLException e) {
            Log.e(QueryUtils.class.toString(), "Error in converting String to URL: ", e);
        }

        return requestUrl;
    }

    public static String makeNetworkConnection(URL queryUrl) {
        StringBuilder stringBuilder = new StringBuilder();
        HttpURLConnection httpURLConnection = null;
        String jsonResponseString = null;
        InputStream inputStream = null;
        try {
            httpURLConnection = (HttpURLConnection) queryUrl.openConnection();
            httpURLConnection.setReadTimeout(10000 /* milliseconds */);
            httpURLConnection.setConnectTimeout(15000 /* milliseconds */);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            if (bufferedReader != null) {
                String responseLine = bufferedReader.readLine();
                while (responseLine != null) {
                    stringBuilder.append(responseLine);
                    responseLine = bufferedReader.readLine();
                }
            }
            jsonResponseString = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return jsonResponseString;
    }
}