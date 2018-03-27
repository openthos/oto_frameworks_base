package com.otosoft.setupwizard;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.res.Resources;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.provider.Settings;
import com.android.internal.app.LocalePicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class SetupWizardActivity extends BaseActivity {
    private TextView mNext;
    private static Locale mCurrentLocale = null;
    private LinearLayout mLanguageContainer;
    private static final int CHOOSE_CHINA_ITEM = 1;
    private static final int CHOOSE_ENGLISH_ITEM = 2;
    private static final int DEFAULT_SLEEP_TIME = 1800000; // 30 min -- 1800 seconds
    private static final String HOURS_24 = "24";
    private static final String CHINA_TIME_ZONE = "GMT+8:00";
    private int noSelectedBg;
    private int selectedBg;
    private final Runnable mRequestFocus = new Runnable() {
        public void run() {
            if (SetupWizardActivity.this.mNext != null) {
                SetupWizardActivity.this.mNext.requestFocusFromTouch();
            }
        }
    };
    private TextView mChinese;
    private TextView mEnglish;
    private int chooseItem = 0;
    private static final String PROPERTY_NATIVEBRIDGE = "persist.sys.nativebridge";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //make the app compatability available
        SystemProperties.set(PROPERTY_NATIVEBRIDGE,"1");
        setContentView(R.layout.activity_setupwizard);
        //send broadcast to control status bar
        Intent intent1 = new Intent();
        intent1.setAction(Intent.STATUS_BAR_HIDE_BOOT_EXIT);
        SetupWizardActivity.this.sendBroadcast(intent1);
        //set time 24 hour
        Settings.System.putString(getContentResolver(), Settings.System.TIME_12_24, HOURS_24);
        Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_OFF_TIMEOUT, DEFAULT_SLEEP_TIME);
        this.mChinese = (TextView) findViewById(R.id.tv_chinese);
        this.mEnglish = (TextView) findViewById(R.id.tv_english);
        Resources res = getBaseContext().getResources();
        noSelectedBg = res.getColor(R.color.no_secleted_bg);
        selectedBg = res.getColor(R.color.selected_bg);
        mChinese.setBackgroundColor(selectedBg);
        this.mChinese.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mChinese.setBackgroundColor(selectedBg);
                mEnglish.setBackgroundColor(noSelectedBg);
                chooseItem = CHOOSE_CHINA_ITEM;
            }
        });

        this.mEnglish.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mChinese.setBackgroundColor(noSelectedBg);
                mEnglish.setBackgroundColor(selectedBg);
                chooseItem = CHOOSE_ENGLISH_ITEM;
            }
        });
        mNext = (TextView) findViewById(R.id.tv_next);
        mNext.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                switch(chooseItem){
                    case CHOOSE_CHINA_ITEM:
                        mCurrentLocale = Locale.CHINA;
                        updateLocale(mCurrentLocale);
                        break;
                    case CHOOSE_ENGLISH_ITEM:
                        mCurrentLocale = Locale.US;
                        updateLocale(mCurrentLocale);
                        break;
                    default:
                        mCurrentLocale = Locale.CHINA;
                        updateLocale(mCurrentLocale);
                        break;
                }
                SetupWizardActivity.this.startActivity(new Intent("com.android.wizard.DISPLAY"));
            }
        });
        new PreInstallThread("/sdcard/Desktop", "UserGuide.html").start();
        new PreInstallThread("/sdcard/Movies", "big_buck_bunny_480p_surround-fix.mp4").start();
    }

    private class PreInstallThread extends Thread {
        private String mFilePath;
        private String mFileName;
        private static final int BUFFER_SIZE_64KB = 64 * 1024;

        public PreInstallThread (String filePath, String fileName) {
            File f = new File(filePath);
            if (!f.exists()) {
                f.mkdirs();
            }
            mFilePath = filePath;
            mFileName = fileName;
        }

        @Override
        public void run() {
            try {
                InputStream in = getAssets().open(mFileName);
                OutputStream out = new FileOutputStream(mFilePath + "/" +mFileName);
                int bytecount;
                byte[] bytes = new byte[BUFFER_SIZE_64KB];
                while ((bytecount = in.read(bytes)) != -1) {
                    out.write(bytes, 0, bytecount);
                }
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onResume() {
        super.onResume();
        this.mRequestFocus.run();
        new Handler().postDelayed(this.mRequestFocus, 500);
    }

    @Override
    public void onBackPressed() {
    }

    private void updateLocale(Locale locale) {
        LocalePicker.updateLocale(locale);
    }
}
