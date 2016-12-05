package com.android.systemui.statusbar.notificationbars;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.R;
import android.content.BroadcastReceiver;
import android.os.BatteryManager;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.internal.os.BatteryStatsHelper;
import android.os.BatteryStats;
import android.os.UserManager;
import java.util.List;
import android.os.UserHandle;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.provider.Settings;

public class BatteryDialog extends BaseSettingDialog {
    private static final String BATTERY_HISTORY_FILE = "tmp_bat_history.bin";
    private static final int REMAIN_HOUR_DEFAULT = 3;
    private static final int REMAIN_MINUTE_DEFAULT = 30;
    private TextView mBatteryPercentage;
    private TextView mBatteryRemaining;
    private TextView mBatterySavingMode;
    private BatteryReceive batteryReceive;
    private BatteryStatsHelper mBatteryStatsHelper;
    private UserManager mUserManager;
    private BatteryStats mBatteryStats;

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
        final AudioManager audioManager = (AudioManager) mContext.getSystemService(
                                                                      Context.AUDIO_SERVICE);
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
                BatteryDialog.this.dismiss();
            }
        });
    }

    private class BatteryReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = (int)(100f * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                            / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
                updataBatteryRemaining();
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                if (status == BatteryManager.BATTERY_STATUS_FULL) {
                    mBatteryPercentage.setText(R.string.battery_percent_full);
                    mBatteryRemaining.setVisibility(View.GONE);
                } else {
                    mBatteryRemaining.setVisibility(View.VISIBLE);
                    mBatteryPercentage.setText(mContext.getResources().getString(
                                               R.string.battery_percent, level) + "%");
                    String strBatteryRemaining;
                    long elapsedRealtime = SystemClock.elapsedRealtime() * 1000;
                    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
                    if (isCharging) {
                        long chargeTimeRemaining = mBatteryStats.
                                                  computeChargeTimeRemaining(elapsedRealtime);
                        if (chargeTimeRemaining < 0) {
                            strBatteryRemaining = mContext.getResources().getString(
                                                  R.string.charge_remaining,
                                                  REMAIN_HOUR_DEFAULT,
                                                  REMAIN_MINUTE_DEFAULT);
                        } else {
                            String chargeString = Formatter.formatShortElapsedTime(getContext(),
                                                  chargeTimeRemaining / 1000);
                            strBatteryRemaining = mContext.getResources().getString(
                                                  R.string.charge_remaining_string, chargeString);
                        }
                    } else {
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
                    }
                    mBatteryRemaining.setText(strBatteryRemaining);
                }
            }
        }
    }

    private void updataBatteryRemaining () {
        BatteryStatsHelper.dropFile(mContext, BATTERY_HISTORY_FILE);
        List<UserHandle> profiles = mUserManager.getUserProfiles();
        mBatteryStatsHelper.clearStats();
        mBatteryStats = mBatteryStatsHelper.getStats();
        mBatteryStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, profiles);
    }

    @Override
    protected void onStart() {
        super.onStart();
        batteryReceive = new BatteryReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(batteryReceive, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mContext.unregisterReceiver(batteryReceive);
    }
}
