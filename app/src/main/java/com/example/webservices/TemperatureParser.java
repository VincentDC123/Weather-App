package com.example.webservices;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TemperatureParser {
    public static final double ZERO_K = -273.15;
    private final String MAIN_KEY = "main";
    private final String TEMPERATURE_KEY = "temp";

    private JSONObject jsonObject;

    public TemperatureParser( String json ) {
        try {
            jsonObject = new JSONObject(json);
//            JSONObject coordJSONObject = jsonObject.getJSONObject( "coord" );
//            JSONArray weatherJSONArray = jsonObject.getJSONArray( "weather" );
//            String city = jsonObject.getString( "name" );
//            Log.w("TemperatureParser", "Coord: " + coordJSONObject + " , Weather: " +
//                    weatherJSONArray + " , City: " + city);
        } catch (JSONException jsonException) {
            Log.w("TemperatureParser", jsonException);
        }
    }

    public double getTemperatureK() {
        try {
            JSONObject jsonMain = jsonObject.getJSONObject(MAIN_KEY);
            return jsonMain.getDouble(TEMPERATURE_KEY);
        } catch (Exception jsonException) {
            Log.w("TemperaturParser", jsonException);
            return 25 - ZERO_K;
        }
    }

    public String getIcon() {
        try{
            JSONArray jsonArray = jsonObject.getJSONArray("weather");
            JSONObject JSONIcon = jsonArray.getJSONObject(0);
            return JSONIcon.getString("icon");
        } catch (JSONException jsonException) {
            Log.w("TemperatureParser", jsonException);
            return null;
        }
    }

    public String getIconDesc() {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("weather");
            JSONObject JSONIcon = jsonArray.getJSONObject(0);
            return JSONIcon.getString("description");
        } catch (JSONException jsonException) {
            Log.w("TemperatureParser", jsonException);
            return null;
        }
    }

    public int getTemperatureC() {
        return (int) (getTemperatureK() + ZERO_K + 0.5);
    }

    public int getTemperatureF() {
        return (int) ((getTemperatureK() + ZERO_K) * 9 / 5 + 32 + 0.5);
    }
}
