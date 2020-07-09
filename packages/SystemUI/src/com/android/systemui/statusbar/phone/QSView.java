package com.android.systemui.statusbar.phone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.util.TypedValue;
import android.view.View;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.systemui.R;

/**
 * Created by matthew on 17-10-20.
 */

public class QSView extends LinearLayout {

    private final static String TAG = "QSView";
    private Context mContext;
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
        final EthernetManager ethManager = (EthernetManager) mContext
                .getSystemService(Context.ETHERNET_SERVICE);
        mgr.setAirplaneMode(state);
        //ethManager.stop();
        //ethManager.start();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //updateResource();
    }

    private void updateResource() {
        TextView settings = (TextView) findViewById(R.id.settings);

        settings.setText(getResources().getString(R.string.notification_settings));

        settings.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelSize(R.dimen.qs_font_size_sixteen));
    }
}
