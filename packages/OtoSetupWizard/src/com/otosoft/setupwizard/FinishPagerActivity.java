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

public class FinishPagerActivity extends BaseActivity {
    private Button mButtonStart;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_pager);

        this.mButtonStart = (Button) findViewById(R.id.button_start_use);
        mButtonStart.getBackground().setAlpha(25);
        this.mButtonStart.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ((SetupWizardApplication) FinishPagerActivity.this.getApplication())
                        .onSetupFinished(FinishPagerActivity.this);
                //send broadcast to control status bar finish
                Intent intent = new Intent();
                intent.setAction(Intent.STATUS_BAR_SHOW_FINISH_ACTIVITY);
                FinishPagerActivity.this.sendBroadcast(intent);
            }
        });
    }

    public void onResume() {
        super.onResume();
    }
}
