package com.android.systemui.dialog;

import android.content.ComponentName;
import android.content.Context;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.internal.os.BatteryStatsHelper;
import com.android.systemui.R;
import com.android.systemui.startupmenu.U;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryControllerImpl;

import java.util.List;

public class BatteryDialog extends BaseDialog implements BatteryController.BatteryStateChangeCallback {
    private static final String BATTERY_SETTINGS = "com.android.settings";
    private static final String BATTERY_SETTINGS_SAVER = BATTERY_SETTINGS
	                        + ".fuelgauge.BatterySaverSettings";
    private static final String BATTERY_HISTORY_FILE = "tmp_bat_history.bin";
    private static final int REMAIN_HOUR_DEFAULT = 3;
    private static final int REMAIN_MINUTE_DEFAULT = 30;
    private TextView mBatteryPercentage;
    private TextView mBatteryRemaining;
    private TextView mBatterySavingMode;
    private BatteryStatsHelper mBatteryStatsHelper;
    private UserManager mUserManager;
    private BatteryStats mBatteryStats;
    private BatteryControllerImpl mBatteryController;

    public BatteryDialog(Context context) {
        super(context);
        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.status_bar_battery, null);
        setContentView(mContentView);
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
    public void initView() {
        mBatteryPercentage = (TextView) mContentView.findViewById(R.id.battery_time_percentage);
        mBatteryRemaining = (TextView) mContentView.findViewById(R.id.battery_time_remaining);
        mBatterySavingMode = (TextView) mContentView.findViewById(R.id.battery_time_enter);
    }

    @Override
    public void initData() {
        mUserManager = (UserManager) getContext().getSystemService(Context.USER_SERVICE);
        mBatteryStatsHelper = new BatteryStatsHelper(getContext());
        mBatteryStatsHelper.create(new Bundle());
        updataBatteryRemaining();
        mBatterySavingMode.setText(R.string.battery_saver_settings);
        mBatteryController = new BatteryControllerImpl(getContext());
    }

    @Override
    public void initListener() {
        mBatterySavingMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                U.launchApp(getContext(),
                        new ComponentName(BATTERY_SETTINGS, BATTERY_SETTINGS_SAVER));
                dismiss();
            }
        });

        mBatteryController.addCallback(this);
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
            if (level == 100) {
                mBatteryPercentage.setText(R.string.battery_percent_full);
            } else {
                mBatteryPercentage.setText(getContext().getResources().getString(
                        R.string.battery_percent, level) + "%");
            }
        } else {
            mBatteryPercentage.setVisibility(View.VISIBLE);
            mBatteryRemaining.setVisibility(View.VISIBLE);
            String strBatteryRemaining;
            long elapsedRealtime = SystemClock.elapsedRealtime() * 1000;
            long batteryTimeRemaining = mBatteryStats.
                    computeBatteryTimeRemaining(elapsedRealtime);
            if (batteryTimeRemaining < 0) {
                strBatteryRemaining = getContext().getResources().getString(
                        R.string.battery_remaining,
                        REMAIN_HOUR_DEFAULT,
                        REMAIN_MINUTE_DEFAULT);
            } else {
                String batteryString = Formatter.formatShortElapsedTime(getContext(),
                        batteryTimeRemaining / 1000);
                strBatteryRemaining = getContext().getResources().getString(
                        R.string.battery_remaining_string, batteryString);
            }
            mBatteryPercentage.setText(getContext().getResources().getString(
                    R.string.battery_percent, level) + "%");
            mBatteryRemaining.setText(strBatteryRemaining);
        }
    }

    @Override
    public void onPowerSaveChanged(boolean isPowerSave) {

    }

    private void updataBatteryRemaining() {
        BatteryStatsHelper.dropFile(getContext(), BATTERY_HISTORY_FILE);
        List<UserHandle> profiles = mUserManager.getUserProfiles();
        mBatteryStatsHelper.clearStats();
        mBatteryStats = mBatteryStatsHelper.getStats();
        mBatteryStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, profiles);
    }
}
