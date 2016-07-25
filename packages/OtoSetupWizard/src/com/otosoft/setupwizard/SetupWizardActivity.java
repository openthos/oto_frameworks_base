package com.otosoft.setupwizard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.app.LocalePicker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class SetupWizardActivity extends BaseActivity {
    private Button mButtonNext;
    private Locale mCurrentLocale;
    private LinearLayout mLanguageContainer;
    private ArrayList<Locale> mLocales = new ArrayList();
    private final Runnable mRequestFocus = new Runnable() {
        public void run() {
            if (SetupWizardActivity.this.mButtonNext != null) {
                SetupWizardActivity.this.mButtonNext.requestFocusFromTouch();
            }
        }
    };
    private TextView mTextViewLanguage;
    private static final String PROPERTY_NATIVEBRIDGE = "persist.sys.nativebridge";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLocales.add(Locale.CHINA);
        this.mLocales.add(Locale.US);
        this.mCurrentLocale = Locale.getDefault();
        //make the app compatability available
        SystemProperties.set(PROPERTY_NATIVEBRIDGE,"1");
        if (!this.mLocales.contains(this.mCurrentLocale)) {
            this.mCurrentLocale = Locale.CHINA;
            updateLocale(this.mCurrentLocale);
        }
        setContentView(R.layout.activity_setupwizard);
        findViewById(R.id.relativelayout_root).setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 0 && SetupWizardActivity.this.mLanguageContainer != null) {
                    SetupWizardActivity.this.mLanguageContainer.setVisibility(8);
                }
                return false;
            }
        });
        //send broadcast to control status bar
        Intent intent1 = new Intent();
        intent1.setAction("com.android.control.statusbar.start");
        SetupWizardActivity.this.sendBroadcast(intent1);
        this.mTextViewLanguage = (TextView) findViewById(R.id.textview_language);
        this.mLanguageContainer = (LinearLayout) findViewById(R.id.linearlayout_language);
        this.mTextViewLanguage.setText(this.mCurrentLocale.getDisplayName(this.mCurrentLocale));
        this.mTextViewLanguage.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SetupWizardActivity.this.toggleContainerVisibility();
            }
        });
        this.mButtonNext = (Button) findViewById(R.id.button_next);
        this.mButtonNext.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (SetupWizardActivity.this.mLanguageContainer != null) {
                    SetupWizardActivity.this.mLanguageContainer.setVisibility(8);
                }
                SetupWizardActivity.this.startActivity(SetupWizardActivity.this.buildWifiSetupIntent());
            }
        });
    }

    public void onResume() {
        super.onResume();
        this.mRequestFocus.run();
        new Handler().postDelayed(this.mRequestFocus, 500);
    }

    protected void onNewIntent(Intent intent) {
        if (intent != null && (intent.getFlags() & 67108864) != 0 && intent.getBooleanExtra("extra_clear_top", false)) {
            ((SetupWizardApplication) getApplication()).onSetupFinishedReally(this);
        }
    }

    private Intent buildWifiSetupIntent() {
        //Intent intent = new Intent("com.otosoft.setupwizard.SETUP_WIFI_NETWORK");
        Intent intent = new Intent("com.android.net.wifi.SETUP_WIFI_NETWORK");
        intent.putExtra("firstRun", true);
        intent.putExtra("allowSkip", true);
        intent.putExtra("useImmersiveMode", true);
        intent.putExtra("theme", "material_light");
        intent.putExtra("wifi_auto_finish_on_connect", true);
        intent.putExtra("scriptUri", "NotUsedNow");
        return intent;
    }

    private void updateLocale(Locale locale) {
        LocalePicker.updateLocale(locale);
    }

    private void toggleContainerVisibility() {
        int i = 0;
        if (this.mLanguageContainer.getChildCount() == 0) {
            Iterator i$ = this.mLocales.iterator();
            while (i$.hasNext()) {
                Locale locale = (Locale) i$.next();
                TextView item = (TextView) LayoutInflater.from(this).inflate(R.layout.language_spinner_item, this.mLanguageContainer, false);
                item.setText(locale.getDisplayName(locale));
                item.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        SetupWizardActivity.this.mLanguageContainer.setVisibility(8);
                        SetupWizardActivity.this.mCurrentLocale = (Locale) SetupWizardActivity.this.mLocales.get(SetupWizardActivity.this.mLanguageContainer.indexOfChild(v));
                        new Handler().post(new Runnable() {
                            public void run() {
                                SetupWizardActivity.this.updateLocale(SetupWizardActivity.this.mCurrentLocale);
                                SetupWizardActivity.this.mTextViewLanguage.setText(SetupWizardActivity.this.mCurrentLocale.getDisplayName(SetupWizardActivity.this.mCurrentLocale));
                            }
                        });
                    }
                });
                this.mLanguageContainer.addView(item);
            }
        }
        for (int i2 = 0; i2 < this.mLanguageContainer.getChildCount(); i2++) {
            boolean z;
            View child = this.mLanguageContainer.getChildAt(i2);
            if (i2 == this.mLocales.indexOf(this.mCurrentLocale)) {
                z = true;
            } else {
                z = false;
            }
            child.setSelected(z);
        }
        LinearLayout linearLayout = this.mLanguageContainer;
        if (this.mLanguageContainer.getVisibility() == 0) {
            i = 8;
        }
        linearLayout.setVisibility(i);
    }
}
