package com.android.systemui.listener;

import com.android.systemui.bean.AppInfo;

public interface OnClickCallback {
    void open(AppInfo appInfo);

    void showDialog(int x, int y, AppInfo appInfo);
}
