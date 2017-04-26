package com.abarska.currencyexchanger;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class QueryUtils {

    private final static String LOG_TAG = "QueryUtils";
    private final static int CONNECTION_TIMEOUT = 10000;

    public static String getToday() {
        return today;
    }

    public static String getCurrentResponse() {
        return currentResponse;
    }

    public static String getPreviousResponse() {
        return previousResponse;
    }

    private static String today;
    private static String currentResponse;
    private static String previousResponse;

    private QueryUtils() {
    }

    public static String buildUrl(String date) {
        if (today == null) today = date;
        Uri baseUri = Uri.parse(CurrencyExchangeActivity.BASE_URL);
        Uri.Builder builder = baseUri.buildUpon();
        builder.appendQueryParameter("date", date);
        return builder + "&json";
    }

    public static Date setPreviousDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);

        // if previous day is FRI/SAT/SUN, we switch to previous working day - THU
        // because exchange rates are not updated on SAT and SUN
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 3);
        }
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 2);
        }
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
        }
        return calendar.getTime();
    }

    public static ArrayList<CurrencyItem> extractData(String url) {

        URL targetUrl = createURL(url);
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        String jsonResponse = "";

        try {
            connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                jsonResponse = readFromStream(inputStream);
                inputStream.close();
            } else {
                Log.d(LOG_TAG, "response code = " + connection.getResponseCode());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();
        }
        return extractFeatureFromJson(jsonResponse);
    }

    private static URL createURL(String url) {
        if (TextUtils.isEmpty(url)) return null;
        URL targetUrl = null;
        try {
            targetUrl = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "wrong URL", e);
        }
        return targetUrl;
    }

    public static ArrayList<CurrencyItem> extractFeatureFromJson(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) return null;

        saveJsonResponse(jsonResponse);
        ArrayList<CurrencyItem> result = new ArrayList<>();

        try {
            JSONArray currencyArray = new JSONArray(jsonResponse);
            for (int i = 0; i < currencyArray.length(); i++) {
                JSONObject current = (JSONObject) currencyArray.get(i);
                String code = current.getString("cc");
                if (code.equals("USD") || code.equals("EUR") || code.equals("RUB")) {
                    double rate = current.getDouble("rate");
                    String description = current.getString("txt");
                    CurrencyItem currencyItem = new CurrencyItem(rate, code, description);
                    result.add(currencyItem);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        if (inputStream == null) return null;
        InputStreamReader isr = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        StringBuilder builder = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            builder.append(line);
            line = br.readLine();
        }
        return builder.toString();
    }

    private static void saveJsonResponse(String response) {
        if (currentResponse == null) currentResponse = response;
        else previousResponse = response;
    }
}
