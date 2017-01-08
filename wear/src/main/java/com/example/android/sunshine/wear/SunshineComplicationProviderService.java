package com.example.android.sunshine.wear;

import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.util.Log;

import java.util.Locale;

import static android.content.ContentValues.TAG;

public class SunshineComplicationProviderService extends ComplicationProviderService {

    @Override
    public void onComplicationUpdate(int complicationId, int dataType, ComplicationManager complicationManager) {
        Log.d(TAG, "onComplicationUpdate(): " + complicationId);

        int randomNumber = (int) Math.floor(Math.random() * 10);

        String randomNumberText =
                String.format(Locale.getDefault(), "%d!", randomNumber);

        ComplicationData complicationData = null;

        switch (dataType) {
            case ComplicationData.TYPE_SHORT_TEXT:
                Log.d(TAG, "TYPE_SHORT_TEXT");
                complicationData = new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                        .setShortText(ComplicationText.plainText(randomNumberText))
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
