package com.android.systemui.calendar.view.listener;

import android.view.View;

import com.android.systemui.calendar.view.bean.DateBean;

public interface OnMultiChooseListener {
    void onMultiChoose(View view, DateBean date, boolean flag);
}
