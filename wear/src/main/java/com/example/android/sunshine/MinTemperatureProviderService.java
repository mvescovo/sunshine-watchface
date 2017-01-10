package com.example.android.sunshine;

import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.util.Log;

public class MinTemperatureProviderService extends ComplicationProviderService {

    private static final String TAG = "MinTemperatureProvider";
    public static String mMinTemp = "";

    @Override
    public void onComplicationUpdate(int complicationId, int dataType, ComplicationManager complicationManager) {
        Log.d(TAG, "onComplicationUpdate(): id is " + complicationId);
        Log.d(TAG, "onComplicationUpdate: min: " + mMinTemp);

        ComplicationData complicationData = null;

        if (!mMinTemp.equals("")) {
            switch (dataType) {
                case ComplicationData.TYPE_SHORT_TEXT:
                    Log.d(TAG, "TYPE_SHORT_TEXT");
                    complicationData = new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                            .setShortText(ComplicationText.plainText(mMinTemp))
                            .build();
                    break;
                default:
                    if (Log.isLoggable(TAG, Log.WARN)) {
                        Log.w(TAG, "Unexpected complication type " + dataType);
                    }
            }
        }

        if (complicationData != null) {
            complicationManager.updateComplicationData(complicationId, complicationData);
        }
    }
}
