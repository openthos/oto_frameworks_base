package com.android.systemui.startupmenu.listener;

import com.android.systemui.startupmenu.bean.AppInfo;

import java.util.Map;

public interface DataCallback {
    void callback(Map<String, AppInfo> appInfoMaps);
}
