package com.example.android.sunshine.sync;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.example.android.sunshine.MainActivity;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import static com.example.android.sunshine.MainActivity.INDEX_WEATHER_CONDITION_ID;
import static com.example.android.sunshine.MainActivity.INDEX_WEATHER_MAX_TEMP;

public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerListener";
    private static final String GET_WEATHER_PATH = "/get-weather";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(GET_WEATHER_PATH)) {
            Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
            String[] MAIN_FORECAST_PROJECTION = {
                    WeatherContract.WeatherEntry.COLUMN_DATE,
                    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                    WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                    WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            };
            String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(
                    forecastQueryUri,
                    MAIN_FORECAST_PROJECTION,
                    selection,
                    null,
                    null);

            if (cursor != null) {
                cursor.moveToFirst();
                double lowInCelsius = cursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
                String lowString = SunshineWeatherUtils.formatTemperature(this, lowInCelsius);
                double highInCelsius = cursor.getDouble(INDEX_WEATHER_MAX_TEMP);
                String highString = SunshineWeatherUtils.formatTemperature(this, highInCelsius);
                int iconId = SunshineWeatherUtils
                        .getLargeArtResourceIdForWeatherCondition(cursor.getInt(INDEX_WEATHER_CONDITION_ID));
                Intent intent = new Intent(this, WearableDataSyncService.class);
                Bundle bundle = new Bundle();
                bundle.putString("minTemp", lowString);
                bundle.putString("maxTemp", highString);
                bundle.putInt("iconId", iconId);
                intent.putExtra("weatherData", bundle);
                this.startService(intent);
            }

            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
