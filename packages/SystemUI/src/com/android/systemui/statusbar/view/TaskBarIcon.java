package com.android.systemui.statusbar.view;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.IActivityManager;
import android.content.Context;
import android.content.ComponentName;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.systemui.R;
import com.android.systemui.dialog.DialogType;
import com.android.systemui.dialog.MenuDialog;
import com.android.systemui.startupmenu.SqliteOpenHelper;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.OnMenuClick;
import com.android.systemui.startupmenu.utils.AppOperateManager;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

public class TaskBarIcon extends FrameLayout implements View.OnTouchListener, Serializable,
        View.OnLongClickListener, View.OnClickListener, OnMenuClick, View.OnHoverListener {

    private static MenuDialog mMenuDialog;
    private static MenuDialog mHoverDialog;
    private static MenuDialog mTaskDialog;
    private AppOperateManager mOperateManager;
    private SqliteOpenHelper mOpenHelper;
    private AppInfo mAppInfo;
    private RelativeLayout mIconBackground;
    private ImageView mIconView;

    private HashSet<Integer> mTasks = new HashSet<>();

    private String mPackageName;
    private ComponentName mComponentName;
    private boolean mIsFocusInApplications;
    private boolean mIsRun;
    private int mTaskId;
    private Rect mTmpRect = new Rect();

    public TaskBarIcon(Context context, ComponentName componentName, AppInfo appInfo) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.taskbar_button_layout, this);
        mComponentName = componentName;
        mPackageName = componentName.getPackageName();
        mAppInfo = appInfo;
        initView();
        initData();
        initListener();
    }
    public TaskBarIcon(Context context, ComponentName componentName) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.taskbar_button_layout, this);
        mComponentName = componentName;
        mPackageName = componentName.getPackageName();
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mIconBackground = (RelativeLayout) findViewById(R.id.taskbar_button_layout);
        mIconView = (ImageView) findViewById(R.id.icon_view);
        setFocusable(false);
        setDefaultFocusHighlightEnabled(false);
    }

    private void initData() {
        try {
            mOperateManager = AppOperateManager.getInstance(getContext());
            mOpenHelper = SqliteOpenHelper.getInstance(getContext());
            mIconView.setImageDrawable(getContext().getPackageManager().
                    getApplicationIcon(getAppInfo().getPackageName()));
            initDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initListener() {
        setOnLongClickListener(this);
        setOnTouchListener(this);
        setOnClickListener(this);
        setOnHoverListener(this);
        mIconBackground.setOnHoverListener(this);
    }

    private void initDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new MenuDialog(getContext());
        }
        if (mHoverDialog == null) {
            mHoverDialog = new MenuDialog(getContext());
        }
        if (mTaskDialog == null) {
            mTaskDialog = new MenuDialog(getContext());
        }
        mMenuDialog.setOnMenuClick(this);
        mTaskDialog.setOnMenuClick(this);
    }

    @Override
    public void onClick(View v) {
        if (!mMenuDialog.isShowing())
            startRun();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
            && event.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
            //startRun();
            showDialog();
        }
        mHoverDialog.dismiss();
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        showDialog();
        return false;
    }

    public static void dismissHoverDialog() {
        if (mHoverDialog.isShowing())
            mHoverDialog.dismiss();
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
                v.setBackground(getResources().getDrawable(R.color.openthos_view_hover_move_color));
                if (!mMenuDialog.isShowing()) {
                    mHoverDialog.show(DialogType.NOTIFY_NAME, getAppInfo(), this);
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                v.setBackground(null);
                ((View)getParent()).getBoundsOnScreen(mTmpRect);
                if (!mTmpRect.contains((int) event.getRawX(), (int) event.getRawY()))
                    mHoverDialog.dismiss();
                break;
        }
        return false;
    }

    @Override
    public void menuClick(View view, Dialog dialog, AppInfo appInfo, String menu, int taskId) {
        if (menu.equals(getContext().getString(R.string.open))) {
            mOperateManager.openApplication(appInfo);
        } else if (menu.equals(getContext().getString(R.string.lock_to_task_bar))) {
            mOperateManager.addToTaskbar(taskId, appInfo.getComponentName());
        } else if (menu.equals(getContext().getString(R.string.unlock_from_task_bar))) {
            mOperateManager.removeFromTaskbar(appInfo.getComponentName());
        } else if (menu.equals(getContext().getString(R.string.close))) {
            Integer[] it = mTasks.toArray(new Integer[0]);
            for (int i : it) {
                mOperateManager.closeApp(i, appInfo.getPackageName());
            }
        }
        if (taskId == -2) {
            try {
                IActivityManager am = ActivityManager.getService();
                am.setFocusedTask(Integer.parseInt(menu));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (taskId == -3) {
            mOperateManager.closeApp(Integer.parseInt(menu), appInfo.getPackageName());
        }
        mHoverDialog.dismiss();
        dialog.dismiss();
    }

    private void showDialog() {
        mMenuDialog.setOnMenuClick(this);
        if (!isRun()) {
            mMenuDialog.show(DialogType.BAR_LOCK_CLOSE, getAppInfo(), this, mTaskId);
        } else if (isLocked()) {
            mMenuDialog.show(DialogType.BAR_LOCK_OPEN, getAppInfo(), this, mTaskId);
        } else {
            mMenuDialog.show(DialogType.BAR_UNLOCK_OPEN, getAppInfo(), this, mTaskId);
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
                if (mTasks.size() > 1) {
                    mTaskDialog.show(DialogType.SELECT_TASK, getAppInfo(), this, mTasks);
                } else {
                    am.setFocusedTask(mTasks.iterator().next());
                }
            } else {
                mOperateManager.openApplication(getAppInfo()/*getAppInfo().getComponentName()*/);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        setRun(false);
    }

    public AppInfo getAppInfo() {
        return mAppInfo != null ? mAppInfo : mOperateManager.getAppInfo(mPackageName);
    }

    public String getPackageName() {
        return mPackageName;
    }

    public ComponentName getComponentName() {
        return mComponentName;
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
            mIconBackground.setBackground(
                    getResources().getDrawable(R.mipmap.status_bar_view_hover_selected_bg));
        } else {
            mIconBackground.setBackground(
                    getResources().getDrawable(R.mipmap.status_bar_view_run_bg));
        }
    }

    public boolean isRun() {
        return mIsRun;
    }

    public void setRun(boolean isRun) {
        mIsRun = isRun;
        if (mIsRun) {
            mIconBackground.setBackground(
                    getResources().getDrawable(R.mipmap.status_bar_view_run_bg));
        } else {
            mTaskId = -1;
            mIconBackground.setBackground(null);
        }
    }

    public int getTaskId() {
        return mTaskId;
    }

    public void setTaskId(int taskId) {
        mTaskId = taskId;
        initDialog();
    }

    public boolean noRunTask() {
        return mTasks.size() == 0;
    }

    public boolean containTask(int taskId) {
        return mTasks.contains((Integer) taskId);
    }

    public void addTaskId(int taskId) {
        mTasks.add(taskId);
    }

    public void closeTask(int taskId) {
        mTasks.remove((Integer)taskId);
        if (noRunTask()) {
            close();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initView();
        initData();
        initListener();
    }
}
