package com.android.documentsui.util;

import java.util.Date;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class AppInfo {

    private String appLabel;
    private Drawable appIcon;
    private Intent intent;
    private String pkgName;
    private Date date;
    private int number;

    private long cachesize;
    private long datasize;
    private long codesieze;

    public long getCachesize() {
        return cachesize;
    }

    public void setCachesize(long cachesize) {
        this.cachesize = cachesize;
    }

    public long getDatasize() {
        return datasize;
    }

    public void setDatasize(long datasize) {
        this.datasize = datasize;
    }

    public long getCodesieze() {
        return codesieze;
    }

    public void setCodesieze(long codesieze) {
        this.codesieze = codesieze;
    }

    public AppInfo() {
    }

    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String appName) {
        this.appLabel = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
