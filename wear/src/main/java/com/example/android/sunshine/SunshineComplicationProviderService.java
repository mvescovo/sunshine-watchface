package com.example.android.sunshine;

import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.util.Log;

public class SunshineComplicationProviderService extends ComplicationProviderService {

    private static final String TAG = "SunshineComplicationPro";
    public static String mData = "";

    @Override
    public void onComplicationUpdate(int complicationId, int dataType, ComplicationManager complicationManager) {
        Log.d(TAG, "onComplicationUpdate(): " + complicationId);

        if (!mData.equals("")) {
            ComplicationData complicationData = null;

            switch (dataType) {
                case ComplicationData.TYPE_SHORT_TEXT:
                    Log.d(TAG, "TYPE_SHORT_TEXT");
                    complicationData = new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                            .setShortText(ComplicationText.plainText(mData))
                            .build();
                    break;
                default:
                    if (Log.isLoggable(TAG, Log.WARN)) {
                        Log.w(TAG, "Unexpected complication type " + dataType);
                    }
            }

            if (complicationData != null) {
                complicationManager.updateComplicationData(complicationId, complicationData);
            }
        }
    }
}
