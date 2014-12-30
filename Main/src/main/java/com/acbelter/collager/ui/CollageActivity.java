package com.acbelter.collager.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.acbelter.collager.Constants;
import com.acbelter.collager.InstagramImageData;
import com.acbelter.collager.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class CollageActivity extends ActionBarActivity implements CollagePreviewFragment.OnBuildingCollageListener {
    private static final String COLLAGES_FOLDER = "Collager";
    private TextView mStatus;
    private ScaledButton mSendCollageButton;
    private Bitmap mCollage;
    private File mCollageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);
        mStatus = (TextView) findViewById(R.id.status);
        mSendCollageButton = (ScaledButton) findViewById(R.id.btn_send_collage);

        mSendCollageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isExternalStorageReadable() || !mCollageFile.exists()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_load_collage_failed),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Uri uri = Uri.fromFile(mCollageFile);
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                emailIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(emailIntent, getString(R.string.sending_with)));
            }
        });

        if (savedInstanceState != null) {
            mCollage = savedInstanceState.getParcelable("collage");
        }

        ArrayList<InstagramImageData> checkedImagesData =
                getIntent().getParcelableArrayListExtra(Constants.KEY_CHECKED_IMAGES_DATA);
        if (checkedImagesData != null) {
            Collections.shuffle(checkedImagesData, new Random(System.nanoTime()));
            FragmentManager fm = getSupportFragmentManager();
            CollagePreviewFragment previewFragment = (CollagePreviewFragment)
                    fm.findFragmentByTag(CollagePreviewFragment.class.getSimpleName());
            if (previewFragment == null) {
                previewFragment = new CollagePreviewFragment();
                Bundle args = new Bundle();
                args.putParcelableArrayList(Constants.KEY_CHECKED_IMAGES_DATA, checkedImagesData);
                previewFragment.setArguments(args);

                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.collage_preview_frame, previewFragment,
                        CollagePreviewFragment.class.getSimpleName());
                ft.commit();
            }
        }
    }

    public Bitmap getCollage() {
        return mCollage;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("collage", mCollage);
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private String createCollageName() {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmmss");
        return "collage_" + sdf.format(new Date()) + ".jpg";
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private File saveCollage(Bitmap bitmap) {
        if (!isExternalStorageWritable()) {
            return null;
        }


        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), COLLAGES_FOLDER);
        dir.mkdirs();
        if (!dir.exists()) {
            return null;
        }

        File collage = new File(dir, createCollageName());
        boolean status;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(collage);
            status = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            status = false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e("Collager", "Method saveCollage() can't close FileOutputStream.");
                }
            }
        }

        if (!status) {
            return null;
        }

        return collage;
    }

    @Override
    public void onCollageBuildingStarted() {
        mSendCollageButton.setEnabled(false);
        mStatus.setText(getString(R.string.status_collage_building));
    }

    private void clearStatus() {
        mStatus.setText("");
    }

    @Override
    public void onCollageCreated(Bitmap collage) {
        clearStatus();
        mCollage = collage;
        mCollageFile = saveCollage(mCollage);
        if (mCollageFile == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_save_collage_failed),
                    Toast.LENGTH_SHORT).show();
        }
        mSendCollageButton.setEnabled(true);
    }

    @Override
    public void onCollageNotCreated(int failCode) {
        mCollage = null;
        clearStatus();
        mSendCollageButton.setEnabled(false);
        switch (failCode) {
            case CollagePreviewFragment.OnBuildingCollageListener.CODE_NO_ELEMENTS: {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_no_collage_elements),
                        Toast.LENGTH_LONG).show();
                break;
            }
            case CollagePreviewFragment.OnBuildingCollageListener.CODE_OUT_OF_MEMORY: {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_out_of_memory),
                        Toast.LENGTH_LONG).show();
                break;
            }
        }
    }
}
