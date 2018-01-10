package com.android.startupmenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.android.startupmenu.bean.AppInfo;
import com.android.startupmenu.util.Constants;
import com.android.startupmenu.util.SqliteOperate;

import java.io.File;
import java.util.List;

public class SqliteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.ACTION_SEND_CLICK_INFO)) {
            String pkgName = intent.getStringExtra("keyAddInfo");

            PackageManager pm = context.getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
            for (ResolveInfo reInfo : resolveInfos) {
                if (pkgName.equals(reInfo.activityInfo.packageName)) {
                    AppInfo appInfo = new AppInfo();
                    appInfo.setInstallTime(
                            new File(reInfo.activityInfo.applicationInfo.sourceDir).lastModified());
                    appInfo.setAppLabel((String) reInfo.loadLabel(pm));
                    appInfo.setPkgName(pkgName);
                    SqliteOperate.updateDataStorage(context, appInfo);
                    break;
                }
            }
        }
    }
}
