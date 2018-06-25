package com.android.systemui.listener;

import com.android.systemui.bean.AppInfo;

import java.util.Map;

public interface Callback {
    void callback(Map<String, AppInfo> appInfoMaps);
}
