package com.acbelter.collager.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.acbelter.collager.InstagramImageData;
import com.acbelter.collager.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class SelectGridViewAdapter extends ArrayAdapter<InstagramImageData> {
    private final int mSelectColor;
    private ImageLoader mImageLoader;
    private LayoutInflater mInflater;

    public SelectGridViewAdapter(Context context, List<InstagramImageData> images) {
        super(context, R.layout.grid_item, images);
        mImageLoader = ImageLoader.getInstance();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSelectColor = context.getResources().getColor(R.color.holo_blue_light);
    }

    static class ViewHolder {
        ImageView image;
        TextView likes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.grid_item, parent, false);

            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.likes = (TextView) convertView.findViewById(R.id.likes);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        InstagramImageData image = getItem(position);
        mImageLoader.displayImage(image.getLink(), holder.image);
        holder.likes.setText(Integer.toString(image.getLikes()));

        if (image.isChecked()) {
            convertView.setBackgroundColor(mSelectColor);
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }
}
