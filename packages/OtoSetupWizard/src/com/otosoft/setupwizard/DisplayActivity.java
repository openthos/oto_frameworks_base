package com.otosoft.setupwizard;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.provider.Settings;

public class DisplayActivity extends BaseActivity implements View.OnClickListener{

    private static final String SYSTEM_DPI = "system_dpi";
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
        findViewById(R.id.tv_next).setOnClickListener(this);
        findViewById(R.id.tv_prev).setOnClickListener(this);
        mLDpi.setOnClickListener(this);
        mMDpi.setOnClickListener(this);
        mHDpi.setOnClickListener(this);

        mSelectColor = getResources().getColor(R.color.selected_bg);
        mNoSelectColor = getResources().getColor(R.color.no_secleted_bg);
        int dpi = Settings.System.getInt(getContentResolver(), SYSTEM_DPI, 160);
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
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, 120);
                mLDpi.setBackgroundColor(mSelectColor);
                mMDpi.setBackgroundColor(mNoSelectColor);
                mHDpi.setBackgroundColor(mNoSelectColor);
                break;
            case R.id.tv_dpi_medium:
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, 160);
                mLDpi.setBackgroundColor(mNoSelectColor);
                mMDpi.setBackgroundColor(mSelectColor);
                mHDpi.setBackgroundColor(mNoSelectColor);
                break;
            case R.id.tv_dpi_high:
                Settings.System.putInt(getContentResolver(), SYSTEM_DPI, 240);
                mLDpi.setBackgroundColor(mNoSelectColor);
                mMDpi.setBackgroundColor(mNoSelectColor);
                mHDpi.setBackgroundColor(mSelectColor);
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
