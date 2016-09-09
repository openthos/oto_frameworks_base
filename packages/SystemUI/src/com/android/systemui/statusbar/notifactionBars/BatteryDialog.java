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

public class BatteryDialog extends BaseSettingDialog {
    private TextView mBatteryPercentage;
    private TextView mBatteryRemaining;

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
        mBatteryPercentage.setText(R.string.wifi_unable);
        mBatteryRemaining.setText(R.string.wifi_unable);
        mContentView = mediaView;
    }
}
