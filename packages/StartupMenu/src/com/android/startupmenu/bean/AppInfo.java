package com.android.startupmenu.bean;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public class AppInfo{

    private String appLabel;
    private Drawable appIcon;
    private String pkgName;
    private long installTime;
    private int clickCounts;
    private String activityName;

    public AppInfo() {
    }

    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public long getInstallTime() {
        return installTime;
    }

    public void setInstallTime(long installTime) {
        this.installTime = installTime;
    }

    public int getClickCounts() {
        return clickCounts;
    }

    public void setClickCounts(int clickCounts) {
        this.clickCounts = clickCounts;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public Intent getIntent(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
        intent.setComponent(new ComponentName(pkgName, activityName));
        return intent;
    }
}
