package com.example.android.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

public class WearableDataSyncService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WearableDataSyncService";
    private ServiceHandler mServiceHandler;
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        Looper serviceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(serviceLooper);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(final Message msg) {
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.blockingConnect();
            }

            if (mGoogleApiClient.isConnected()) {
                int randomNumber = (int) Math.floor(Math.random() * 10);
                String randomNumberText = String.format(Locale.getDefault(), "%d!", randomNumber);
                Bitmap bitmap = getBitmap((VectorDrawableCompat.create(getResources(), msg.getData().getInt("iconId"), null)));
                Asset asset = createAssetFromBitmap(bitmap);
                String minTemp = msg.getData().getString("minTemp");
                String maxTemp = msg.getData().getString("maxTemp");
                PutDataMapRequest dataMap = PutDataMapRequest.create("/weather");
                dataMap.getDataMap().putAsset("weatherIcon", asset);
                dataMap.getDataMap().putString("random", randomNumberText);
                dataMap.getDataMap().putString("minTemp", minTemp);
                dataMap.getDataMap().putString("maxTemp", maxTemp);
                PutDataRequest request = dataMap.asPutDataRequest();
                request.setUrgent();

                Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                        .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                                if (dataItemResult.getStatus().isSuccess()) {
                                    Log.d(TAG, "onResult: successfully sent data item");
                                } else {
                                    Log.d(TAG, "onResult: did not send data item");
                                }
                                stopSelf(msg.arg1);
                            }
                        });
            }
        }
    }

    private static Bitmap getBitmap(VectorDrawableCompat vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.setData(intent.getBundleExtra("weatherData"));
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {}

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
