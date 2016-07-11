package com.example.valverde.stopwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceRestarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, StopwatchService.class));
    }
}