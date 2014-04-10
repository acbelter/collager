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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.ResultReceiver;
import android.util.SparseArray;

import com.acbelter.nslib.command.BaseNetworkServiceCommand;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkService extends Service {
    public static final String ACTION_EXECUTE_COMMAND = NetworkApplication.PACKAGE
            + ".ACTION_EXECUTE_COMMAND";
    public static final String ACTION_CANCEL_COMMAND = NetworkApplication.PACKAGE
            + ".ACTION_CANCEL_COMMAND";

    public static final String EXTRA_REQUEST_ID = NetworkApplication.PACKAGE
            + ".EXTRA_REQUEST_ID";
    public static final String EXTRA_COMMAND = NetworkApplication.PACKAGE
            + ".EXTRA_COMMAND";
    public static final String EXTRA_RESULT_RECEIVER = NetworkApplication.PACKAGE
            + ".RESULT_RECEIVER";

    private static final int NUM_THREADS = 4;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(NUM_THREADS);
    private final SparseArray<RunningCommand> mRunningCommands = new SparseArray<RunningCommand>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_EXECUTE_COMMAND.equals(intent.getAction())) {
            RunningCommand runningCommand = new RunningCommand(intent);

            synchronized (mRunningCommands) {
                mRunningCommands.append(getRequestId(intent), runningCommand);
            }

            mExecutor.submit(runningCommand);
        } else if (ACTION_CANCEL_COMMAND.equals(intent.getAction())) {
            RunningCommand runningCommand = mRunningCommands.get(getRequestId(intent));
            if (runningCommand != null) {
                runningCommand.cancel();
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutor.shutdownNow();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int getRequestId(Intent requestIntent) {
        return requestIntent.getIntExtra(EXTRA_REQUEST_ID, -1);
    }

    private BaseNetworkServiceCommand getCommand(Intent requestIntent) {
        return requestIntent.getParcelableExtra(EXTRA_COMMAND);
    }

    private ResultReceiver getResultReceiver(Intent requestIntent) {
        return requestIntent.getParcelableExtra(EXTRA_RESULT_RECEIVER);
    }

    private class RunningCommand implements Runnable {
        private Intent mRequestIntent;
        private BaseNetworkServiceCommand mCommand;

        public RunningCommand(Intent requestIntent) {
            mRequestIntent = requestIntent;
            mCommand = getCommand(requestIntent);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            mCommand.execute(getApplicationContext(), mRequestIntent, getResultReceiver(mRequestIntent));
            shutdown();
        }

        private void shutdown() {
            synchronized (mRunningCommands) {
                mRunningCommands.remove(getRequestId(mRequestIntent));
                if (mRunningCommands.size() == 0) {
                    stopSelf();
                }
            }
        }

        public void cancel() {
            mCommand.cancel();
        }
    }
}
