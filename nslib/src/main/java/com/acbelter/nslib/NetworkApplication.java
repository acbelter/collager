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
import android.content.Context;

public class NetworkApplication extends Application {
    public static final String PACKAGE = "com.acbelter.nslib";

    protected BaseNetworkServiceHelper mServiceHelper;

    public static NetworkApplication getApplication(Context context) {
        if (context instanceof NetworkApplication) {
            return (NetworkApplication) context;
        }
        return (NetworkApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceHelper = new BaseNetworkServiceHelper(this);
    }

    public BaseNetworkServiceHelper getNetworkServiceHelper() {
        return mServiceHelper;
    }
}
