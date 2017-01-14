package com.example.android.sunshine;

import android.content.Intent;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.util.Log;

public class MaxTemperatureProviderService extends ComplicationProviderService {

    private static final String TAG = "MaxTemperatureProvider";
    public static String mMaxTemp = "";

    @Override
    public void onComplicationUpdate(int complicationId, int dataType, ComplicationManager complicationManager) {
        Log.d(TAG, "onComplicationUpdate(): id is " + complicationId);
        Log.d(TAG, "onComplicationUpdate: max: " + mMaxTemp);

        ComplicationData complicationData = null;

        if (!mMaxTemp.equals("")) {
            switch (dataType) {
                case ComplicationData.TYPE_SHORT_TEXT:
                    Log.d(TAG, "TYPE_SHORT_TEXT");
                    complicationData = new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                            .setShortText(ComplicationText.plainText(mMaxTemp))
                            .build();
                    break;
                default:
                    if (Log.isLoggable(TAG, Log.WARN)) {
                        Log.w(TAG, "Unexpected complication type " + dataType);
                    }
            }
        } else {
            Intent intent = new Intent(getApplicationContext(), GetWeatherService.class);
            startService(intent);
        }

        if (complicationData != null) {
            complicationManager.updateComplicationData(complicationId, complicationData);
        }
    }
}
