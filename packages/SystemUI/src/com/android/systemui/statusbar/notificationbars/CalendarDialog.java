package com.android.systemui.statusbar.notificationbars;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.android.systemui.R;

import java.util.Date;
import java.text.SimpleDateFormat;

import android.provider.Settings;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.util.Calendar;

import com.android.systemui.statusbar.notificationbars.CalendarView.OnCalendarClickListener;
import com.android.systemui.statusbar.notificationbars.CalendarView.OnCalendarDateChangedListener;

public class CalendarDialog extends BaseSettingDialog
        implements OnClickListener, OnCalendarClickListener, OnCalendarDateChangedListener {

    private static final String TIME_FORMAT_24 = "24";
    private CalendarView mCalendarView;
    public static final int YEAR_BREAK_NUMBER = -11;// Across the years
    public static final int NEXT_DATE = 1;// year, month, and day to add 1
    public static final int UPDATE_DATE_INTERVAL = 1000;// per 1 second

    private TextView mCalendarMonth;
    private TextView mCalendarTime;
    private TextView mCalendarDate;
    private TextView mCalendarSet;

    private boolean mIs24Hour;
    private SimpleDateFormat mDateFormat;
    private SimpleDateFormat mTimeFormat;

    public CalendarDialog(Context context) {
        super(context);
    }

    @Override
    protected void initViews() {
        mContentView = LayoutInflater.from(mContext)
                .inflate(R.layout.popupwindows_calendar, null);
        setContentView(mContentView);
        mCalendarTime = (TextView) mContentView.findViewById(R.id.calendar_time);
        mCalendarDate = (TextView) mContentView.findViewById(R.id.calendar_date);
        mCalendarMonth = (TextView)
                mContentView.findViewById(R.id.popupwindow_calendar_month);
        mCalendarView = (CalendarView) mContentView.findViewById(R.id.popupwindow_calendar);
        mCalendarSet = (TextView) mContentView.findViewById(R.id.popupwindow_calendar_bt_enter);

        initData();
        initListener();
    }

    private void initData() {
        mCalendarSet.setText(R.string.set_date_and_time);

        updateFormat();
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd EEEE");
    }

    private void initListener() {
        mContentView.findViewById(R.id.popupwindow_calendar_next_month).setOnClickListener(this);
        mContentView.findViewById(R.id.popupwindow_calendar_last_month).setOnClickListener(this);
        mCalendarSet.setOnClickListener(this);
        mCalendarView.setOnCalendarDateChangedListener(this);
        mCalendarView.setOnCalendarClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popupwindow_calendar_bt_enter:
                Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(intent);
                dismiss();
                break;
            case R.id.popupwindow_calendar_next_month:
                mCalendarView.nextMonth();
                break;
            case R.id.popupwindow_calendar_last_month:
                mCalendarView.lastMonth();
                break;
        }
    }

    @Override
    public void onCalendarClick(int row, int col, String dateFormat) {
        int month = Integer.parseInt(dateFormat.substring(
                dateFormat.indexOf("-") + NEXT_DATE,
                dateFormat.lastIndexOf("-")));
        if (mCalendarView.getCalendarMonth() - month == NEXT_DATE
                || mCalendarView.getCalendarMonth() - month == YEAR_BREAK_NUMBER) {
            mCalendarView.lastMonth();
        } else if (month - mCalendarView.getCalendarMonth() == NEXT_DATE
                || month - mCalendarView.getCalendarMonth() == YEAR_BREAK_NUMBER) {
            mCalendarView.nextMonth();
        } else {
            mCalendarView.removeAllBgColor();
            mCalendarView.setCalendarDayBgColor(dateFormat,
                    R.drawable.status_bar_calendar_background);
        }
    }

    @Override
    public void onCalendarDateChanged(int year, int month) {
        mCalendarMonth.setText(getMonthTetx(year, month));
    }

    private void updateFormat() {
        mIs24Hour = TIME_FORMAT_24.equals(Settings.System.getString(
                getContext().getContentResolver(), Settings.System.TIME_12_24));
        if (mIs24Hour) {
            mTimeFormat = new SimpleDateFormat("HH:mm:ss");
        } else {
            mTimeFormat = new SimpleDateFormat("hh:mm:ss a");
        }
    }

    private String showMonth(Date date) {
        return mDateFormat.format(date);
    }

    private String showTime(Date date) {
        return mTimeFormat.format(date);
    }

    private String getMonthTetx(int year, int month) {
        if (month < 10) {
            return year + "-0" + month;
        } else {
            return year + "-" + month;
        }
    }

    public void showCalendar() {
        updateFormat();
        if (mCalendarView != null) {
            mCalendarView.showCalendar();
        }
    }

    private void updateView() {
        new Thread() {
            @Override
            public void run() {
                while (isShowing()) {
                    final Date date = Calendar.getInstance().getTime();
                    mContentView.post(new Runnable() {
                        @Override
                        public void run() {
                            mCalendarTime.setText(showTime(date));
                            mCalendarDate.setText(showMonth(date));
                            mCalendarMonth.setText(getMonthTetx(mCalendarView.getCalendarYear(),
                                    mCalendarView.getCalendarMonth()));
                        }
                    });
                    try {
                        Thread.sleep(UPDATE_DATE_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void show(View v) {
        super.show(v);
        /**
         * Only show year and month.(yyyy-MM)
         */
        Calendar calendar = Calendar.getInstance();
        int years = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        mCalendarView.clearAll();
        mCalendarMonth.setText(getMonthTetx(years, month));
        mCalendarView.showCalendar(years, month);
        updateView();
    }
}
