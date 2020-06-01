package com.android.systemui.calendar.view.listener;

import android.view.View;

import com.android.systemui.calendar.view.bean.DateBean;

public interface OnSingleChooseListener {
    void onSingleChoose(View view, DateBean date);
}
