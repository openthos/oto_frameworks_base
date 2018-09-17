package com.android.systemui.statusbar.view;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.IActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.systemui.R;
import com.android.systemui.dialog.DialogType;
import com.android.systemui.dialog.MenuDialog;
import com.android.systemui.startupmenu.SqliteOpenHelper;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.OnMenuClick;
import com.android.systemui.startupmenu.utils.AppOperateManager;

public class TaskBarIcon extends FrameLayout implements View.OnTouchListener,
        View.OnLongClickListener, OnMenuClick, View.OnHoverListener {

    private static MenuDialog mMenuDialog;
    private MenuDialog mHoverDialog;
    private AppOperateManager mOperateManager;
    private SqliteOpenHelper mOpenHelper;
    private ImageView mIconView;
    private View mFocuseView;
    private View mRunView;

    private String mPackageName;
    private boolean mIsFocusInApplications;
    private boolean mIsRun;
    private int mTaskId;

    public TaskBarIcon(Context context, String packageName) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.taskbar_button_layout, this);
        mPackageName = packageName;
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mIconView = (ImageView) findViewById(R.id.icon_view);
        mFocuseView = findViewById(R.id.focuse_view);
        mRunView = findViewById(R.id.run_view);
    }

    private void initData() {
        mOperateManager = AppOperateManager.getInstance(getContext());
        mOpenHelper = SqliteOpenHelper.getInstance(getContext());
        mIconView.setImageDrawable(getAppInfo().getIcon());
        initDialog();
    }

    private void initListener() {
        setOnLongClickListener(this);
        setOnTouchListener(this);
        setOnHoverListener(this);
    }

    private void initDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new MenuDialog(getContext());
            mMenuDialog.setOnMenuClick(this);
        }
        mHoverDialog = new MenuDialog(getContext());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startRun();
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        showDialog();
        return true;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
                if (!mMenuDialog.isShowing()) {
                    mHoverDialog.show(DialogType.NOTIFY_NAME, getAppInfo(), this);
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                mHoverDialog.dismiss();
                break;
        }
        return false;
    }

    @Override
    public void menuClick(View view, Dialog dialog, AppInfo appInfo, String menu) {
        if (menu.equals(getContext().getString(R.string.open))) {
            mOperateManager.openApplication(appInfo.getComponentName());
        } else if (menu.equals(getContext().getString(R.string.phone_mode))) {
            mOperateManager.runPhoneMode(appInfo.getComponentName());
        } else if (menu.equals(getContext().getString(R.string.desktop_mode))) {
            mOperateManager.runDesktopMode(appInfo.getComponentName());
        } else if (menu.equals(getContext().getString(R.string.lock_to_task_bar))) {
            mOperateManager.addToTaskbar(appInfo.getPackageName());
        } else if (menu.equals(getContext().getString(R.string.unlock_from_task_bar))) {
            mOperateManager.removeFromTaskbar(appInfo.getPackageName());
        } else if (menu.equals(getContext().getString(R.string.close))) {
            mOperateManager.closeApp(appInfo.getPackageName());
        }
        dialog.dismiss();
    }

    @Override
    public void sortShow(View view, Dialog dialog, String menu) {
        dialog.dismiss();
    }

    private void showDialog() {
        if (!isRun()) {
            mMenuDialog.show(DialogType.BAR_LOCK_CLOSE, getAppInfo(), this);
        } else if (isLocked()) {
            mMenuDialog.show(DialogType.BAR_LOCK_OPEN, getAppInfo(), this);
        } else {
            mMenuDialog.show(DialogType.BAR_UNLOCK_OPEN, getAppInfo(), this);
        }
    }

    public void locked() {
        setLocked(true);
        mOpenHelper.updateDataLocked(getAppInfo());
    }

    public void unlocked() {
        setLocked(false);
        mOpenHelper.updateDataLocked(getAppInfo());
    }

    private void startRun() {
        try {
            IActivityManager am = ActivityManager.getService();
            if (isRun()) {
                am.setFocusedTask(mTaskId);
            } else {
                mOperateManager.openApplication(getAppInfo().getComponentName());
            }
            setFocusInApplications(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        setRun(false);
    }

    public AppInfo getAppInfo() {
        return mOperateManager.getAppInfo(mPackageName);
    }

    public String getPackageName() {
        return mPackageName;
    }

    public boolean isLocked() {
        return getAppInfo().isLocked();
    }

    public void setLocked(boolean isLocked) {
        getAppInfo().setLocked(isLocked);
    }

    public boolean isFocusInApplications() {
        return mIsFocusInApplications;
    }

    public void setFocusInApplications(boolean isFocusInApplications) {
        mIsFocusInApplications = isFocusInApplications;
        mIsRun = true;
        if (mIsFocusInApplications) {
            mFocuseView.setVisibility(VISIBLE);
            mRunView.setVisibility(GONE);
        } else {
            mFocuseView.setVisibility(GONE);
            mRunView.setVisibility(VISIBLE);
        }
    }

    public boolean isRun() {
        return mIsRun;
    }

    public void setRun(boolean isRun) {
        mIsRun = isRun;
        if (mIsRun) {
            mFocuseView.setVisibility(GONE);
            mRunView.setVisibility(VISIBLE);
        } else {
            mTaskId = -1;
            mFocuseView.setVisibility(GONE);
            mRunView.setVisibility(GONE);
        }
    }

    public int getTaskId() {
        return mTaskId;
    }

    public void setTaskId(int taskId) {
        mTaskId = taskId;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initView();
        initData();
        initListener();
    }
}
