/*
 * Copyright (C) 2013 Alexander Osmanov (http://perfectear.educkapps.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acbelter.nslib;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.SparseArray;

import com.acbelter.nslib.command.BaseNetworkServiceCommand;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseNetworkServiceHelper implements NetworkServiceHelper {
    private ArrayList<NetworkServiceCallbackListener> mCallbacks;
    private AtomicInteger mRequestCounter;
    private SparseArray<Intent> mPendingRequests;
    private Application mApplication;

    public BaseNetworkServiceHelper(Application application) {
        mApplication = application;
        mCallbacks = new ArrayList<NetworkServiceCallbackListener>();
        mRequestCounter = new AtomicInteger();
        mPendingRequests = new SparseArray<Intent>();
    }

    @Override
    public void addListener(NetworkServiceCallbackListener callback) {
        mCallbacks.add(callback);
    }

    @Override
    public void removeListener(NetworkServiceCallbackListener callback) {
        mCallbacks.remove(callback);
    }

    @Override
    public void cancelRequest(int requestId) {
        Intent cancelIntent = new Intent(mApplication, NetworkService.class);
        cancelIntent.setAction(NetworkService.ACTION_CANCEL_COMMAND);
        cancelIntent.putExtra(NetworkService.EXTRA_REQUEST_ID, requestId);

        mApplication.startService(cancelIntent);
        mPendingRequests.remove(requestId);
    }

    @Override
    public boolean checkCommandClass(Intent requestIntent,
                                     Class<? extends BaseNetworkServiceCommand> commandClass) {
        Parcelable commandExtra = requestIntent.getParcelableExtra(NetworkService.EXTRA_COMMAND);
        return commandExtra != null && commandExtra.getClass().equals(commandClass);
    }

    public int createCommandId() {
        return mRequestCounter.getAndIncrement();
    }

    @Override
    public boolean isPending(int requestId) {
        return mPendingRequests.get(requestId) != null;
    }

    public int executeRequest(int requestId, Intent requestIntent) {
        mPendingRequests.append(requestId, requestIntent);
        mApplication.startService(requestIntent);
        return requestId;
    }

    public Intent buildRequestIntent(BaseNetworkServiceCommand command, final int requestId) {
        Intent requestIntent = new Intent(mApplication, NetworkService.class);
        requestIntent.setAction(NetworkService.ACTION_EXECUTE_COMMAND);

        requestIntent.putExtra(NetworkService.EXTRA_COMMAND, command);
        requestIntent.putExtra(NetworkService.EXTRA_REQUEST_ID, requestId);
        requestIntent.putExtra(NetworkService.EXTRA_RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (isPending(requestId)) {
                    Intent pendingRequest = mPendingRequests.get(requestId);

                    for (int i = 0; i < mCallbacks.size(); i++) {
                        if (mCallbacks.get(i) != null) {
                            mCallbacks.get(i).onServiceCallback(requestId, pendingRequest, resultCode, resultData);
                        }
                    }

                    if (resultCode != BaseNetworkServiceCommand.RESPONSE_PROGRESS) {
                        mPendingRequests.remove(requestId);
                    }
                }
            }
        });
        return requestIntent;
    }
}
