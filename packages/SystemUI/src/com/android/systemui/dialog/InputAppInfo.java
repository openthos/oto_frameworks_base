package com.android.systemui.dialog;

import android.view.textservice.SpellCheckerSubtype;

public class InputAppInfo {
    private String mName;
    private boolean mIsSelected;
    private SpellCheckerSubtype mSubtype;

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

    public SpellCheckerSubtype getSubtype() {
        return mSubtype;
    }

    public void setSubtype(SpellCheckerSubtype subtype) {
        mSubtype = subtype;
    }

    @Override
    public String toString() {
        return "AppInfo [name=" + mName + "]";
    }

}
