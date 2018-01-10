package com.android.startupmenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class KeepReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent in = new Intent(context, KeepService.class);
        context.startService(in);
    }
}
