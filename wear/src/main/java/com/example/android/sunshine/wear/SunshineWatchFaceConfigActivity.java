package com.example.android.sunshine.wear;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SunshineWatchFaceConfigActivity extends Activity
        implements WearableListView.ClickListener {

    private static final String TAG = "SunshineWatchFaceConfig";
    private ConfigurationAdapter mAdapter;
    private WearableListView mWearableConfigListView;
    private static final int PROVIDER_CHOOSER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_config);

        mAdapter = new ConfigurationAdapter(getApplicationContext(), getComplicationItems());

        mWearableConfigListView = (WearableListView) findViewById(R.id.wearable_list);
        mWearableConfigListView.setAdapter(mAdapter);
        mWearableConfigListView.setClickListener(this);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Log.d(TAG, "onClick()");

        Integer tag = (Integer) viewHolder.itemView.getTag();
        ComplicationItem complicationItem = mAdapter.getItem(tag);

        startActivityForResult(
                ComplicationHelperActivity.createProviderChooserHelperIntent(
                        getApplicationContext(),
                        complicationItem.watchFace,
                        complicationItem.complicationId,
                        complicationItem.supportedTypes),
                PROVIDER_CHOOSER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PROVIDER_CHOOSER_REQUEST_CODE
                && resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    private final class ComplicationItem {
        ComponentName watchFace;
        int complicationId;
        int[] supportedTypes;
        Drawable icon;
        String title;

        public ComplicationItem(ComponentName watchFace, int complicationId, int[] supportedTypes,
                                Drawable icon, String title) {
            this.watchFace = watchFace;
            this.complicationId = complicationId;
            this.supportedTypes = supportedTypes;
            this.icon = icon;
            this.title = title;
        }
    }

    private List<ComplicationItem> getComplicationItems() {
        ComponentName watchFace = new ComponentName(getApplicationContext(), SunshineWatchFace.class);
        String[] complicationNames = getResources().getStringArray(R.array.complication_names);
        int[] complicationIds = SunshineWatchFace.COMPLICATION_IDS;
        TypedArray icons = getResources().obtainTypedArray(R.array.complication_icons);

        List<ComplicationItem> items = new ArrayList<>();
        for (int i = 0; i < complicationIds.length; i++) {
            items.add(new ComplicationItem(
                    watchFace,
                    complicationIds[i],
                    SunshineWatchFace.COMPLICATION_SUPPORTED_TYPES[i],
                    icons.getDrawable(i),
                    complicationNames[i]
            ));
        }
        icons.recycle();
        return items;
    }

    private static class ConfigurationAdapter extends WearableListView.Adapter {

        private Context mContext;
        private final LayoutInflater mInflater;
        private List<ComplicationItem> mItems;


        public ConfigurationAdapter (Context context, List<ComplicationItem> items) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mItems = items;
        }

        // Provides a reference to the type of views you're using
        public static class ItemViewHolder extends WearableListView.ViewHolder {
            private ImageView iconImageView;
            private TextView textView;
            public ItemViewHolder(View itemView) {
                super(itemView);
                iconImageView = (ImageView) itemView.findViewById(R.id.icon);
                textView = (TextView) itemView.findViewById(R.id.name);
            }
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            // Inflate custom layout for list items.
            return new ItemViewHolder(
                    mInflater.inflate(R.layout.activity_watch_face_config_list_item, null));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {

            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            ImageView imageView = itemHolder.iconImageView;
            imageView.setImageDrawable(mItems.get(position).icon);

            TextView textView = itemHolder.textView;
            textView.setText(mItems.get(position).title);

            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public ComplicationItem getItem(int position) {
            return mItems.get(position);
        }
    }
}
