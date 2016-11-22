package com.example.android.sunshine.app;

import android.app.Activity;
import android.content.res.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Батинчук on 22.08.2016.
 */
public class WeatherJsonParser {

    public static String[] getWeatherDataString(String jsonDataString, String units) throws JSONException {

        String[] weatherDataString = new String[7];

        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        JSONObject serverReplyObject = new JSONObject(jsonDataString);

        JSONArray listJsonArray = serverReplyObject.getJSONArray("list");

        for (int i = 0; i < 7; i++) {
            JSONObject dayForecastObject = listJsonArray.getJSONObject(i);
            JSONObject temperatureObject = dayForecastObject.getJSONObject("temp");

            JSONArray weatherMainJsonArray = dayForecastObject.getJSONArray("weather");
            JSONObject mainJsonObject = weatherMainJsonArray.getJSONObject(0);

            String temperatureForecast;

            if (units.equals(Resources.getSystem().getString(R.string.pref_units_metric))) {
                temperatureForecast = maxMinTemperature(temperatureObject.getDouble("max"),
                        temperatureObject.getDouble("min"));
            } else temperatureForecast = maxMinTemperature(
                    temperatureObject.getDouble("max") * 1.8 + 32,
                    temperatureObject.getDouble("min") * 1.8 + 32);
            weatherDataString[i] = getReadableDate(calendar.getTimeInMillis()) + "-"
                    + mainJsonObject.getString("main") + "-"
                    + temperatureForecast;

            calendar.roll(Calendar.DAY_OF_YEAR, true);
        }

        return weatherDataString;
    }

    private static String getReadableDate(long dateTime) {
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE dd MMM");
        return shortenedDateFormat.format(dateTime);
    }

    private static String maxMinTemperature(double max, double min) {

        return String.valueOf(Math.round(max)) + "/" + String.valueOf(Math.round(min));
    }
}
