package com.android.systemui.dialog;

import android.content.ComponentName;
import android.content.Context;
import android.os.Process;
import android.os.UserManager;
import android.view.View;
import android.widget.FrameLayout;

import com.android.systemui.R;

/**
 * Created by ljh on 17-10-25.
 */

public class TaskbarIcon {
    private int taskId;
    private long userId;
    private ComponentName componentName;
    private FrameLayout iconLayout;
    private boolean isRun;
    private boolean isLocked;
    private boolean isFocus;
    private View focuseView;
    private View runView;

    public TaskbarIcon(ComponentName componentName, FrameLayout iconLayout) {
        this.componentName = componentName;
        this.iconLayout = iconLayout;
        focuseView = iconLayout.findViewById(R.id.activity_focused);
        runView = iconLayout.findViewById(R.id.activity_run);
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getUserId(Context context) {
        if(userId == -1) {
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            return userManager.getSerialNumberForUser(Process.myUserHandle());
        } else
            return userId;
    }

    public ComponentName getComponentName() {
        return componentName;
    }

    public void setComponentName(ComponentName componentName) {
        this.componentName = componentName;
    }

    public FrameLayout getIconLayout() {
        return iconLayout;
    }

    public void setIconLayout(FrameLayout iconLayout) {
        this.iconLayout = iconLayout;
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean run) {
        if (run) {
            runView.setVisibility(View.VISIBLE);
        } else {
            runView.setVisibility(View.GONE);
            setFocus(false);
        }
        isRun = run;
    }

    public boolean isFocus() {
        return isFocus;
    }

    public void setFocus(boolean focus) {
        focuseView.setVisibility(focus ? View.VISIBLE : View.GONE);
        isFocus = focus;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}