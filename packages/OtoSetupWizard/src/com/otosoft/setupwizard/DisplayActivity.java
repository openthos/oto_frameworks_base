package com.otosoft.setupwizard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RadioButton;

public class DisplayActivity extends BaseActivity implements View.OnClickListener {

    private static final String SYSTEM_DPI = "system_dpi";
    private static final String DPI_INFO_IN_CONFIG = "sys.sf.lcd_density.recommend";
    private static final int NUM_DPI_LOW = 120;
    private static final int NUM_DPI_MEDIUM = 160;
    private static final int NUM_DPI_HIGH = 240;

    private static final int LAPTOP_1366 = 0;
    private static final int LAPTOP_1920 = 1;
    private static final int LAPTOP_2560 = 2;
    private static final int DESKTOP_1920 = 3;
    private static final int DESKTOP_2560 = 4;
    private static final int DESKTOP_3840 = 5;
    private static final int FHD_TV = 6;
    private static final int UHD_TV = 7;
    private RadioButton mL1366;
    private RadioButton mL1920;
    private RadioButton mL2560;
    private RadioButton mD1920;
    private RadioButton mD2560;
    private RadioButton mD3840;
    private RadioButton mFHD;
    private RadioButton mUHD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        init();
    }

    private boolean filtDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        if (widthPixels <= 1366 && heightPixels <= 768) {
            return true;
        }
        return false;
    }

    private void init() {
        mL1366 = (RadioButton) findViewById(R.id.tv_laptop_1366);
        mL1920 = (RadioButton) findViewById(R.id.tv_laptop_1920);
        mL2560 = (RadioButton) findViewById(R.id.tv_laptop_2560);
        mD1920 = (RadioButton) findViewById(R.id.tv_desktop_1920);
        mD2560 = (RadioButton) findViewById(R.id.tv_desktop_2560);
        mD3840 = (RadioButton) findViewById(R.id.tv_deskptop_3840);
        mFHD = (RadioButton) findViewById(R.id.tv_fhd);
        mUHD = (RadioButton) findViewById(R.id.tv_uhd);
        findViewById(R.id.tv_next).setOnClickListener(this);
        findViewById(R.id.tv_prev).setOnClickListener(this);
        mL1366.setOnClickListener(this);
        mL1920.setOnClickListener(this);
        mL2560.setOnClickListener(this);
        mD1920.setOnClickListener(this);
        mD2560.setOnClickListener(this);
        mD3840.setOnClickListener(this);
        mFHD.setOnClickListener(this);
        mUHD.setOnClickListener(this);

        int index = Settings.System.getInt(getContentResolver(), SYSTEM_DPI, LAPTOP_1920);
        if (filtDisplayMetrics()) {
            mL2560.setEnabled(false);
            mD2560.setEnabled(false);
            mD3840.setEnabled(false);
            mFHD.setEnabled(false);
            mUHD.setEnabled(false);
            mL2560.setTextColor(Color.GRAY);
            mD2560.setTextColor(Color.GRAY);
            mD3840.setTextColor(Color.GRAY);
            mFHD.setTextColor(Color.GRAY);
            mUHD.setTextColor(Color.GRAY);
            index = LAPTOP_1366;
        }
        switch (index) {
            case LAPTOP_1366:
                mL1366.setChecked(true);
                break;
            case LAPTOP_1920:
                mL1920.setChecked(true);
                break;
            case LAPTOP_2560:
                mL2560.setChecked(true);
                break;
            case DESKTOP_1920:
                mD1920.setChecked(true);
                break;
            case DESKTOP_2560:
                mD2560.setChecked(true);
                break;
            case DESKTOP_3840:
                mD3840.setChecked(true);
                break;
            case FHD_TV:
                mFHD.setChecked(true);
                break;
            case UHD_TV:
                mUHD.setChecked(true);
                break;
        }
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
            case R.id.tv_laptop_1366:
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, LAPTOP_1366);
                SystemProperties.set(DPI_INFO_IN_CONFIG, String.valueOf(NUM_DPI_LOW));
                break;
            case R.id.tv_laptop_1920:
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, LAPTOP_1920);
                SystemProperties.set(DPI_INFO_IN_CONFIG, String.valueOf(NUM_DPI_MEDIUM));
                break;
            case R.id.tv_laptop_2560:
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, LAPTOP_2560);
                SystemProperties.set(DPI_INFO_IN_CONFIG, String.valueOf(NUM_DPI_HIGH));
                break;
            case R.id.tv_desktop_1920:
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, DESKTOP_1920);
                SystemProperties.set(DPI_INFO_IN_CONFIG, String.valueOf(NUM_DPI_MEDIUM));
                break;
            case R.id.tv_desktop_2560:
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, DESKTOP_2560);
                SystemProperties.set(DPI_INFO_IN_CONFIG, String.valueOf(NUM_DPI_HIGH));
                break;
            case R.id.tv_deskptop_3840:
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, DESKTOP_3840);
                SystemProperties.set(DPI_INFO_IN_CONFIG, String.valueOf(NUM_DPI_HIGH));
                break;
            case R.id.tv_fhd:
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, FHD_TV);
                SystemProperties.set(DPI_INFO_IN_CONFIG, String.valueOf(NUM_DPI_HIGH));
                break;
            case R.id.tv_uhd:
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, UHD_TV);
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
