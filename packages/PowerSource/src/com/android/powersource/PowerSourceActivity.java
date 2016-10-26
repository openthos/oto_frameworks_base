package com.android.powersource;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
        hideStatusBar();
    }

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

    @Override
    protected void onDestroy() {
        finishStatusbarPwoerLayout();
        super.onDestroy();
    }

    private void finishStatusbarPwoerLayout() {
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
        try {
            switch (v.getId()) {
                case R.id.power_off:
                    Runtime.getRuntime().exec("su -c \"/system/xbin/poweroff\"");
                    break;
                case R.id.power_restart:
                    Runtime.getRuntime().exec("su -c \"/system/bin/reboot\"");
                    break;
                case R.id.power_lock:
                    Intent intentLock = new Intent("android.intent.action.LOCKNOW");
                    intentLock.addFlags(Intent.FLAG_RUN_FULLSCREEN | Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intentLock);
                    System.exit(0);
                    break;
                case R.id.power_sleep:
                    PowerSourceActivity.this.finish();
                    break;
                case R.id.power_close:
                    System.exit(0);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
