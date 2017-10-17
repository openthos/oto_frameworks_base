package com.android.systemui.dialog;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.systemui.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarDialog extends BaseDialog implements OnClickListener , Runnable{

    public static final int COLOR_VIEW_FOCUS = Color.parseColor("#2b1f52");
    String mDate;
    String mStr;
    private CalendarView mCalendarView;
    private Handler mHandler;
    private Calendar mCalendar;
    public static final int TIME_CUT_BEGIN = 11;
    public static final int TIME_CUT_THIRTEEN = 13;
    public static final int TIME_CUT_EIGHTEEN = 22;
    public static final int WEEK_CUT_BEGIN = 21;
    public static final int YEAR_BREAK_NUMBER = -11;// Across the years
    public static final int NEXT_DATE = 1;// year, month, and day to add 1
    public static final int THREAD_SEND_MESSAGE = 100;// send message to handler
    public static final int UPDATE_DATE_INTERVAL = 1000;// per 1 second
    public static final int TIME_FORMAT_TWELVE = 12;
    public String mYear, mMonth, mDay, mMonthText;
    private TextView mCalendarMonth;
    private TextView mCalendarTime;
    private TextView mCalendarDate;
    private TextView mCalendarEnter;
    private RelativeLayout mLastMonth;
    private RelativeLayout mNextMonth;

    public CalendarDialog(Context context) {
        super(context);
        mContentView =
                LayoutInflater.from(getContext()).inflate(R.layout.popupwindows_calendar, null);
        setContentView(mContentView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        mCalendarTime = (TextView) mContentView.findViewById(R.id.calendar_time);
        mCalendarDate = (TextView) mContentView.findViewById(R.id.calendar_date);
        mCalendarMonth = (TextView) mContentView.findViewById(R.id.calendar_month);
        mCalendarView = (CalendarView) mContentView.findViewById(R.id.calendar_view);
        mCalendarEnter = (TextView)mContentView.findViewById(R.id.calendar_enter);
        mLastMonth = (RelativeLayout) mContentView.findViewById(R.id.calendar_last_month);
        mNextMonth = (RelativeLayout) mContentView.findViewById(R.id.calendar_next_month);
    }

    @Override
    public void initData() {
        mCalendar = Calendar.getInstance();
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                mStr = (String)msg.obj;
                mDate = (String)msg.obj;
                mCalendarTime.setText(showTime(mStr));
                mCalendarDate.setText(showMonth(mStr));
                mCalendarEnter.setText(R.string.set_date_and_time);
                mCalendarMonth.setText(mCalendarView.getCalendarYear() + mYear
                        + mCalendarView.getCalendarMonth() + mMonthText);
            }
        };
        new Thread(this).start();

        List<String> list = new ArrayList<String>();
        list.add("2014-04-01");
        list.add("2014-04-02");
        mCalendarView.addMarks(list, 0);
    }

    @Override
    public void initListener() {
        mCalendarView.setOnCalendarClickListener(new CalendarView.OnCalendarClickListener() {
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
                            R.mipmap.status_bar_calendar_background);
                    mDate = dateFormat;
                }
            }
        });

        mCalendarView.setOnCalendarDateChangedListener(
                new CalendarView.OnCalendarDateChangedListener() {
            public void onCalendarDateChanged(int year, int month) {
                mCalendarMonth.setText(year + mYear + month + mMonthText);
            }
        });

        mCalendarEnter.setOnClickListener(this);
        mNextMonth.setOnClickListener(this);
        mLastMonth.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.calendar_enter:
            getContext().startActivity(new Intent(Settings.ACTION_DATE_SETTINGS)
             .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            CalendarDialog.this.dismiss();
            break;
        case R.id.calendar_next_month:
            mCalendarView.nextMonth();
            break;
        case R.id.calendar_last_month:
            mCalendarView.lastMonth();
            break;
        }
    }

    public String showMonth(String str) {
        String week = str.substring(WEEK_CUT_BEGIN, str.length());
        return str.substring(0, TIME_CUT_BEGIN) + " " + week;
    }

    public String showTime(String str) {
        Locale locale = getContext().getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            String time = str.substring(TIME_CUT_THIRTEEN, TIME_CUT_EIGHTEEN);
            if (!DateFormat.is24HourFormat(getContext(), ActivityManager.getCurrentUser())) {
                if (mCalendar.get(Calendar.AM_PM) != 0) {
                    int hhFormat24 = Integer.parseInt(time.substring(0, 2));
                    int hhFormat12 = hhFormat24 - TIME_FORMAT_TWELVE;
                    if (hhFormat24 <= 12) {
                        if (hhFormat24 == 00) {
                            String timeFormat12 = 12 + time.substring(2);
                            return getContext().getString(R.string.morning)+" "+timeFormat12;
                        }
                        return getContext().getString(R.string.morning) + " " + time;
                    }
                    String timeFormat12 = hhFormat12 + time.substring(2);
                    return getContext().getString(R.string.afternoon) + " " + timeFormat12;
                } else {
                    int hhFormat24 = Integer.parseInt(time.substring(0, 2));
                    if (hhFormat24 > 12) {
                        int hhFormat12 = hhFormat24 - TIME_FORMAT_TWELVE;
                        String timeFormat12 = hhFormat12 + time.substring(2);
                        return getContext().getString(R.string.afternoon) + " " + timeFormat12;
                    }
                    return getContext().getString(R.string.morning) + " " + time;
                }
            }
            return time;
        } else {
            String time = str.substring(TIME_CUT_THIRTEEN - NEXT_DATE,
                                          TIME_CUT_EIGHTEEN - NEXT_DATE);
            if (!DateFormat.is24HourFormat(getContext(), ActivityManager.getCurrentUser())) {
                if (mCalendar.get(Calendar.AM_PM) != 0) {
                    int hhFormat24 = Integer.parseInt(time.substring(0, 2));
                    int hhFormat12 = hhFormat24 - TIME_FORMAT_TWELVE;
                    if (hhFormat24 <= 12) {
                        if (hhFormat24 == 00) {
                            String timeFormat12 = 12 + time.substring(2);
                            return timeFormat12+" "+getContext().getString(R.string.morning);
                        }
                        return time + " " + getContext().getString(R.string.morning);
                    }
                    String timeFormat12 = hhFormat12 + time.substring(2) + " " +
                                                   getContext().getString(R.string.afternoon);
                    return timeFormat12;
                } else {
                    int hhFormat24 = Integer.parseInt(time.substring(0, 2));
                    if (hhFormat24 > 12) {
                        int hhFormat12 = hhFormat24 - TIME_FORMAT_TWELVE;
                        String timeFormat12 = hhFormat12 + time.substring(2) + " " +
                                                    getContext().getString(R.string.afternoon);
                        return timeFormat12;
                    }
                    return time + " " + getContext().getString(R.string.morning);
                }
            }
            return time;
        }
    }

    @Override
    public void run() {
        try {
            while(true) {
                mYear = getContext().getString(R.string.year);
                mMonth = getContext().getString(R.string.month);
                mDay = getContext().getString(R.string.day);
                mMonthText = getContext().getString(R.string.text_month);
                SimpleDateFormat formatter = new SimpleDateFormat
                                              ("yyyy" + mYear + "MM" + mMonth + "dd" +
                                               mDay +"  "+ "HH:mm:ss EEEE" , Locale.getDefault());
                String str = formatter.format(new Date());
                mHandler.sendMessage(mHandler.obtainMessage(THREAD_SEND_MESSAGE, str));
                Thread.sleep(UPDATE_DATE_INTERVAL);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show(View v) {
        super.show(v);
        /**
         * Only show year and month.(yyyy-MM)
         */
        Calendar calendar=Calendar.getInstance();
        int years = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        mCalendarView.clearAll();
        mCalendarMonth.setText(years + mYear  + month + mMonthText);
        mCalendarView.showCalendar(years, month);
    }
}
