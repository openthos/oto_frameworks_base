package com.android.systemui.startupmenu.listener;

import com.android.systemui.startupmenu.bean.AppInfo;

public interface OnRecentClickCallback {

    void open(AppInfo appInfo);

    void showDialog(int x, int y, AppInfo appInfo);
}
