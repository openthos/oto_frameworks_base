package com.android.systemui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class StartupMenuManager {
    private static StartupMenuManager manager;
    private static StartupMenuView mStartupMenuView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private Context mContext;
    private boolean mIsShowing;

    public static StartupMenuManager getInstance(Context context) {
        if (manager == null) {
            manager = new StartupMenuManager(context);
        }
        return manager;
    }

    private StartupMenuManager(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mContext = context;
        mIsShowing = false;
        initStartupMenuView();
    }

    public void showStartupMenu() {
        if (isShowing()) {
            hideStartupMenu();
            return;
        }
        initStartupMenuView();
        if (mParams == null) {
            mParams = new WindowManager.LayoutParams();
            mParams.width = mStartupMenuView.getMeasuredWidth();
            mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            mParams.gravity = Gravity.TOP | Gravity.LEFT;
            mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mParams.flags =
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            mParams.format = PixelFormat.TRANSPARENT;
        }
        mStartupMenuView.refresh();
        mWindowManager.addView(mStartupMenuView, mParams);
        mIsShowing = true;
    }

    public void hideStartupMenu() {
        if (mIsShowing) {
            mWindowManager.removeView(mStartupMenuView);
            mIsShowing = false;
        }
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    private void initStartupMenuView() {
        if (mStartupMenuView == null) {
            mStartupMenuView = new StartupMenuView(mContext);
            mStartupMenuView.measure(FrameLayout.MeasureSpec.UNSPECIFIED,
                    FrameLayout.MeasureSpec.UNSPECIFIED);
        }
    }

    public void reCreateView() {
        mStartupMenuView = null;
        mParams = null;
        initStartupMenuView();
    }
}
