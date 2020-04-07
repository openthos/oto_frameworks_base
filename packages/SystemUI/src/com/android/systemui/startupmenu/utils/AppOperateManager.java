package com.android.systemui.startupmenu.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.view.Display;

import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.dialog.StartupMenuDialog;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.RecentDataCallback;
import com.android.systemui.statusbar.phone.StatusBar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_AWARE;
import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_UNAWARE;

public class AppOperateManager {
    private static AppOperateManager instance;

    private List<AppInfo> mAppInfos;
    private List<AppInfo> mAppUseCountInfos;
    private Context mContext;
    private StatusBar mStatusBar;
    private RecentDataCallback mCallback;

    public static AppOperateManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppOperateManager(context);
        }
        return instance;
    }

    private AppOperateManager(Context context) {
        mContext = context;
        mAppInfos = new ArrayList<>();
        mAppUseCountInfos = new ArrayList<>();
        mStatusBar = SysUiServiceProvider.getComponent(mContext, StatusBar.class);
    }

    public List<AppInfo> loadAppsInfo() {
        PackageManager pm = mContext.getPackageManager();
        List<AppInfo> list = new ArrayList<>();
        AppInfo appInfo;
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        int queryFlags = MATCH_DIRECT_BOOT_AWARE | MATCH_DIRECT_BOOT_UNAWARE;
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, queryFlags);
        for (ResolveInfo info : infos) {
            appInfo = new AppInfo();
            appInfo.setLabel((String) info.loadLabel(pm));
            appInfo.setComponentName(new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name));
            Util.getPinyinList(appInfo);
            if (Util.FILEMANAGER.equals(info.activityInfo.packageName)) {
                Util.speApps.put(Util.FILEMANAGER, appInfo);
            } else if (Util.SETTINGS.equals(info.activityInfo.packageName)) {
                Util.speApps.put(Util.SETTINGS, appInfo);
            }
            list.add(appInfo);
        }
        return list;
    }

    public void updateAppsInfo(List<AppInfo> appsInfo, List<AppInfo> appsUseCountInfo) {
        mAppInfos.clear();
        mAppUseCountInfos.clear();
        mAppInfos.addAll(appsInfo);
        mAppUseCountInfos.addAll(appsUseCountInfo);
    }

    public List<AppInfo> getDataList() {
        return mAppInfos;
    }

    public List<AppInfo> getUseCountInfos() {
        return mAppUseCountInfos;
    }

    public void openApplication(AppInfo appInfo) {
        updateClick(appInfo);
        Intent intent = new Intent();
        intent.setComponent(appInfo.getComponentName());
        android.util.Log.e("lxx-open","name="+appInfo.getPackageName()+" ComponentName="+appInfo.getComponentName());
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mContext.startActivity(intent, null);
        dismissStartupMenuDialog();
    }

    public void addToTaskbar(int taskId, ComponentName componentName) {
        mStatusBar.addToTaskbar(taskId, componentName);
    }

    public void removeFromTaskbar(ComponentName componentName) {
        mStatusBar.removeFromTaskbar(componentName);
    }

    //public void removeFromTaskbar(String packageName) {
    //    mStatusBar.removeFromTaskbar(packageName);
    //}

    public void closeApp(int taskId, String packageName) {
        mStatusBar.closeApp(taskId, packageName);
    }

    public void uninstallApp(String packageName) {
        dismissStartupMenuDialog();
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        mContext.startActivity(intent);
    }

    public void removeAppFromRecent(AppInfo appInfo) {
        android.util.Log.e("lxx-remove","before---size="+mAppUseCountInfos.size());
        Util.removeAppInfo(appInfo.getPackageName(), mAppUseCountInfos);
        android.util.Log.e("lxx-remove","middle---size="+mAppUseCountInfos.size());
        Util.sortDatasByUseCount(mContext, mAppUseCountInfos);
        mCallback.udpateCallback();
    }

    private void updateClick(AppInfo appInfo) {
        appInfo.setUseCount(appInfo.getUseCount() + 1);
        android.util.Log.e("lxx-update","before--size="+mAppUseCountInfos.size());
        updateAppUseCount(appInfo);
        android.util.Log.e("lxx-update","after--size="+mAppUseCountInfos.size());
        Util.sortDatasByUseCount(mContext, mAppUseCountInfos);
        mCallback.udpateCallback();
    }

    private void updateAppUseCount(AppInfo info) {
        Iterator<AppInfo> iterator = mAppUseCountInfos.iterator();
        while (iterator.hasNext()) {
            AppInfo next = iterator.next();
            if (info.getPackageName().equals(next.getPackageName())) {
                iterator.remove();
            }
        }
        mAppUseCountInfos.add(info);
    }

    public void setRecentDataCallback(RecentDataCallback callback){
        mCallback = callback;
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
