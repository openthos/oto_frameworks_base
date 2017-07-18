package com.android.systemui.util;

public class InputAppInfo {
    private String mName;
    private boolean mIsSelected;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean selected) {
        mIsSelected = selected;
    }

    @Override
    public String toString() {
        return "AppInfo [name=" + mName + "]";
    }

}
