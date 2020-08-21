package com.android.systemui.power;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.util.Log;
import java.lang.reflect.Method;

import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.LockReceiver;
import com.android.systemui.R;

public class PowerSourceActivity extends Activity implements View.OnClickListener {

    private static final int WAIT_INTERVAL = 1000; // wait for 1 second.
    private LinearLayout mPowerOff;
    private LinearLayout mPowerSleep;
    private LinearLayout mPowerLock;
    private LinearLayout mPowerRestart;
    private ImageView mPowerClose;
    private IStatusBarService mBarService;

    @Override
    protected void onCreate(Bundle context) {
        super.onCreate(context);
        setContentView(R.layout.activity_power_source);

        mBarService = IStatusBarService.Stub.asInterface(
                ServiceManager.getService(Context.STATUS_BAR_SERVICE));

        initView();
        initListener();

        hideStatusBar();
        shutDownHideStatusBar();
    }

    private void initView() {
        mPowerClose = (ImageView) findViewById(R.id.power_close);
        mPowerOff = (LinearLayout) findViewById(R.id.power_off);
        mPowerSleep = (LinearLayout) findViewById(R.id.power_sleep);
        mPowerLock = (LinearLayout) findViewById(R.id.power_lock);
        mPowerRestart = (LinearLayout) findViewById(R.id.power_restart);
    }

    private void initListener() {
        mPowerClose.setOnClickListener(this);
        mPowerOff.setOnClickListener(this);
        mPowerSleep.setOnClickListener(this);
        mPowerLock.setOnClickListener(this);
        mPowerRestart.setOnClickListener(this);

        mPowerClose.setOnHoverListener(mHoverListener);
        mPowerOff.setOnHoverListener(mHoverListener);
        mPowerSleep.setOnHoverListener(mHoverListener);
        mPowerLock.setOnHoverListener(mHoverListener);
        mPowerRestart.setOnHoverListener(mHoverListener);

        mPowerClose.setOnFocusChangeListener(mFocuschangelistener);
        mPowerOff.setOnFocusChangeListener(mFocuschangelistener);
        mPowerSleep.setOnFocusChangeListener(mFocuschangelistener);
        mPowerLock.setOnFocusChangeListener(mFocuschangelistener);
        mPowerRestart.setOnFocusChangeListener(mFocuschangelistener);
    }

    View.OnFocusChangeListener mFocuschangelistener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                v.setBackgroundResource(R.mipmap.power_setting_background);
            } else {
                v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
        }
    };

    View.OnHoverListener mHoverListener = new View.OnHoverListener() {
        public boolean onHover(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundResource(R.mipmap.power_setting_background);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    break;
            }
            return false;
        }
    };

    private void finishStatusbarPowerLayout() {
    }

    private void showStatusBar() {
        Log.v("MDX","PowerSourceActivity showStatusBar");
    }

    private void hideStatusBar() {
    }

    private void shutDownHideStatusBar() {
    }

    private void closeButtonShowStatusBar() {
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
        final int id = v.getId();
        if (id == R.id.power_off) {
            Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
            intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else if (id == R.id.power_restart) {
            try {
                mBarService.reboot(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.power_lock) {
            DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager)
                getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (mDevicePolicyManager.isAdminActive(LockReceiver.getCn(this))) {
                mDevicePolicyManager.lockNow();
            } else {
                Intent intentLock = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intentLock.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, LockReceiver.getCn(this));
                intentLock.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "lock screen");
                startActivity(intentLock);
            }
            finish();
        } else if (id == R.id.power_sleep) {
            PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            powerManager.goToSleep(SystemClock.uptimeMillis());
            finish();
        } else if (id == R.id.power_close) {
            closeButtonShowStatusBar();
        }
    }
}
