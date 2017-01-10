package com.example.android.sunshine;

import android.content.ComponentName;
import android.support.wearable.complications.ProviderUpdateRequester;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerListener";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onDataChanged: " + dataEvents);
        }

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult = googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMapItem = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String path = event.getDataItem().getUri().getPath();
                if (path.equals("/weather")) {
                    Log.d(TAG, "onDataChanged: RANDOM: " + dataMapItem.getString("random"));
                    Log.d(TAG, "onDataChanged: MIN TEMP IS: " + dataMapItem.getString("minTemp"));
                    Log.d(TAG, "onDataChanged: MAX TEMP IS: " + dataMapItem.getString("maxTemp"));

                    MinTemperatureProviderService.mMinTemp = dataMapItem.getString("minTemp");
                    ComponentName componentName = new ComponentName(getApplicationContext(), MinTemperatureProviderService.class);
                    ProviderUpdateRequester providerUpdateRequester = new ProviderUpdateRequester(this, componentName);
                    providerUpdateRequester.requestUpdateAll();

                    MaxTemperatureProviderService.mMaxTemp = dataMapItem.getString("maxTemp");
                    componentName = new ComponentName(getApplicationContext(), MaxTemperatureProviderService.class);
                    providerUpdateRequester = new ProviderUpdateRequester(this, componentName);
                    providerUpdateRequester.requestUpdateAll();
                }
            }
        }
    }
}
