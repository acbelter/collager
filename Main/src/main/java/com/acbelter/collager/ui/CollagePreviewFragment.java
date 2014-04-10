package com.acbelter.collager.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.acbelter.collager.Constants;
import com.acbelter.collager.InstagramImageData;
import com.acbelter.collager.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollagePreviewFragment extends Fragment {
    private ImageLoader mImageLoader;
    private ArrayList<InstagramImageData> mCheckedImageData;
    private OnBuildingCollageListener mCallback;
    private ImageView mCollagePreview;
    private int mFailCode;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (OnBuildingCollageListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageLoader = ImageLoader.getInstance();
        setRetainInstance(true);
        mCheckedImageData = getArguments().getParcelableArrayList(Constants.KEY_CHECKED_IMAGES_DATA);
        new BuildingCollageTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collage_preview, container, false);
        mCollagePreview = (ImageView) view.findViewById(R.id.collage_preview);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bitmap collage = ((CollageActivity) getActivity()).getCollage();
        mCollagePreview.setImageBitmap(collage);
    }

    private Bitmap buildCollage(List<Bitmap> bitmaps) {
        if (bitmaps == null || bitmaps.isEmpty()) {
            mFailCode = OnBuildingCollageListener.CODE_NO_ELEMENTS;
            return null;
        }

        // Remove all null bitmaps
        bitmaps.removeAll(Collections.singleton(null));
        if (bitmaps.isEmpty()) {
            mFailCode = OnBuildingCollageListener.CODE_NO_ELEMENTS;
            return null;
        }

        int size = (int) Math.sqrt(bitmaps.size());
        if (size * size < bitmaps.size()) {
            size++;
        }

        int width = bitmaps.get(0).getWidth();
        Bitmap collage;
        try {
            collage = Bitmap.createBitmap(width * size, width * size, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            mFailCode = OnBuildingCollageListener.CODE_OUT_OF_MEMORY;
            return null;
        }

        Canvas canvas = new Canvas(collage);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();

        float left, top;
        for (int i = 0; i < bitmaps.size(); i++) {
            left = (i % size) * width;
            top = (i / size) * width;
            canvas.drawBitmap(bitmaps.get(i), left, top, paint);
        }
        return collage;
    }

    public static interface OnBuildingCollageListener {
        static final int CODE_NO_ELEMENTS = -1;
        static final int CODE_OUT_OF_MEMORY = -2;
        void onCollageBuildingStarted();
        void onCollageCreated(Bitmap collage);
        void onCollageNotCreated(int failCode);
    }

    private class BuildingCollageTask extends AsyncTask<Void, Integer, Bitmap> {
        @Override
        protected void onPreExecute() {
            if (mCallback != null) {
                mCallback.onCollageBuildingStarted();
            }
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            List<Bitmap> collageBitmaps = new ArrayList<Bitmap>(mCheckedImageData.size());
            for (int i = 0; i < mCheckedImageData.size(); i++) {
                Bitmap bitmap = mImageLoader.loadImageSync(mCheckedImageData.get(i).getLink());
                collageBitmaps.add(bitmap);
            }

            return buildCollage(collageBitmaps);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mCollagePreview.setImageBitmap(bitmap);
            if (mCallback != null) {
                if (bitmap != null) {
                    mCallback.onCollageCreated(bitmap);
                } else {
                    mCallback.onCollageNotCreated(mFailCode);
                }
            }
        }
    }
}
