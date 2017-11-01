package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.systemui.R;

/**
 * Created by matthew on 17-10-31.
 */

public class OpenthosNotificationView extends LinearLayout {

    public OpenthosNotificationView(Context context) {
        super(context);
    }

    public OpenthosNotificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OpenthosNotificationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void initView() {
        TextView notificationInfo = (TextView)findViewById(R.id.notification_info);
        Button notificationManager = (Button)findViewById(R.id.notification_manager);
        Button clearAll = (Button)findViewById(R.id.notification_clear_all);
        TextView printMessage = (TextView)findViewById(R.id.print_message);
        Button printManager = (Button)findViewById(R.id.printManager);

        notificationInfo.setText(getResources().getString(R.string.notification_info));
        notificationManager.setText(getResources().getString(R.string.notification_manager));
        clearAll.setText(getResources().getString(R.string.clear_all));
        printMessage.setText(getResources().getString(R.string.print_message));
        printManager.setText(getResources().getString(R.string.print_manager));
    }
}
