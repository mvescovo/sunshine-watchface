/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationText;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.example.android.sunshine.wear.R;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class SunshineWatchFace extends CanvasWatchFaceService {

    private static final String TAG = "SunshineWatchFace";

    private static final int TOP_COMPLICATION = 0;
    private static final int LEFT_COMPLICATION = 1;
    private static final int MIDDLE_COMPLICATION = 2;
    private static final int RIGHT_COMPLICATION = 3;
    public static final int[] COMPLICATION_IDS = {TOP_COMPLICATION, LEFT_COMPLICATION, MIDDLE_COMPLICATION, RIGHT_COMPLICATION};
    public static final int[][] COMPLICATION_SUPPORTED_TYPES = {
            {ComplicationData.TYPE_SHORT_TEXT},
            {ComplicationData.TYPE_SHORT_TEXT, ComplicationData.TYPE_ICON},
            {ComplicationData.TYPE_SHORT_TEXT, ComplicationData.TYPE_ICON},
            {ComplicationData.TYPE_SHORT_TEXT, ComplicationData.TYPE_ICON}
    };

    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<SunshineWatchFace.Engine> mWeakReference;

        public EngineHandler(SunshineWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SunshineWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint;
        Paint mTextPaint;
        boolean mAmbient;
        Calendar mCalendar;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        float mXOffset;
        float mYOffset;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        /*
        * Complication stuff
        * */
        private Paint mComplicationPaint;
        private int mTopComplicationY;
        private int mDialsComplicationsY;
        private SparseArray<ComplicationData> mActiveComplicationDataSparseArray;
        private static final float COMPLICATION_TEXT_SIZE = 38f;
        private int mWidth;
        private int mHeight;


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());
            Resources resources = SunshineWatchFace.this.getResources();

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.background));

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));

            mCalendar = Calendar.getInstance();

            initialiseComplications();
        }

        private void initialiseComplications() {
            Log.d(TAG, "initialiseComplications()");
            mActiveComplicationDataSparseArray = new SparseArray<>(COMPLICATION_IDS.length);
            mComplicationPaint = new Paint();
            mComplicationPaint.setColor(Color.WHITE);
            mComplicationPaint.setTextSize(COMPLICATION_TEXT_SIZE);
            mComplicationPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            mComplicationPaint.setAntiAlias(true);
            setActiveComplications(COMPLICATION_IDS);
        }

        @Override
        public void onComplicationDataUpdate(int complicationId, ComplicationData complicationData) {
            Log.d(TAG, "onComplicationDataUpdate() id: " + complicationId);

            // Adds/updates active complication data in the array.
            mActiveComplicationDataSparseArray.put(complicationId, complicationData);
            invalidate();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SunshineWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SunshineWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = SunshineWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mYOffset = resources.getDimension(isRound
                    ? R.dimen.digital_y_offset_round : R.dimen.digital_y_offset);
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                    mComplicationPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mWidth = width;
            mHeight = height;
            mTopComplicationY = (int) ((mHeight / 2) + (mComplicationPaint.getTextSize() / 2));
            mDialsComplicationsY = (int) (((mHeight / 3)) * 2 + (mComplicationPaint.getTextSize() / 2));
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            }

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            String text = mAmbient
                    ? String.format(Locale.getDefault(), "%d:%02d", mCalendar.get(Calendar.HOUR),
                    mCalendar.get(Calendar.MINUTE))
                    : String.format(Locale.getDefault(), "%d:%02d:%02d", mCalendar.get(Calendar.HOUR),
                    mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);

            drawComplications(canvas, now);
        }

        private void drawComplications(Canvas canvas, long currentTimeMillis) {
            ComplicationData complicationData;

            for (int COMPLICATION_ID : COMPLICATION_IDS) {

                complicationData = mActiveComplicationDataSparseArray.get(COMPLICATION_ID);

                if ((complicationData != null)
                        && (complicationData.isActive(currentTimeMillis))) {

                    if (complicationData.getType() == ComplicationData.TYPE_SHORT_TEXT
                            || complicationData.getType() == ComplicationData.TYPE_NO_PERMISSION) {

                        ComplicationText mainText = complicationData.getShortText();
                        ComplicationText subText = complicationData.getShortTitle();

                        CharSequence complicationMessage =
                                mainText.getText(getApplicationContext(), currentTimeMillis);

                        if (subText != null) {
                            complicationMessage = TextUtils.concat(
                                    complicationMessage,
                                    " ",
                                    subText.getText(getApplicationContext(), currentTimeMillis));
                        }

                        double textWidth = mComplicationPaint.measureText(
                                complicationMessage,
                                0,
                                complicationMessage.length()
                        );

                        int complicationsX = 0;
                        int offset;
                        switch (COMPLICATION_ID) {
                            case TOP_COMPLICATION:
                                complicationsX = (int) (mWidth - textWidth) / 2;
                                break;
                            case LEFT_COMPLICATION:
                                complicationsX = (int) ((mWidth / 3) - textWidth) / 2;
                                break;
                            case MIDDLE_COMPLICATION:
                                offset = (int) ((mWidth / 3) - textWidth) / 2;
                                complicationsX = (mWidth / 3) + offset;
                                break;
                            case RIGHT_COMPLICATION:
                                offset = (int) ((mWidth / 3) - textWidth) / 2;
                                complicationsX = (mWidth / 3 * 2) + offset;
                                break;
                        }

                        canvas.drawText(
                                complicationMessage,
                                0,
                                complicationMessage.length(),
                                complicationsX,
                                COMPLICATION_ID == TOP_COMPLICATION
                                        ? mTopComplicationY
                                        : mDialsComplicationsY,
                                mComplicationPaint);
                    } else if (complicationData.getType() == ComplicationData.TYPE_ICON) {
                        Icon icon = complicationData.getIcon();
                        Drawable drawable = icon.loadDrawable(getApplicationContext());
                        if (drawable instanceof BitmapDrawable) {
                            Bitmap bitmap;
                            bitmap = ((BitmapDrawable) drawable).getBitmap();
                            int complicationsX = 0;
                            int offset;
                            switch (COMPLICATION_ID) {
                                case LEFT_COMPLICATION:
                                    complicationsX = ((mWidth / 3) - bitmap.getWidth()) / 2;
                                    break;
                                case MIDDLE_COMPLICATION:
                                    offset = ((mWidth / 3) - bitmap.getWidth()) / 2;
                                    complicationsX = (mWidth / 3) + offset;
                                    break;
                                case RIGHT_COMPLICATION:
                                    offset = ((mWidth / 3) - bitmap.getWidth()) / 2;
                                    complicationsX = (mWidth / 3 * 2) + offset;
                                    break;
                            }
                            int bitmapY = mDialsComplicationsY - (int) mComplicationPaint.getTextSize();
                            canvas.drawBitmap(bitmap, complicationsX, bitmapY, null);
                        }
                    }
                }
            }
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
