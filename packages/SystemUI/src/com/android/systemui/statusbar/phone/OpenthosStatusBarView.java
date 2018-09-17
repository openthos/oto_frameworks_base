package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.dialog.BaseDialog;
import com.android.systemui.dialog.BatteryDialog;
import com.android.systemui.dialog.CalendarDialog;
import com.android.systemui.dialog.CalendarDisplayView;
import com.android.systemui.dialog.InputMethodDialog;
import com.android.systemui.dialog.StartupMenuDialog;
import com.android.systemui.dialog.VolumeDialog;
import com.android.systemui.dialog.WifiDialog;

import com.android.systemui.R;

import java.util.List;

public class OpenthosStatusBarView extends PanelBar {
    private static final String SYSTEM_INPUT_METHOD_ID = "com.android.inputmethod.latin/.LatinIME";
    private static final String TAG = "OpenthosStatusBarView";

    private InputMethodManager mInputMethodManager;
    private StatusBar mStatusBar;
    private ImageView mStartupMenu;
    private OpenthosStatusBarView mOpenthosStatusBarView;
    private ImageView mInputView;
    private ImageView mBatteryView;
    private ImageView mWifiView;
    private ImageView mVolumeView;
    private ImageView mNotificationView;
    private CalendarDisplayView mCalendarView;
    private ImageView mHomeView;
    private LinearLayout mLlScrollContents;
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
        mStatusBar = SysUiServiceProvider.getComponent(context, StatusBar.class);
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
        super.onFinishInflate();
        initView();
        initData();
        initListener();
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
        mEmptyStatusBar = (View) findViewById(R.id.empty_statusbar);
        mLlScrollContents = (LinearLayout) findViewById(R.id.ll_scroll_icon_contents);
    }

    private void initData() {
        mInputMethodManager =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        updateInputMethodIcon();
        initDialog();
    }

    private void initDialog() {
        mStartupMenuDialog = new StartupMenuDialog(getContext());
        mInputManagerDialog = new InputMethodDialog(getContext());
        mBatteryDialog = new BatteryDialog(getContext());
        mWifiDialog = new WifiDialog(getContext());
        mVolumeDialog = new VolumeDialog(getContext());
        mCalendarDialog = new CalendarDialog(getContext());
    }

    public StartupMenuDialog getStartupMenuDialog() {
        return (StartupMenuDialog) mStartupMenuDialog;
    }

    private void initListener() {
        mStartupMenu.setOnTouchListener(mTouchListener);
        mInputView.setOnTouchListener(mTouchListener);
        mBatteryView.setOnTouchListener(mTouchListener);
        mWifiView.setOnTouchListener(mTouchListener);
        mVolumeView.setOnTouchListener(mTouchListener);
        mNotificationView.setOnTouchListener(mTouchListener);
        mCalendarView.setOnTouchListener(mTouchListener);
        mHomeView.setOnTouchListener(mTouchListener);
        mEmptyStatusBar.setOnTouchListener(mTouchListener);
//        mLlScrollContents.setOnClickListener(mOpenthosStatusbarListener);

	mInputView.setOnHoverListener(mHoverListener);
	mBatteryView.setOnHoverListener(mHoverListener);
	mWifiView.setOnHoverListener(mHoverListener);
	mVolumeView.setOnHoverListener(mHoverListener);
	mNotificationView.setOnHoverListener(mHoverListener);
	mCalendarView.setOnHoverListener(mHoverListener);
    }

    private View.OnHoverListener mHoverListener = (View v, MotionEvent event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
                v.setBackground(getResources().getDrawable(R.color.common_hover_bg));
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                v.setBackground(getResources().getDrawable(R.drawable.system_bar_background));
                break;
        }
        return false;
    };

    private View.OnTouchListener mTouchListener = (View v, MotionEvent event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
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
                    getContext().startActivity(home);
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
        return false;
    };

    public void updateBattertIcon(int level, boolean pluggedIn, boolean charging) {
        if (charging || pluggedIn || level == 0) {
            mBatteryView.setImageDrawable(getContext().getDrawable(
                    R.mipmap.statusbar_battery));
        } else if (level >= 75) {
            mBatteryView.setImageDrawable(getContext().getDrawable(
                    R.mipmap.statusbar_battery_high));
        } else if (level >= 25 && level <= 75) {
            mBatteryView.setImageDrawable(getContext().getDrawable(
                    R.mipmap.ic_notice_battery_half));
        } else {
            mBatteryView.setImageDrawable(getContext().getDrawable(
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
//                MenuDialog.getInstance(mContext).show(DialogType.SHOW_TASKBAR, mDownX, mDownY);
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

    public void updateInputMethodIcon() {
        List<InputMethodInfo> inputMethodList = mInputMethodManager.getInputMethodList();
        String currentInputMethodId = Settings.Secure.getString(
                getContext().getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        for (InputMethodInfo im : inputMethodList) {
            if (im.getId().equals(currentInputMethodId)) {
                if (currentInputMethodId.equals(SYSTEM_INPUT_METHOD_ID)) {//os input
                    mInputView.setImageResource(R.drawable.statusbar_switch_input_method);
                    return;
                } // other input methods;
                mInputView.setImageDrawable(im.loadIcon(getContext().getPackageManager()));
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
        layoutParams.height = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        layoutParams.width = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        setLayoutParams(layoutParams);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onFinishInflate();
    }
}
