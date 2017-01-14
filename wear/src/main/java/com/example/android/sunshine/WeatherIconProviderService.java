package com.example.android.sunshine;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.util.Log;

public class WeatherIconProviderService extends ComplicationProviderService {

    private static final String TAG = "WeatherIconProvider";
    public static Bitmap mWeatherIcon;

    @Override
    public void onComplicationUpdate(int complicationId, int dataType, ComplicationManager complicationManager) {
        Log.d(TAG, "onComplicationUpdate(): id is " + complicationId);

        ComplicationData complicationData = null;

        if (mWeatherIcon != null) {
            Log.d(TAG, "onComplicationUpdate: icon is NOT NULL");
            switch (dataType) {
                case ComplicationData.TYPE_ICON:
                    Log.d(TAG, "TYPE_ICON");
                    complicationData = new ComplicationData.Builder(ComplicationData.TYPE_ICON)
                            .setIcon(Icon.createWithBitmap(mWeatherIcon))
                            .build();
                    break;
                default:
                    if (Log.isLoggable(TAG, Log.WARN)) {
                        Log.w(TAG, "Unexpected complication type " + dataType);
                    }
            }
        }  else {
            Intent intent = new Intent(getApplicationContext(), GetWeatherService.class);
            startService(intent);
        }

        if (complicationData != null) {
            Log.d(TAG, "onComplicationUpdate: DATA IS NOT NULL");
            complicationManager.updateComplicationData(complicationId, complicationData);
        } else {
            Log.d(TAG, "onComplicationUpdate: DATA IS NULL");
        }
    }
}
