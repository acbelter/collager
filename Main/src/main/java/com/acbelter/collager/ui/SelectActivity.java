package com.acbelter.collager.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import com.acbelter.collager.Constants;
import com.acbelter.collager.InstagramImageData;
import com.acbelter.collager.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectActivity extends Activity implements OnItemClickListener {
    private SelectGridViewAdapter mGridViewAdapter;
    private List<InstagramImageData> mInstagramImageData;
    private ArrayList<InstagramImageData> mCheckedImagesData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        GridView selectionGrid = (GridView) findViewById(R.id.select_grid);
        selectionGrid.setOnItemClickListener(this);

        if (savedInstanceState == null) {
            mInstagramImageData = getIntent().getParcelableArrayListExtra(Constants.KEY_IMAGES_DATA);
            Collections.sort(mInstagramImageData, Collections.reverseOrder());
            mCheckedImagesData = new ArrayList<InstagramImageData>();
        } else {
            mInstagramImageData = savedInstanceState.getParcelableArrayList(Constants.KEY_IMAGES_DATA);
            mCheckedImagesData = savedInstanceState.getParcelableArrayList(Constants.KEY_CHECKED_IMAGES_DATA);
        }

        mGridViewAdapter = new SelectGridViewAdapter(this, mInstagramImageData);
        selectionGrid.setAdapter(mGridViewAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Constants.KEY_IMAGES_DATA, new ArrayList<InstagramImageData>(mInstagramImageData));
        outState.putParcelableArrayList(Constants.KEY_CHECKED_IMAGES_DATA, mCheckedImagesData);
    }

    public void buildCollage(View view) {
        mCheckedImagesData.clear();
        for (int i = 0; i < mInstagramImageData.size(); i++) {
            if (mInstagramImageData.get(i).isChecked()) {
                mCheckedImagesData.add(mInstagramImageData.get(i));
            }
        }
        Intent intent = new Intent(this, CollageActivity.class);
        if (mCheckedImagesData.isEmpty()) {
            for (int i = 0; i < mInstagramImageData.size(); i++) {
                mCheckedImagesData.add(mInstagramImageData.get(i));
            }
        }
        intent.putParcelableArrayListExtra(Constants.KEY_CHECKED_IMAGES_DATA, mCheckedImagesData);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        if (!mGridViewAdapter.getItem(position).isChecked()) {
            mGridViewAdapter.getItem(position).setChecked(true);
        } else {
            mGridViewAdapter.getItem(position).setChecked(false);
        }
        mGridViewAdapter.notifyDataSetChanged();
    }
}
