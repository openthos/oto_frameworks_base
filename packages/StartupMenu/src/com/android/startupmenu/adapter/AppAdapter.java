package com.android.startupmenu.adapter;

import java.util.List;

import com.android.startupmenu.R;
import com.android.startupmenu.bean.AppInfo;

import android.content.Context;

import com.android.startupmenu.dialog.AppDialog;

public class AppAdapter extends BasicAdapter {

    public AppAdapter(Context context, List<AppInfo> appInfos) {
        super(context, appInfos);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_gridview;
    }

    @Override
    public int getExitResource() {
        return android.R.color.transparent;
    }

    @Override
    public int getEnterResource() {
        return R.color.app_background;
    }

    @Override
    public void showDialog(int x, int y, AppInfo appInfo) {
        AppDialog appDialog = new AppDialog(mActivity, R.style.dialog, appInfo);
        appDialog.showDialog(x, y);
    }
}
