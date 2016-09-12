package com.android.documentsui.util;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.documentsui.R;

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
    static final int MAX_CHARACTER_COUNT = 9;

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

    public String limitNameLength(String appName, Context mContext, AppInfo appInfo) {
        int mCharacterCount = 0;
        int mInputNum = 0;
        for (int i = 0; i<appName.length(); i++) {
             String str = appName.substring(i, i + 1);
             mInputNum++;
             mCharacterCount += regexInput(str);
             if (mCharacterCount > MAX_CHARACTER_COUNT) {
                break;
             }
        }
        if (mCharacterCount > MAX_CHARACTER_COUNT && (mCharacterCount % 2 == 0)) {
            appName = appName.substring(0,mInputNum);
            if (!appName.equals(appInfo.getAppLabel())) {
                appName += mContext.getString(R.string.omit);
            }

        } else if (mCharacterCount > MAX_CHARACTER_COUNT && (mCharacterCount % 2 == 1)) {
            appName = appName.substring(0, mInputNum - 1) + mContext.getString(R.string.omit);
        }
        return appName;
    }

    private int regexInput(String subString) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(subString);
        if (m.matches()) {
            return 2;
        } else {
            return 1;
        }
    }

}
