package com.acbelter.nslib;

import android.content.Intent;

import com.acbelter.nslib.command.BaseNetworkServiceCommand;

public interface NetworkServiceHelper {
    void addListener(NetworkServiceCallbackListener callback);

    void removeListener(NetworkServiceCallbackListener callback);

    void cancelRequest(int requestId);

    boolean checkCommandClass(Intent requestIntent,
                              Class<? extends BaseNetworkServiceCommand> commandClass);

    boolean isPending(int requestId);
}
