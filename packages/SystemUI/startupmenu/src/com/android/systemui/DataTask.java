package com.android.systemui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import com.android.systemui.bean.AppInfo;
import com.android.systemui.listener.Callback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTask extends AsyncTask<Void, Integer, Map<String, AppInfo>> {
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
    protected Map<String, AppInfo> doInBackground(Void... voids) {
        Map<String, AppInfo> appInfos = new HashMap<>();
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
            appInfo.setSystemApp(isSystemApp(reInfo));
            appInfo.setClickCounts(0);
            appInfo.setFullScreen(isFullScreen(packageName));
            appInfos.put(packageName, appInfo);
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
    protected void onPostExecute(final Map<String, AppInfo> appInfos) {
        SqliteOpenHelper.getInstance(mContext).queryAllDataStorage(new Callback() {
            @Override
            public void callback(Map<String, AppInfo> appInfoMaps) {
                for (AppInfo appInfo : appInfoMaps.values()) {
                    appInfos.get(appInfo.getPkgName()).setClickCounts(appInfo.getClickCounts());
                }
                mCallback.callback(appInfos);
            }
        });
    }
}
