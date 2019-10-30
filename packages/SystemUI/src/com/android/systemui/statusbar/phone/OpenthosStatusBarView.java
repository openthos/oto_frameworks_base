package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.RemoteException;
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
import com.android.systemui.dialog.StartupMenuDialog;

import com.android.systemui.R;

import java.util.List;

public class OpenthosStatusBarView extends PanelBar {
    private static final String TAG = "OpenthosStatusBarView";

    private StatusBar mStatusBar;
    private ImageView mStartupMenu;
    private OpenthosStatusBarView mOpenthosStatusBarView;
    private LinearLayout mLlScrollContents;
    private BaseDialog mStartupMenuDialog;
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
        mLlScrollContents = (LinearLayout) findViewById(R.id.ll_scroll_icon_contents);
    }

    private void initData() {
        initDialog();
    }

    private void initDialog() {
        mStartupMenuDialog = new StartupMenuDialog(getContext());
    }

    public StartupMenuDialog getStartupMenuDialog() {
        return (StartupMenuDialog) mStartupMenuDialog;
    }

    private void initListener() {
        mStartupMenu.setOnTouchListener(mTouchListener);
    }

    private View.OnTouchListener mTouchListener = (View v, MotionEvent event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (v.getId()) {
                case R.id.iv_startupmenu_status_bar:
                    showDialog(mStartupMenu, mStartupMenuDialog);
                    break;
                case R.id.ll_scroll_icon_contents:
                    //Handle events
                    break;
            }
        }
        return false;
    };

    private void showDialog(View view, BaseDialog dialog) {
        if (view == null) {
            return;
        }
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
