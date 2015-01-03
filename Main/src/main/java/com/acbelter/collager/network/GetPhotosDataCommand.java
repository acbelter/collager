package com.acbelter.collager.network;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import com.acbelter.collager.Constants;
import com.acbelter.collager.InstagramImageData;
import com.acbelter.collager.Utils;
import com.acbelter.nslib.command.BaseNetworkServiceCommand;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GetPhotosDataCommand extends BaseNetworkServiceCommand {
    private static final int MAX_IMAGES = 100;
    private String mUserId;

    public GetPhotosDataCommand(String userId) {
        mUserId = userId;
    }

    private GetPhotosDataCommand(Parcel in) {
        mUserId = in.readString();
    }

    private static String buildGetPhotosUrl(String userId) {
        return Constants.API_URL + "users/" + userId + "/media/recent/?client_id=" +
                Constants.CLIENT_ID + "&count=" + 20;
    }

    private static ArrayList<InstagramImageData> parseImagesData(JSONObject jsonObj)
            throws JSONException {
        JSONArray jsonData = jsonObj.getJSONArray("data");
        ArrayList<InstagramImageData> images = new ArrayList<InstagramImageData>(jsonData.length());
        String likes, link;
        for (int i = 0; i < jsonData.length(); i++) {
            likes = jsonData.getJSONObject(i).getJSONObject("likes").getString("count");
            link = jsonData.getJSONObject(i).getJSONObject("images")
                    .getJSONObject(Constants.IMAGE_SIZE_TYPE).getString("url");
            link.replaceAll("\\\\", "");
            images.add(new InstagramImageData(Integer.parseInt(likes), link));
        }
        return images;
    }

    @Override
    protected void doExecute(Context context, Intent requestIntent, ResultReceiver callback) {
        HttpsURLConnection conn = null;
        Bundle data = new Bundle();
        try {
            ArrayList<InstagramImageData> images = new ArrayList<InstagramImageData>(MAX_IMAGES);
            URL url = new URL(buildGetPhotosUrl(mUserId));
            while (images.size() != MAX_IMAGES) {
                if (mCancelled) {
                    return;
                }

                Log.d("Collager", "GET PHOTOS URL: " + url);
                conn = (HttpsURLConnection) url.openConnection();
                if (conn.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                    data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_ACCESS_EXCEPTION);
                    notifyFailure(data);
                    return;
                }

                String response = Utils.readToString(conn.getInputStream());
                if (response == null) {
                    data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_UNKNOWN_EXCEPTION);
                    notifyFailure(data);
                    return;
                }

                JSONObject jsonObj = new JSONObject(response);
                ArrayList<InstagramImageData> tempImages = parseImagesData(jsonObj);
                for (int i = 0; i < tempImages.size(); i++) {
                    if (images.size() == MAX_IMAGES) {
                        data.putParcelableArrayList(Constants.KEY_IMAGES_DATA, images);
                        notifySuccess(data);
                        return;
                    } else {
                        images.add(tempImages.get(i));
                    }
                }

                JSONObject paginationObj = jsonObj.getJSONObject("pagination");
                String nextUrlStr = null;
                if (!paginationObj.isNull("next_url")) {
                    nextUrlStr = paginationObj.getString("next_url");
                }
                if (nextUrlStr != null) {
                    nextUrlStr.replaceAll("\\\\", "");
                    URL nextUrl = new URL(nextUrlStr);
                    if (!url.equals(nextUrl)) {
                        conn.disconnect();
                        url = nextUrl;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }

            if (mCancelled) {
                return;
            }

            data.putParcelableArrayList(Constants.KEY_IMAGES_DATA, images);
            notifySuccess(data);
        } catch (MalformedURLException e) {
            data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_URL_EXCEPTION);
            notifyFailure(data);
        } catch (IOException e) {
            data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_IO_EXCEPTION);
            notifyFailure(data);
        } catch (JSONException e) {
            data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_JSON_EXCEPTION);
            notifyFailure(data);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static final Parcelable.Creator<GetPhotosDataCommand> CREATOR =
            new Parcelable.Creator<GetPhotosDataCommand>() {
                @Override
                public GetPhotosDataCommand createFromParcel(Parcel in) {
                    return new GetPhotosDataCommand(in);
                }

                @Override
                public GetPhotosDataCommand[] newArray(int size) {
                    return new GetPhotosDataCommand[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mUserId);
    }
}
