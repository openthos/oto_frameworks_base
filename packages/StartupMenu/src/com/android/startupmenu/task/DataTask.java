package com.android.startupmenu.task;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import com.android.startupmenu.bean.AppInfo;
import com.android.startupmenu.listener.Callback;
import com.android.startupmenu.util.SqliteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DataTask extends AsyncTask<Void, Integer, List<AppInfo>> {
    private Context mContext;
    private Callback mCallback;
    private List<String> mFilterAppPkgNames;

    public DataTask(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
        mFilterAppPkgNames = new ArrayList<>();
        String[] array = context.getResources().getStringArray(
                 com.android.internal.R.array.poor_quality_apps);
        mFilterAppPkgNames.addAll(Arrays.asList(array));
    }

    @Override
    protected List<AppInfo> doInBackground(Void... voids) {
        List<AppInfo> appInfos = new ArrayList<>();
        Map<String, AppInfo> map = SqliteOpenHelper.getInstance(mContext).QueryAllDataStorage();
        PackageManager pm = mContext.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        AppInfo appInfo;
        for (ResolveInfo reInfo : resolveInfos) {
            String packageName = reInfo.activityInfo.packageName;
            if (isFilterApp(packageName)) {
                continue;
            }
            appInfo = new AppInfo();
            appInfo.setAppLabel((String) reInfo.loadLabel(pm));
            appInfo.setPkgName(packageName);
            appInfo.setInstallTime(
                    new File(reInfo.activityInfo.applicationInfo.sourceDir).lastModified());
            appInfo.setAppIcon(reInfo.loadIcon(pm));
            appInfo.setActivityName(reInfo.activityInfo.name);
            appInfo.setClickCounts(getClickCounts(map.get(packageName)));
            appInfo.setSystemApp(isSystemApp(reInfo));
            appInfo.setFullScreen(isFullScreen(packageName));
            appInfos.add(appInfo);
        }
        return appInfos;
    }

    private boolean isFilterApp(String pkgName) {
        return mFilterAppPkgNames.contains(pkgName);
    }

    private int getClickCounts(AppInfo appInfo) {
        return appInfo == null ? 0 : appInfo.getClickCounts();
    }

    private boolean isSystemApp(ResolveInfo resolveInfo) {
        return (resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
    }

    private boolean isFullScreen(String packageName) {
        return ApplicationInfo.isMaximizedStyleWindow(packageName)
                || ApplicationInfo.isRealFullScreenStyleWindow(packageName);
    }

    @Override
    protected void onPostExecute(List<AppInfo> appInfos) {
        mCallback.callback(appInfos);
    }
}
