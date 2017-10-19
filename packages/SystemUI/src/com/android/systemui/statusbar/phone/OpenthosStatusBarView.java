package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View;
import android.util.Log;
import android.util.AttributeSet;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;

import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.DejankUtils;
import com.android.systemui.dialog.BaseDialog;
import com.android.systemui.dialog.BatteryDialog;
import com.android.systemui.dialog.CalendarDialog;
import com.android.systemui.dialog.CalendarDisplayView;
import com.android.systemui.dialog.InputMethodDialog;
import com.android.systemui.dialog.StartupMenuDialog;
import com.android.systemui.dialog.VolumeDialog;
import com.android.systemui.dialog.WifiDialog;

import com.android.systemui.R;

/**
 * Created by cao on 17-9-26.
 */

public class OpenthosStatusBarView extends PanelBar{
    private static final String TAG = "OpenthosStatusBarView";

    private StatusBar mStatusBar;
    private Context mContext;

    //==================================================== Openthos Status bar
    private ImageView mStartupMenu;
    private OpenthosStatusBarView mOpenthosStatusBarView;
    private ImageView mInputView;
    private ImageView mBatteryView;
    private ImageView mWifiView;
    private ImageView mVolumeView;
    private ImageView mNotificationView;
    private CalendarDisplayView mCalendarView;
    private ImageView mHomeView;
    private BaseDialog mStartupMenuDialog;
    private BaseDialog mInputManagerDialog;
    private BaseDialog mBatteryDialog;
    private BaseDialog mWifiDialog;
    private BaseDialog mVolumeDialog;
    private BaseDialog mCalendarDialog;
    private BaseDialog mCurrentDialog;

    public OpenthosStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStatusBar = SysUiServiceProvider.getComponent(mContext, StatusBar.class);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
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
    protected void onFinishInflate() {
        initView();
    }

    private void initView() {
        mStartupMenu = (ImageView) findViewById(R.id.iv_startupmenu_status_bar);
        mInputView = (ImageView) findViewById(R.id.iv_input_status_bar);
        mBatteryView = (ImageView) findViewById(R.id.iv_battery_status_bar);
        mWifiView = (ImageView) findViewById(R.id.iv_wifi_status_bar);
        mVolumeView = (ImageView) findViewById(R.id.iv_volume_status_bar);
        mNotificationView = (ImageView) findViewById(R.id.iv_notification_status_bar);
        mCalendarView = (CalendarDisplayView) findViewById(R.id.iv_date_status_bar);
        mHomeView = (ImageView) findViewById(R.id.iv_home_status_bar);
        //mSrollStatusBar = (HorizontalScrollView) findViewById(R.id.sroll_status_bar);

        //init dialog.
        mStartupMenuDialog = StartupMenuDialog.getInstance(getContext());
        mInputManagerDialog = new InputMethodDialog(getContext());
        mBatteryDialog = new BatteryDialog(getContext());
        mWifiDialog = new WifiDialog(getContext());
        mVolumeDialog = new VolumeDialog(getContext());
        mCalendarDialog = new CalendarDialog(getContext());

        openthosStatusBarClickListener();
    }

    private void openthosStatusBarClickListener() {
        mStartupMenu.setOnClickListener(mOpenthosStatusbarListener);
        mInputView.setOnClickListener(mOpenthosStatusbarListener);
        mBatteryView.setOnClickListener(mOpenthosStatusbarListener);
        mWifiView.setOnClickListener(mOpenthosStatusbarListener);
        mVolumeView.setOnClickListener(mOpenthosStatusbarListener);
        mNotificationView.setOnClickListener(mOpenthosStatusbarListener);
        mCalendarView.setOnClickListener(mOpenthosStatusbarListener);
        mHomeView.setOnClickListener(mOpenthosStatusbarListener);
    }

    private View.OnClickListener mOpenthosStatusbarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
               case R.id.iv_startupmenu_status_bar:
                   showDialog(mStartupMenu, mStartupMenuDialog);
                   break;
               case R.id.iv_input_status_bar:
                   showDialog(mInputView, mInputManagerDialog);
                   break;
               case R.id.iv_battery_status_bar:
                   showDialog(mBatteryView, mBatteryDialog);
                   break;
               case R.id.iv_wifi_status_bar:
                   showDialog(mWifiView, mWifiDialog);
                   break;
               case R.id.iv_volume_status_bar:
                   showDialog(mVolumeView, mVolumeDialog);
                   break;
               case R.id.iv_notification_status_bar:
                   if (mCurrentDialog != null && mCurrentDialog.isShowing()) {
                       mCurrentDialog.dismiss();
                   }
                   mStatusBar.showCustomNotificationPanel();
                   break;
               case R.id.iv_date_status_bar:
                   showDialog(mCalendarView, mCalendarDialog);
                   break;
               case R.id.iv_home_status_bar:
                   Toast.makeText(mContext, "home", Toast.LENGTH_SHORT).show();
                   mStatusBar.awakenDreams();
                   break;
            }
        }
    };

    private void showDialog(View view, BaseDialog dialog) {
        if (view == null) {
            return;
        }
        mStatusBar.hideCustomNotificationPanel();
        if (mCurrentDialog == null) {
            dialog.show(view);
            mCurrentDialog = dialog;
        } else {
            if (mCurrentDialog == dialog) {
                if (mCurrentDialog.isShowing()) {
                    dialog.dismiss();
                } else {
                    dialog.show(view);
                }
            } else {
                if (mCurrentDialog.isShowing()) {
                    mCurrentDialog.dismiss();
                }
                dialog.show(view);
                mCurrentDialog = dialog;
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onTrackingStarted() {
        super.onTrackingStarted();
    }

    @Override
    public void panelExpansionChanged(float frac, boolean expanded) {
        super.panelExpansionChanged(frac, expanded);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public void panelScrimMinFractionChanged(float minFraction) {

    }

    public void onDensityOrFontScaleChanged() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = getResources().getDimensionPixelSize(
                R.dimen.status_bar_height);
        layoutParams.width = getResources().getDimensionPixelSize(
                R.dimen.status_bar_height);
        setLayoutParams(layoutParams);
    }
}
