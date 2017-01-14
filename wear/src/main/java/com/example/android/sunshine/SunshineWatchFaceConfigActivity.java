package com.example.android.sunshine;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.wear.R;

import java.util.ArrayList;
import java.util.List;

public class SunshineWatchFaceConfigActivity extends Activity {

    private static final String TAG = "SunshineWatchFaceConfig";
    private static final int PROVIDER_CHOOSER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_config);
        Adapter adapter = new Adapter(this, getComplicationItems());
        WearableRecyclerView wearableRecyclerView = (WearableRecyclerView) findViewById(R.id.recycler_view);
        wearableRecyclerView.setAdapter(adapter);
        wearableRecyclerView.setCenterEdgeItems(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PROVIDER_CHOOSER_REQUEST_CODE
                && resultCode == RESULT_OK) {
            finish();
        }
    }

    private final class ComplicationItem {
        ComponentName watchFace;
        int complicationId;
        int[] supportedTypes;
        Drawable icon;
        String title;

        ComplicationItem(ComponentName watchFace, int complicationId, int[] supportedTypes,
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

    private static class Adapter extends WearableRecyclerView.Adapter {

        private Context mContext;
        private final LayoutInflater mInflater;
        private List<ComplicationItem> mItems;

        Adapter(Context context, List<ComplicationItem> items) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mItems = items;
        }

        class ItemViewHolder extends WearableRecyclerView.ViewHolder {

            private ImageView iconImageView;
            private TextView textView;

            ItemViewHolder(View itemView) {
                super(itemView);
                iconImageView = (ImageView) itemView.findViewById(R.id.icon);
                textView = (TextView) itemView.findViewById(R.id.name);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Integer tag = (Integer) view.getTag();
                        ComplicationItem complicationItem = getItem(tag);
                        ((Activity)mContext).startActivityForResult(
                                ComplicationHelperActivity.createProviderChooserHelperIntent(
                                        mContext,
                                        complicationItem.watchFace,
                                        complicationItem.complicationId,
                                        complicationItem.supportedTypes),
                                PROVIDER_CHOOSER_REQUEST_CODE);
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Adapter.ItemViewHolder(
                    mInflater.inflate(R.layout.activity_watch_face_config_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Adapter.ItemViewHolder itemHolder = (Adapter.ItemViewHolder) holder;
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

        ComplicationItem getItem(int position) {
            return mItems.get(position);
        }
    }
}
