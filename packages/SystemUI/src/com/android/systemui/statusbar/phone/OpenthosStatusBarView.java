package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.LinearLayout;

import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.DejankUtils;
import com.android.systemui.dialog.BaseDialog;
import com.android.systemui.dialog.BatteryDialog;
import com.android.systemui.dialog.CalendarDialog;
import com.android.systemui.dialog.CalendarDisplayView;
import com.android.systemui.dialog.InputMethodDialog;
import com.android.systemui.dialog.StartupMenuDialog;
import com.android.systemui.dialog.VolumeDialog;
import com.android.systemui.dialog.MenuDialog;
import com.android.systemui.dialog.WifiDialog;
import com.android.systemui.startupmenu.DialogType;

import com.android.systemui.R;

/**
 * Created by cao on 17-9-26.
 */

public class OpenthosStatusBarView extends PanelBar {
    private static final String TAG = "OpenthosStatusBarView";

    private StatusBar mStatusBar;
    private Context mContext;
    private ImageView mStartupMenu;
    private OpenthosStatusBarView mOpenthosStatusBarView;
    private ImageView mInputView;
    private ImageView mBatteryView;
    private ImageView mWifiView;
    private ImageView mVolumeView;
    private ImageView mNotificationView;
    private CalendarDisplayView mCalendarView;
    private ImageView mHomeView;
    private HorizontalScrollView mScrollStatusBar;
    private LinearLayout mllScrollContents;
    private View mEmptyStatusBar;
    private BaseDialog mStartupMenuDialog;
    private BaseDialog mInputManagerDialog;
    private BaseDialog mBatteryDialog;
    private BaseDialog mWifiDialog;
    private BaseDialog mVolumeDialog;
    private BaseDialog mCalendarDialog;
    private BaseDialog mCurrentDialog;

    private int mDownX = 0;
    private int mDownY = 0;

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
        mScrollStatusBar = (HorizontalScrollView) findViewById(R.id.sroll_status_bar);
        mEmptyStatusBar = (View) findViewById(R.id.empty_statusbar);
        mllScrollContents = (LinearLayout) findViewById(R.id.ll_scroll_icon_contents);
        initDialog();
        mStartupMenuDialog = StartupMenuDialog.getInstance(getContext());
        openthosStatusBarClickListener();
    }

    private void initDialog() {
        mInputManagerDialog = new InputMethodDialog(getContext());
        mBatteryDialog = new BatteryDialog(getContext());
        mWifiDialog = new WifiDialog(getContext());
        mVolumeDialog = new VolumeDialog(getContext());
        mCalendarDialog = new CalendarDialog(getContext());
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
        mEmptyStatusBar.setOnClickListener(mOpenthosStatusbarListener);
        mScrollStatusBar.setOnClickListener(mOpenthosStatusbarListener);
        mllScrollContents.setOnClickListener(mOpenthosStatusbarListener);
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
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.addCategory(Intent.CATEGORY_HOME);
                    mContext.startActivity(home);
                    break;
                case R.id.sroll_status_bar:
                    //Handle events
                    break;
                case R.id.ll_scroll_icon_contents:
                    //Handle events
                    break;
                case R.id.empty_statusbar:
                    //click empty status bar ,then dismiss all dialog and hide notification panel.
                    if (mCurrentDialog != null && mCurrentDialog.isShowing()) {
                        mCurrentDialog.dismiss();
                    }
                    setCustomStatusBar();
                    mStatusBar.hideCustomNotificationPanel();
                    break;
            }
        }
    };

    public void updateBattertIcon(int level, boolean pluggedIn, boolean charging) {
        if (charging || pluggedIn || level == 0) {
            mBatteryView.setImageDrawable(mContext.getDrawable(
                    R.mipmap.statusbar_battery));
        } else if (level >= 75) {
            mBatteryView.setImageDrawable(mContext.getDrawable(
                    R.mipmap.statusbar_battery_high));
        } else if (level >= 25 && level <= 75) {
            mBatteryView.setImageDrawable(mContext.getDrawable(
                    R.mipmap.ic_notice_battery_half));
        } else {
            mBatteryView.setImageDrawable(mContext.getDrawable(
                    R.mipmap.statusbar_battery_low));
        }
    }

    //show / hide status bar.
    private void setCustomStatusBar() {
        mEmptyStatusBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDownX = (int) event.getRawX();
                    mDownY = (int) event.getRawY();
                }
                return false;
            }
        });
        mEmptyStatusBar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mStatusBar.mShowTaskbarDialog.show(DialogType.SHOW_TASKBAR, mDownX, mDownY);
                return false;
            }
        });
    }

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

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mStartupMenuDialog = StartupMenuDialog.reCreateStartupMenudialog(getContext());
        initDialog();
    }
}
