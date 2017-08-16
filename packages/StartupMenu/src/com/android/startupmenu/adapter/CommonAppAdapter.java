package com.android.startupmenu.adapter;

import com.android.startupmenu.R;

import java.util.ArrayList;

import com.android.startupmenu.dialog.CommonAppDialog;
import com.android.startupmenu.bean.AppInfo;

import android.content.Context;

public class CommonAppAdapter extends BasicAdapter {

    public CommonAppAdapter(Context context, ArrayList<AppInfo> appInfos) {
        super(context, appInfos);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_listview_item;
    }

    @Override
    public int getExitResource() {
        return R.color.appUsuallyBackground;
    }

    @Override
    public int getEnterResource() {
        return R.drawable.power_background;
    }

    @Override
    public void showDialog(int x, int y, AppInfo appInfo) {
        CommonAppDialog commonAppDialog =
                new CommonAppDialog(mActivity, R.style.dialog, appInfo);
        commonAppDialog.showDialog(x, y);
    }
}
