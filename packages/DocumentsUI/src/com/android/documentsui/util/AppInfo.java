package com.android.documentsui.util;

import java.util.Date;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo implements Parcelable {

    private String appLabel;
    private Drawable appIcon;
    private Intent intent;
    private String pkgName;
    private Date date;
    private int number;

    private long cachesize;
    private long datasize;
    private long codesieze;

    protected AppInfo(Parcel in) {
        appLabel = in.readString();
        intent = in.readParcelable(Intent.class.getClassLoader());
        pkgName = in.readString();
        number = in.readInt();
        cachesize = in.readLong();
        datasize = in.readLong();
        codesieze = in.readLong();
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            AppInfo appInfo = new AppInfo();
            appInfo.setAppLabel(in.readString());
            appInfo.setIntent((Intent) in.readParcelable(Intent.class.getClassLoader()));
            appInfo.setPkgName(in.readString());
            appInfo.setNumber(in.readInt());
            appInfo.setCachesize(in.readLong());
            appInfo.setDatasize(in.readLong());
            appInfo.setCodesieze(in.readLong());
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(appLabel);
        parcel.writeParcelable(intent, i);
        parcel.writeString(pkgName);
        parcel.writeInt(number);
        parcel.writeLong(cachesize);
        parcel.writeLong(datasize);
        parcel.writeLong(codesieze);
    }
}
