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

package com.acbelter.nslib.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;

import com.acbelter.nslib.NetworkApplication;

public abstract class BaseNetworkServiceCommand implements Parcelable {
    public static final int RESPONSE_SUCCESS = 0;
    public static final int RESPONSE_FAILURE = 1;
    public static final int RESPONSE_PROGRESS = 2;
    public static String EXTRA_PROGRESS = NetworkApplication.PACKAGE + ".EXTRA_PROGRESS";
    protected volatile boolean mCancelled;
    private ResultReceiver mCallback;

    public final void execute(Context context, Intent requestIntent, ResultReceiver callback) {
        mCallback = callback;
        doExecute(context, requestIntent, callback);
    }

    protected abstract void doExecute(Context context, Intent requestIntent, ResultReceiver callback);

    protected void notifySuccess(Bundle data) {
        sendUpdate(RESPONSE_SUCCESS, data);
    }

    protected void notifyFailure(Bundle data) {
        sendUpdate(RESPONSE_FAILURE, data);
    }

    protected void sendProgress(int progress) {
        Bundle data = new Bundle();
        data.putInt(EXTRA_PROGRESS, progress);
        sendUpdate(RESPONSE_PROGRESS, data);
    }

    private void sendUpdate(int resultCode, Bundle data) {
        if (mCallback != null) {
            mCallback.send(resultCode, data);
        }
    }

    public synchronized void cancel() {
        mCancelled = true;
    }
}
