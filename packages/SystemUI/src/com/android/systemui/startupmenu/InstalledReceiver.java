package com.android.systemui.startupmenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.systemui.startupmenu.util.SqliteOperate;

public class InstalledReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String pkName = intent.getData().getSchemeSpecificPart();
            SqliteOperate.deleteDataStorage(context, pkName);
        }
    }
}
