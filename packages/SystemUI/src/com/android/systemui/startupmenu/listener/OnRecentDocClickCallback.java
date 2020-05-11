package com.android.systemui.startupmenu.listener;

import com.android.systemui.startupmenu.bean.AppInfo;

public interface OnRecentDocClickCallback {

    void open(AppInfo appInfo);
}
