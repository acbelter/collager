package com.acbelter.collager.network;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import com.acbelter.collager.Constants;
import com.acbelter.collager.Utils;
import com.acbelter.nslib.command.BaseNetworkServiceCommand;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class GetUserIdCommand extends BaseNetworkServiceCommand {
    private String mNick;

    public GetUserIdCommand(String nick) {
        mNick = nick;
    }

    private GetUserIdCommand(Parcel in) {
        mNick = in.readString();
    }

    private static String buildSearchIdUrl(String nick) {
        String url = Constants.API_URL + "users/search?q=" + nick + "&client_id=" +
                Constants.CLIENT_ID;
        Log.d("Collager", "SEARCH USER ID URL: " + url);
        return url;
    }

    @Override
    protected void doExecute(Context context, Intent requestIntent, ResultReceiver callback) {
        HttpsURLConnection conn = null;
        Bundle data = new Bundle();
        try {
            URL url = new URL(buildSearchIdUrl(mNick));
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

            JSONArray jsonData = new JSONObject(response).getJSONArray("data");
            for (int i = 0; i < jsonData.length(); i++) {
                if (jsonData.getJSONObject(i).getString("username").equals(mNick)) {
                    String userId = jsonData.getJSONObject(i).getString("id");
                    data.putString(Constants.KEY_USER_ID, userId);
                    notifySuccess(data);
                    return;
                }
            }
        } catch (MalformedURLException e) {
            data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_URL_EXCEPTION);
            notifyFailure(data);
            return;
        } catch (IOException e) {
            data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_IO_EXCEPTION);
            notifyFailure(data);
            return;
        } catch (JSONException e) {
            data.putInt(Constants.KEY_EXCEPTION_CODE, Constants.CODE_JSON_EXCEPTION);
            notifyFailure(data);
            return;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        data.putString(Constants.KEY_USER_ID, "");
        notifySuccess(data);
    }

    public static final Parcelable.Creator<GetUserIdCommand> CREATOR =
            new Parcelable.Creator<GetUserIdCommand>() {
                @Override
                public GetUserIdCommand createFromParcel(Parcel in) {
                    return new GetUserIdCommand(in);
                }

                @Override
                public GetUserIdCommand[] newArray(int size) {
                    return new GetUserIdCommand[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mNick);
    }
}
