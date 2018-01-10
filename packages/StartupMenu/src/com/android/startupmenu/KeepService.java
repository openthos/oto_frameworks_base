package com.android.startupmenu;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by wang on 6/15/17.
 */

public class KeepService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
