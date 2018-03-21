package com.otosoft.setupwizard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.provider.Settings;
import android.util.DisplayMetrics;

public class DisplayActivity extends BaseActivity implements View.OnClickListener{

    private static final String SYSTEM_DPI = "system_dpi";
    private static final String DPI_INFO_IN_CONFIG = "sys.sf.lcd_density.recommend";
    private static final int NUM_DPI_LOW = 120;
    private static final int NUM_DPI_MEDIUM = 160;
    private static final int NUM_DPI_HIGH = 240;
    private TextView mLDpi;
    private TextView mMDpi;
    private TextView mHDpi;
    private int mSelectColor;
    private int mNoSelectColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        init();
    }

    private void init() {
        mLDpi = (TextView) findViewById(R.id.tv_dpi_low);
        mMDpi = (TextView) findViewById(R.id.tv_dpi_medium);
        mHDpi = (TextView) findViewById(R.id.tv_dpi_high);
        if (filterDisplayMetrics()) {
            mHDpi.setEnabled(false);
            mHDpi.setTextColor(Color.GRAY);
        }
        findViewById(R.id.tv_next).setOnClickListener(this);
        findViewById(R.id.tv_prev).setOnClickListener(this);
        mLDpi.setOnClickListener(this);
        mMDpi.setOnClickListener(this);
        mHDpi.setOnClickListener(this);

        mSelectColor = getResources().getColor(R.color.selected_bg);
        mNoSelectColor = getResources().getColor(R.color.no_secleted_bg);
        int dpi = Settings.System.getInt(getContentResolver(), SYSTEM_DPI, NUM_DPI_MEDIUM);
        switch (dpi) {
            case 120:
                mLDpi.setBackgroundColor(mSelectColor);
                break;
            case 160:
                mMDpi.setBackgroundColor(mSelectColor);
                break;
            case 240:
                mHDpi.setBackgroundColor(mSelectColor);
                break;
        }
    }

    private boolean filterDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels <= 1366 && displayMetrics.heightPixels <= 768;
    }

    private Intent buildWifiSetupIntent() {
        Intent intent = new Intent("com.android.net.wifi.SETUP_WIFI_NETWORK");
        intent.putExtra("firstRun", true);
        intent.putExtra("allowSkip", true);
        intent.putExtra("useImmersiveMode", true);
        intent.putExtra("theme", "material_light");
        intent.putExtra("wifi_auto_finish_on_connect", false);
        intent.putExtra("scriptUri", "NotUsedNow");
        return intent;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_dpi_low:
                mLDpi.setBackgroundColor(mSelectColor);
                mMDpi.setBackgroundColor(mNoSelectColor);
                mHDpi.setBackgroundColor(mNoSelectColor);
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, NUM_DPI_LOW);
                SystemProperties.set(DPI_INFO_IN_CONFIG, String.valueOf(NUM_DPI_LOW));
                break;
            case R.id.tv_dpi_medium:
                mLDpi.setBackgroundColor(mNoSelectColor);
                mMDpi.setBackgroundColor(mSelectColor);
                mHDpi.setBackgroundColor(mNoSelectColor);
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, NUM_DPI_MEDIUM);
                SystemProperties.set(DPI_INFO_IN_CONFIG, String.valueOf(NUM_DPI_MEDIUM));
                break;
            case R.id.tv_dpi_high:
                mLDpi.setBackgroundColor(mNoSelectColor);
                mMDpi.setBackgroundColor(mNoSelectColor);
                mHDpi.setBackgroundColor(mSelectColor);
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, NUM_DPI_HIGH);
                SystemProperties.set(DPI_INFO_IN_CONFIG, String.valueOf(NUM_DPI_HIGH));
                break;
            case R.id.tv_prev:
                onBackPressed();
                break;
            case R.id.tv_next:
                startActivity(buildWifiSetupIntent());
                break;
        }
    }
}
