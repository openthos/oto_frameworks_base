package com.android.systemui.startupmenu.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Display;

import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.dialog.StartupMenuDialog;
import com.android.systemui.startupmenu.LaunchAppUtil;
import com.android.systemui.startupmenu.SqliteOpenHelper;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.DataCallback;
import com.android.systemui.statusbar.phone.StatusBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppOperateManager {
    private static AppOperateManager instance;

    private List<AppInfo> mAppInfos;
    private Context mContext;
    private StatusBar mStatusBar;

    public static AppOperateManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppOperateManager(context);
        }
        return instance;
    }

    private AppOperateManager(Context context) {
        mContext = context;
        mAppInfos = new ArrayList<>();
        mStatusBar = SysUiServiceProvider.getComponent(mContext, StatusBar.class);
    }


    public void reloadData() {
        SqliteOpenHelper.getInstance(mContext).queryAllDataStorage(new DataCallback() {
            @Override
            public void callback(Map<String, AppInfo> appInfoMap) {
                mAppInfos.clear();
                for (AppInfo appInfo : appInfoMap.values()) {
                    mAppInfos.add(appInfo);
                }
            }
        });
    }

    public void reloadData(final DataCallback callback) {
        SqliteOpenHelper.getInstance(mContext).queryAllDataStorage(new DataCallback() {
            @Override
            public void callback(Map<String, AppInfo> appInfoMap) {
                mAppInfos.clear();
                for (AppInfo appInfo : appInfoMap.values()) {
                    mAppInfos.add(appInfo);
                }
                callback.callback(appInfoMap);
            }
        });
    }

    public List<AppInfo> getDataList() {
        return mAppInfos;
    }

    public void openApplication(ComponentName componentName) {
        LaunchAppUtil.launchApp(mContext, componentName);
        updateClick(componentName.getPackageName());
        dismissStartupMenuDialog();
    }

    public void addToTaskbar(int taskId, ComponentName componentName) {
        mStatusBar.addToTaskbar(taskId, componentName);
    }

    public void removeFromTaskbar(ComponentName componentName) {
        mStatusBar.removeFromTaskbar(componentName);
    }

    public void removeFromTaskbar(String packageName) {
        mStatusBar.removeFromTaskbar(packageName);
    }

    public void closeApp(int taskId, String packageName) {
        mStatusBar.closeApp(taskId, packageName);
    }

    public void uninstallApp(String packageName) {
        dismissStartupMenuDialog();
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        mContext.startActivity(intent);
    }

    private void updateClick(String packageName) {
        AppInfo appInfo = getAppInfo(packageName);
        if (appInfo != null) {
            appInfo.setUseCounts(appInfo.getUseCounts() + 1);
            SqliteOpenHelper.getInstance(mContext).updateDataClick(appInfo);
        }
    }

    public AppInfo getAppInfo(String packageName) {
        for (AppInfo appInfo : mAppInfos) {
            if (packageName.equals(appInfo.getPackageName())) {
                return appInfo;
            }
        }
        return null;
    }

    public void dismissStartupMenuDialog() {
        StartupMenuDialog startupMenuDialog = mStatusBar.getStartupMenuDialog();
        if (startupMenuDialog != null && startupMenuDialog.isShowing()) {
            startupMenuDialog.dismiss();
        }
    }
}
