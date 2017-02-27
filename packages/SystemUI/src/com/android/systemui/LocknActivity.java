package com.android.systemui;

import com.android.systemui.LockReceiver;
import com.android.systemui.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;

public class LocknActivity extends Activity {
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(LocknActivity.this, LockReceiver.class);
        if (mDevicePolicyManager.isAdminActive(mComponentName)) {
            sendLockScreenShowBar();
            mDevicePolicyManager.lockNow();
            finish();
        } else {
            activeManager();
        }
    }

    private void activeManager() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "set the lock screen");
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void sendLockScreenShowBar() {
        Intent intent = new Intent();
        intent.setAction(Intent.LOCK_SCREEN_SHOW_STATUS_BAR);
        sendBroadcast(intent);
    }
}
