package com.android.startupmenu.listener;

import android.app.Dialog;
import android.view.View;

import com.android.startupmenu.bean.AppInfo;

public interface OnMenuClick {
    void menuClick(View view, Dialog dialog, AppInfo appInfo, String menu);

    void sortShow(View view, Dialog dialog, String menu);
}
