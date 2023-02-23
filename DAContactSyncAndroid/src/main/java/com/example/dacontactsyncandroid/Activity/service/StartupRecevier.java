package com.example.dacontactsyncandroid.Activity.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class StartupRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg) {
        Intent intent = new Intent(context, ContactService.class);
        Log.d("TAG", "overviewService: yyyServicestart1111");
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Log.d("TAG", "overviewService: yyyServicestart");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }
}