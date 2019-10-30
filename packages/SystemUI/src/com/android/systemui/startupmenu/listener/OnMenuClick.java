package com.android.systemui.startupmenu.listener;

import android.app.Dialog;
import android.view.View;

import com.android.systemui.startupmenu.bean.AppInfo;

public interface OnMenuClick {
    void menuClick(View view, Dialog dialog, AppInfo appInfo, String menu, int taskId);

    void sortShow(View view, Dialog dialog, String menu);
}
