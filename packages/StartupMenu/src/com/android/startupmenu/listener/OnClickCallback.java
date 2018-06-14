package com.android.startupmenu.listener;

import com.android.startupmenu.bean.AppInfo;

public interface OnClickCallback {
    void open(AppInfo appInfo);

    void showDialog(int x, int y, AppInfo appInfo);
}
