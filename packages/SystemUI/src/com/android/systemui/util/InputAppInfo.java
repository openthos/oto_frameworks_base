package com.android.systemui.util;

public class InputAppInfo {
    private String mName;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    @Override
    public String toString() {
        return "AppInfo [name=" + mName + "]";
    }

}
