package com.android.systemui.calendar.view.listener;

import android.view.View;
import android.widget.TextView;

import com.android.systemui.calendar.view.bean.DateBean;

public interface CalendarViewAdapter {
    TextView[] convertView(View view, DateBean date);
}
