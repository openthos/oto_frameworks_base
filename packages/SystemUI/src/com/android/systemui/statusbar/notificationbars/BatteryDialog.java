package com.android.systemui.statusbar.notificationbars;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.systemui.R;

import android.content.Intent;

import com.android.internal.os.BatteryStatsHelper;
import com.android.systemui.statusbar.policy.BatteryController;

import android.os.BatteryStats;
import android.os.UserManager;

import java.util.List;

import android.os.UserHandle;
import android.os.SystemClock;
import android.text.format.Formatter;

public class BatteryDialog extends BaseSettingDialog
                           implements BatteryController.BatteryStateChangeCallback {
    private static final String BATTERY_HISTORY_FILE = "tmp_bat_history.bin";
    private static final int REMAIN_HOUR_DEFAULT = 3;
    private static final int REMAIN_MINUTE_DEFAULT = 30;
    private TextView mBatteryPercentage;
    private TextView mBatteryRemaining;
    private TextView mBatterySavingMode;
    private BatteryStatsHelper mBatteryStatsHelper;
    private UserManager mUserManager;
    private BatteryStats mBatteryStats;
    private BatteryController mBatteryController;

    public BatteryDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void show(View v) {
        super.show(v);
    }

    @Override
    protected void initViews() {
        View mediaView = LayoutInflater.from(mContext).inflate(R.layout.status_bar_battery, null);
        setContentView(mediaView);
        mBatteryPercentage = (TextView) mediaView.findViewById(R.id.battery_time_percentage);
        mBatteryRemaining = (TextView) mediaView.findViewById(R.id.battery_time_remaining);
        mBatterySavingMode = (TextView) mediaView.findViewById(R.id.battery_time_enter);
        mUserManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
        mBatteryStatsHelper = new BatteryStatsHelper(mContext);
        mBatteryStatsHelper.create(new Bundle());
        updataBatteryRemaining();
        mContentView = mediaView;
        mBatterySavingMode.setText(R.string.battery_saver_settings);
        mBatterySavingMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.startActivity(new Intent("com.android.settings.POWERMANAGER_SETTINGS")
                       .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                dismiss();
            }
        });
        mBatteryController = new BatteryController(mContext);
        mBatteryController.addStateChangedCallback(this);
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        updataBatteryRemaining();
        if (level == 0) {
            mBatteryPercentage.setVisibility(View.GONE);
            mBatteryRemaining.setVisibility(View.GONE);
        } else if (charging || pluggedIn) {
              mBatteryPercentage.setVisibility(View.VISIBLE);
              mBatteryRemaining.setVisibility(View.GONE);
              if (level < 100) {
                  mBatteryPercentage.setText(mContext.getResources().getString(
                          R.string.battery_percent, level) + "%");
              } else {
                  mBatteryPercentage.setText(R.string.battery_percent_full);
              }
        } else {
            mBatteryPercentage.setVisibility(View.VISIBLE);
            mBatteryRemaining.setVisibility(View.VISIBLE);
            String strBatteryRemaining;
            long elapsedRealtime = SystemClock.elapsedRealtime() * 1000;
            long batteryTimeRemaining = mBatteryStats.
                    computeBatteryTimeRemaining(elapsedRealtime);
            if (batteryTimeRemaining < 0) {
                strBatteryRemaining = mContext.getResources().getString(
                        R.string.battery_remaining,
                        REMAIN_HOUR_DEFAULT,
                        REMAIN_MINUTE_DEFAULT);
            } else {
                String batteryString = Formatter.formatShortElapsedTime(getContext(),
                        batteryTimeRemaining / 1000);
                strBatteryRemaining = mContext.getResources().getString(
                        R.string.battery_remaining_string, batteryString);
            }
            mBatteryPercentage.setText(mContext.getResources().getString(
                    R.string.battery_percent, level) + "%");
            mBatteryRemaining.setText(strBatteryRemaining);
        }
    }

    @Override
    public void onPowerSaveChanged() {

    }

    private void updataBatteryRemaining() {
        BatteryStatsHelper.dropFile(mContext, BATTERY_HISTORY_FILE);
        List<UserHandle> profiles = mUserManager.getUserProfiles();
        mBatteryStatsHelper.clearStats();
        mBatteryStats = mBatteryStatsHelper.getStats();
        mBatteryStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, profiles);
    }
}
