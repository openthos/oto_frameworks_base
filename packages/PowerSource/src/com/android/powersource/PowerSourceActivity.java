package com.android.powersource;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.PowerManager;
import java.lang.reflect.Method;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.MotionEvent;
import android.view.View.OnHoverListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;

public class PowerSourceActivity extends Activity implements OnClickListener {

    private static final String TAG = "PowerSourceActivity";
    private static final int WAIT_INTERVAL = 1000; // wait for 1 second.

    @Override
    protected void onCreate(Bundle context) {
        super.onCreate(context);
        setContentView(R.layout.power_source_activity);

        View view = findViewById(R.id.power_source);
        ImageView powerClose = (ImageView) view.findViewById(R.id.power_close);
        LinearLayout powerOff = (LinearLayout) view.findViewById(R.id.power_off);
        LinearLayout powerSleep = (LinearLayout) view.findViewById(R.id.power_sleep);
        LinearLayout powerLock = (LinearLayout) view.findViewById(R.id.power_lock);
        LinearLayout powerRestart = (LinearLayout) view.findViewById(R.id.power_restart);
        powerClose.setOnClickListener(this);
        powerOff.setOnClickListener(this);
        powerSleep.setOnClickListener(this);
        powerLock.setOnClickListener(this);
        powerRestart.setOnClickListener(this);

        powerClose.setOnHoverListener(hoverListeners);
        powerOff.setOnHoverListener(hoverListeners);
        powerSleep.setOnHoverListener(hoverListeners);
        powerLock.setOnHoverListener(hoverListeners);
        powerRestart.setOnHoverListener(hoverListeners);

        powerClose.setOnFocusChangeListener(focusChangeListener);
        powerOff.setOnFocusChangeListener(focusChangeListener);
        powerSleep.setOnFocusChangeListener(focusChangeListener);
        powerLock.setOnFocusChangeListener(focusChangeListener);
        powerRestart.setOnFocusChangeListener(focusChangeListener);

        hideStatusBar();
        shutDownHideStatusBar();
    }

    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                v.setBackgroundResource(R.drawable.power_setting_background);
            } else {
                v.setBackgroundColor(android.R.color.transparent);
            }
        }
    };

    View.OnHoverListener hoverListeners= new View.OnHoverListener() {
        public boolean onHover(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundResource(R.drawable.power_setting_background);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundColor(android.R.color.transparent);
                    break;
            }
            return false;
        }
    };

    private void finishStatusbarPowerLayout() {
        Intent intent = new Intent();
        intent.setAction(Intent.STATUS_BAR_POWER_SLEEP);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        sendBroadcast(intent);
    }

    private void showStatusBar() {
        Intent intent = new Intent();
        intent.setAction(Intent.STATUS_BAR_SHOW_SUGGEST);
        sendBroadcast(intent);
    }

    private void hideStatusBar() {
        Intent intent = new Intent();
        intent.setAction(Intent.STATUS_BAR_HIDE_MARKLESS);
        sendBroadcast(intent);
    }

    private void shutDownHideStatusBar() {
        Intent intent = new Intent();
        intent.setAction(Intent.STATUS_BAR_HIDE_BOOT_EXIT);
        sendBroadcast(intent);
    }

    private void closeButtonShowStatusBar() {
        Intent intent = new Intent();
        intent.setAction(Intent.STATUS_BAR_SHOW_FINISH_ACTIVITY);
        sendBroadcast(intent);
    }

    private void waitForAWhile() {
        try {
            Thread.sleep(WAIT_INTERVAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        showStatusBar();
        switch (v.getId()) {
            case R.id.power_off:
                try {
                    Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
                    Method getService =
                           ServiceManager.getMethod("getService", java.lang.String.class);
                    Object oRemoteService =
                           getService.invoke(null,Context.POWER_SERVICE);
                    Class<?> cStub =
                           Class.forName("android.os.IPowerManager$Stub");
                    Method asInterface =
                           cStub.getMethod("asInterface", android.os.IBinder.class);
                    Object oIPowerManager =
                           asInterface.invoke(null, oRemoteService);
                    Method shutdown =
                           oIPowerManager.getClass().getMethod("shutdown",
                                                               boolean.class, boolean.class);
                    shutdown.invoke(oIPowerManager,false,true);
                    //Runtime.getRuntime().exec("su -c \"/system/xbin/poweroff\"");
                } catch(Exception e) {}
                break;
            case R.id.power_restart:
                PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
                pm.reboot("true");
                //Runtime.getRuntime().exec("su -c \"/system/bin/reboot\"");
                break;
            case R.id.power_lock:
                closeButtonShowStatusBar();
                Intent intentLock = new Intent("android.intent.action.LOCKNOW");
                intentLock.addFlags(Intent.FLAG_RUN_FULLSCREEN | Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentLock);
                System.exit(0);
            case R.id.power_sleep:
                closeButtonShowStatusBar();
                finishStatusbarPowerLayout();
                PowerSourceActivity.this.finish();
                break;
            case R.id.power_close:
                closeButtonShowStatusBar();
                System.exit(0);
        }
    }
}
