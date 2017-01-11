package com.example.android.sunshine;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.complications.ProviderUpdateRequester;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class DataLayerListenerService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "DataLayerListener";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

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

                    Asset weatherIconAsset = dataMapItem.getAsset("weatherIcon");
                    WeatherIconProviderService.mWeatherIcon = loadBitmapFromAsset(weatherIconAsset);
                    ComponentName componentName = new ComponentName(getApplicationContext(), WeatherIconProviderService.class);
                    ProviderUpdateRequester providerUpdateRequester = new ProviderUpdateRequester(this, componentName);
                    providerUpdateRequester.requestUpdateAll();

                    MinTemperatureProviderService.mMinTemp = dataMapItem.getString("minTemp");
                    componentName = new ComponentName(getApplicationContext(), MinTemperatureProviderService.class);
                    providerUpdateRequester = new ProviderUpdateRequester(this, componentName);
                    providerUpdateRequester.requestUpdateAll();

                    MaxTemperatureProviderService.mMaxTemp = dataMapItem.getString("maxTemp");
                    componentName = new ComponentName(getApplicationContext(), MaxTemperatureProviderService.class);
                    providerUpdateRequester = new ProviderUpdateRequester(this, componentName);
                    providerUpdateRequester.requestUpdateAll();
                }
            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        ConnectionResult result = mGoogleApiClient.blockingConnect();
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
