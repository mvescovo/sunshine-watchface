package com.example.android.sunshine;

import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SunshineDateProviderService extends ComplicationProviderService {

    private static final String TAG = "SunshineDateProvider";

    @Override
    public void onComplicationUpdate(int complicationId, int dataType, ComplicationManager complicationManager) {
        Log.d(TAG, "onComplicationUpdate(): id is " + complicationId);

        ComplicationData complicationData = null;

        switch (dataType) {
            case ComplicationData.TYPE_LONG_TEXT:
                Log.d(TAG, "TYPE_SHORT_TEXT");

                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
                Calendar calendar = Calendar.getInstance();
                long now = System.currentTimeMillis();
                calendar.setTimeInMillis(now);
                String date = dateFormat.format(calendar.getTime());

                complicationData = new ComplicationData.Builder(ComplicationData.TYPE_LONG_TEXT)
                        .setLongText(ComplicationText.plainText(date))
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
