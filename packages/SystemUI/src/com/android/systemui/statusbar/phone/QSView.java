package com.android.systemui.statusbar.phone;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.hardware.input.InputManager;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.util.AttributeSet;
import android.util.Log;
import com.android.systemui.startupmenu.U;

import com.android.systemui.R;

/**
 * Created by matthew on 17-10-20.
 */

public class QSView extends LinearLayout {

    private final static String TAG = "QSView";
    private Context mContext;
    private ImageView mSettings;
    private ImageView mScreenShot;
    private ImageView mProjection;
    private ImageView mIsolation;
    private int mNumClick = 0;

    public QSView(Context context) {
        super(context);
        mContext = context;
    }

    public QSView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public QSView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onFinishInflate() {
        mSettings = (ImageView) findViewById(R.id.qs_settings);
        mScreenShot = (ImageView) findViewById(R.id.qs_shot_screen);
        mScreenShot = (ImageView) findViewById(R.id.qs_isolation);
        mProjection = (ImageView) findViewById(R.id.qs_projection);
        mIsolation = (ImageView) findViewById(R.id.qs_isolation);
        clickQsPanel();
    }

    private void clickQsPanel() {
        mSettings.setOnClickListener(mQSViewListener);
        mScreenShot.setOnClickListener(mQSViewListener);
        mProjection.setOnClickListener(mQSViewListener);
        mIsolation.setOnClickListener(mQSViewListener);
    }

    private View.OnClickListener mQSViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.qs_shot_screen:
                   //((InputManager)mContext.getSystemService(Context.INPUT_SERVICE))
                   //    .sendKeyEvent(KeyEvent.KEYCODE_SYSRQ);
                    break;
                case R.id.qs_isolation:
		    if ((mNumClick ++) % 2 == 0) {
                        stopAllConnection(false);
                        mIsolation.setImageDrawable(getResources().getDrawable(R.mipmap.ic_notification_isolation_off));
		    } else {
			stopAllConnection(true);
                        mIsolation.setImageDrawable(getResources().getDrawable(R.mipmap.ic_notification_isolation_on));
		    }
                    break;
                case R.id.qs_settings:
                    U.launchApp(getContext(),
                        new ComponentName("com.android.settings", "com.android.settings.Settings"));
                    break;
                case R.id.qs_projection:
                    break;
            }
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void stopAllConnection(boolean state) {
        final ConnectivityManager mgr = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final EthernetManager ethManager = (EthernetManager)mContext
                .getSystemService(Context.ETHERNET_SERVICE);
        mgr.setAirplaneMode(state);
        //ethManager.stop();
        //ethManager.start();
    }
}
