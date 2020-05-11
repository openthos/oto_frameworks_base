package com.android.systemui.startupmenu.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.Display;
import android.widget.Toast;

import com.android.systemui.R;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.dialog.StartupMenuDialog;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.RecentAppDataCallback;
import com.android.systemui.statusbar.phone.StatusBar;

import java.io.File;
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
    private List<String> mAppLockedPackages;
    private Context mContext;
    private StatusBar mStatusBar;
    private RecentAppDataCallback mCallback;
    private PackageManager mPackageManager;

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
        mAppLockedPackages = new ArrayList<>();
        mStatusBar = SysUiServiceProvider.getComponent(mContext, StatusBar.class);
        mPackageManager = context.getPackageManager();
    }

    public List<AppInfo> loadAppsInfo() {
        setAppInfoLocked();
        List<AppInfo> list = new ArrayList<>();
        AppInfo appInfo;
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        int queryFlags = MATCH_DIRECT_BOOT_AWARE | MATCH_DIRECT_BOOT_UNAWARE;
        List<ResolveInfo> infos = mPackageManager.queryIntentActivities(intent, queryFlags);
        for (ResolveInfo info : infos) {
            if ((info.activityInfo.flags & ActivityInfo.FLAG_NO_HISTORY) != 0) {
                continue;
            }
            appInfo = new AppInfo();
            appInfo.setLabel((String) info.loadLabel(mPackageManager));
            appInfo.setComponentName(new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name));
            appInfo.setLocked(mAppLockedPackages.contains(info.activityInfo.packageName));
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
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mContext.startActivity(intent, null);
        dismissStartupMenuDialog();
    }

    public void openDoc(AppInfo appInfo) {
        String path = appInfo.getPath();
        String fileType = Util.getMIMEType(new File(path));
        List<ResolveInfo> resolveInfoList = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(mContext,
                    "com.android.systemui.fileprovider", new File(path));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(new File(path));
        }
        intent.setDataAndType(uri, fileType);
        resolveInfoList = mPackageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfoList.size() > 0) {
            if (android.os.Build.VERSION.SDK_INT >= 24) {
	            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            mContext.startActivity(intent);
            dismissStartupMenuDialog();
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.no_app_open_with)
                    , Toast.LENGTH_LONG).show();
        }
    }

    private void addToTaskbar(int taskId, AppInfo appInfo) {
        mStatusBar.addToTaskbar(taskId, appInfo);
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

    public void removeAppFromRecent(AppInfo appInfo) {
        Util.removeAppInfo(appInfo.getPackageName(), mAppUseCountInfos);
        Util.sortDatasByUseCount(mContext, mAppUseCountInfos);
        mCallback.udpateCallback();
    }

    private void updateClick(AppInfo appInfo) {
        appInfo.setUseCount(appInfo.getUseCount() + 1);
        updateAppUseCount(appInfo);
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

    private void setAppInfoLocked() {
        SharedPreferences sp = mContext.getSharedPreferences("lockedmap", 0);
        List<AppInfo> appLockedDatas = Util.deSerialization(sp.getString("lockedmap", null));
        if (appLockedDatas != null) {
            Iterator<AppInfo> iterator = appLockedDatas.iterator();
            while (iterator.hasNext()) {
                AppInfo next = iterator.next();
                mAppLockedPackages.add(next.getPackageName());
            }
        }
    }

    public void setRecentAppDataCallback(RecentAppDataCallback callback){
        mCallback = callback;
    }

    public void initTaskbarIcons() {
        for (AppInfo appInfo : mAppInfos) {
            if (mAppLockedPackages.contains(appInfo.getPackageName())) {
                addToTaskbar(-1, appInfo);
            }
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
