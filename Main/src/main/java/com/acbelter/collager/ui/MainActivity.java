package com.acbelter.collager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.acbelter.collager.Constants;
import com.acbelter.collager.InstagramImageData;
import com.acbelter.collager.R;
import com.acbelter.collager.network.CollagerNetworkServiceHelper;
import com.acbelter.collager.network.GetPhotosDataCommand;
import com.acbelter.collager.network.GetUserIdCommand;
import com.acbelter.nslib.NetworkApplication;
import com.acbelter.nslib.NetworkServiceCallbackListener;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements NetworkServiceCallbackListener {
    private AdView mBanner;
    private CollagerNetworkServiceHelper mServiceHelper;
    private EditText mUserNick;
    private ScaledButton mGetCollageButton;
    private TextView mStatus;

    private int mGetUserIdRequestId = -1;
    private int mGetPhotosDataRequestId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServiceHelper = new CollagerNetworkServiceHelper(getApp().getNetworkServiceHelper());

        setContentView(R.layout.activity_main);
        mUserNick = (EditText) findViewById(R.id.user_nick);
        mGetCollageButton = (ScaledButton) findViewById(R.id.btn_get_collage);
        mStatus = (TextView) findViewById(R.id.status);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nick = mUserNick.getText().toString().toLowerCase();
                if (!"".equals(nick)) {
                    setUiEnabled(false);
                    mStatus.setText(getString(R.string.status_search_user));
                    mGetUserIdRequestId = mServiceHelper.getUserId(nick);
                }
            }
        };
        mGetCollageButton.setOnClickListener(listener);

        if (savedInstanceState != null) {
            mGetUserIdRequestId = savedInstanceState.getInt("get_user_id_request_id");
            mGetPhotosDataRequestId = savedInstanceState.getInt("get_photos_data_request_id");
            mStatus.setText(savedInstanceState.getCharSequence("status"));
            setUiEnabled(savedInstanceState.getBoolean("ui_enabled"));
        }

        mBanner = (AdView) findViewById(R.id.banner);
        mBanner.loadAd(CollagerAdRequestGenerator.generate());
    }

    private void setUiEnabled(boolean enabled) {
        mUserNick.setEnabled(enabled);
        mGetCollageButton.setEnabled(enabled);
    }

    @Override
    public void onBackPressed() {
        if (mGetUserIdRequestId == -1 && mGetPhotosDataRequestId == -1) {
            super.onBackPressed();
            return;
        }

        if (mGetUserIdRequestId != -1) {
            mServiceHelper.cancelRequest(mGetUserIdRequestId);
            mGetUserIdRequestId = -1;
            setUiEnabled(true);
            mStatus.setText("");
        }
        if (mGetPhotosDataRequestId != -1) {
            mServiceHelper.cancelRequest(mGetPhotosDataRequestId);
            mGetPhotosDataRequestId = -1;
            setUiEnabled(true);
            mStatus.setText("");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("get_user_id_request_id", mGetUserIdRequestId);
        outState.putInt("get_photos_data_request_id", mGetPhotosDataRequestId);
        outState.putCharSequence("status", mStatus.getText());
        outState.putBoolean("ui_enabled", mUserNick.isEnabled());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBanner != null) {
            mBanner.pause();
        }
        mServiceHelper.removeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBanner != null) {
            mBanner.resume();
        }
        mServiceHelper.addListener(this);
    }

    @Override
    public void onDestroy() {
        if (mBanner != null) {
            mBanner.destroy();
        }
        super.onDestroy();
    }

    private NetworkApplication getApp() {
        return (NetworkApplication) getApplication();
    }

    private void processException(int exceptionCode) {
        switch (exceptionCode) {
            case Constants.CODE_URL_EXCEPTION: {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_url_exception),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.CODE_IO_EXCEPTION: {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_io_exception),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.CODE_JSON_EXCEPTION: {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_json_exception),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.CODE_ACCESS_EXCEPTION: {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_access_denied),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case Constants.CODE_UNKNOWN_EXCEPTION: {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_unknown_exception),
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void clearStatus() {
        mStatus.setText("");
    }

    @Override
    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
        if (mServiceHelper.checkCommandClass(requestIntent, GetUserIdCommand.class)) {
            if (resultCode == GetUserIdCommand.RESPONSE_SUCCESS) {
                clearStatus();

                String userId = data.getString(Constants.KEY_USER_ID);
                if (!"".equals(userId)) {
                    mStatus.setText(getString(R.string.status_loading));
                    mGetPhotosDataRequestId = mServiceHelper.getPhotosData(userId);
                } else {
                    setUiEnabled(true);
                    clearStatus();
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.toast_user_not_found), Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == GetUserIdCommand.RESPONSE_PROGRESS) {
                // TODO For the future
            } else if (resultCode == GetUserIdCommand.RESPONSE_FAILURE) {
                clearStatus();
                setUiEnabled(true);
                int exceptionCode = data.getInt(Constants.KEY_EXCEPTION_CODE);
                processException(exceptionCode);
            }
        }

        if (mServiceHelper.checkCommandClass(requestIntent, GetPhotosDataCommand.class)) {
            if (resultCode == GetPhotosDataCommand.RESPONSE_SUCCESS) {
                setUiEnabled(true);
                clearStatus();

                ArrayList<InstagramImageData> images = data.getParcelableArrayList(Constants.KEY_IMAGES_DATA);
                Intent intent = new Intent(this, SelectActivity.class);
                intent.putParcelableArrayListExtra(Constants.KEY_IMAGES_DATA, images);
                startActivity(intent);
            } else if (resultCode == GetUserIdCommand.RESPONSE_PROGRESS) {
                // TODO For the future
            } else if (resultCode == GetUserIdCommand.RESPONSE_FAILURE) {
                clearStatus();
                setUiEnabled(true);
                int exceptionCode = data.getInt(Constants.KEY_EXCEPTION_CODE);
                processException(exceptionCode);
            }
        }
    }
}
