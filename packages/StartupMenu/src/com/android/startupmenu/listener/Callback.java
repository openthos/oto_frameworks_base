package com.android.startupmenu.listener;

import com.android.startupmenu.bean.AppInfo;

import java.util.List;

public interface Callback {
    void callback(List<AppInfo> appInfos);
}
