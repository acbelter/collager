package com.acbelter.collager.network;

import android.content.Intent;

import com.acbelter.nslib.BaseNetworkServiceHelper;
import com.acbelter.nslib.NetworkServiceCallbackListener;
import com.acbelter.nslib.NetworkServiceHelper;
import com.acbelter.nslib.command.BaseNetworkServiceCommand;

public class CollagerNetworkServiceHelper implements NetworkServiceHelper {
    private BaseNetworkServiceHelper mBaseNetworkServiceHelper;

    public CollagerNetworkServiceHelper(BaseNetworkServiceHelper baseNetworkServiceHelper) {
        mBaseNetworkServiceHelper = baseNetworkServiceHelper;
    }

    public int getUserId(String nick) {
        final int requestId = mBaseNetworkServiceHelper.createCommandId();
        Intent requestIntent = mBaseNetworkServiceHelper.buildRequestIntent(new GetUserIdCommand(nick), requestId);
        return mBaseNetworkServiceHelper.executeRequest(requestId, requestIntent);
    }

    public int getPhotosData(String userId) {
        final int requestId = mBaseNetworkServiceHelper.createCommandId();
        Intent requestIntent = mBaseNetworkServiceHelper.buildRequestIntent(new GetPhotosDataCommand(userId),
                requestId);
        return mBaseNetworkServiceHelper.executeRequest(requestId, requestIntent);
    }

    @Override
    public void addListener(NetworkServiceCallbackListener callback) {
        mBaseNetworkServiceHelper.addListener(callback);
    }

    @Override
    public void removeListener(NetworkServiceCallbackListener callback) {
        mBaseNetworkServiceHelper.removeListener(callback);
    }

    @Override
    public void cancelRequest(int requestId) {
        mBaseNetworkServiceHelper.cancelRequest(requestId);
    }

    @Override
    public boolean checkCommandClass(Intent requestIntent, Class<? extends BaseNetworkServiceCommand> commandClass) {
        return mBaseNetworkServiceHelper.checkCommandClass(requestIntent, commandClass);
    }

    @Override
    public boolean isPending(int requestId) {
        return mBaseNetworkServiceHelper.isPending(requestId);
    }
}

