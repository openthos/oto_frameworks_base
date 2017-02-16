package com.android.systemui.statusbar.notificationbars;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.R;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.graphics.Color;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import android.app.ActivityManager;
import android.text.format.DateFormat;
import java.util.Calendar;

import com.android.systemui.statusbar.notificationbars.CalendarView.OnCalendarClickListener;
import com.android.systemui.statusbar.notificationbars.CalendarView.OnCalendarDateChangedListener;


public class CalendarDialog extends BaseSettingDialog implements OnClickListener , Runnable{

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
    private TextView mPopupwindowCalendarMonth;

    public CalendarDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViews() {
        final AudioManager audioManager = (AudioManager) mContext.getSystemService(
                                                                      Context.AUDIO_SERVICE);
        View mediaView = LayoutInflater.from(mContext)
                                            .inflate(R.layout.popupwindows_calendar, null);
        setContentView(mediaView);
        final TextView calendarTime = (TextView) mediaView.findViewById(R.id.calendar_time);
        final TextView calendarDate = (TextView) mediaView.findViewById(R.id.calendar_date);
        mPopupwindowCalendarMonth = (TextView)
                                         mediaView.findViewById(R.id.popupwindow_calendar_month);
        mCalendarView = (CalendarView) mediaView.findViewById(R.id.popupwindow_calendar);
        final TextView popupwindow_calendar_bt_enter = (TextView)
                                               mediaView.findViewById(R.id.popupwindow_calendar_bt_enter);
        mCalendar = Calendar.getInstance();
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                mStr = (String)msg.obj;
                mDate = (String)msg.obj;
                calendarTime.setText(showTime(mStr));
                calendarDate.setText(showMonth(mStr));
                popupwindow_calendar_bt_enter.setText(R.string.set_date_and_time);
                mPopupwindowCalendarMonth.setText(mCalendarView.getCalendarYear() + mYear
                                           + mCalendarView.getCalendarMonth() + mMonthText);
            }
        };
        new Thread(this).start();

        List<String> list = new ArrayList<String>();
        list.add("2014-04-01");
        list.add("2014-04-02");
        mCalendarView.addMarks(list, 0);

        mCalendarView.setOnCalendarClickListener(new OnCalendarClickListener() {
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
                    mDate = dateFormat;
                }
            }
        });

        mCalendarView.setOnCalendarDateChangedListener(new OnCalendarDateChangedListener() {
            public void onCalendarDateChanged(int year, int month) {
                mPopupwindowCalendarMonth.setText(year + mYear + month + mMonthText);
            }
        });

        RelativeLayout popupwindow_calendar_last_month = (RelativeLayout)
                       mediaView.findViewById(R.id.popupwindow_calendar_last_month);
        RelativeLayout popupwindow_calendar_next_month = (RelativeLayout)
                       mediaView.findViewById(R.id.popupwindow_calendar_next_month);
        popupwindow_calendar_bt_enter.setOnClickListener(this);
        popupwindow_calendar_next_month.setOnClickListener(this);
        popupwindow_calendar_last_month.setOnClickListener(this);
        mContentView = mediaView;
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.popupwindow_calendar_bt_enter:
            mContext.startActivity(new Intent(Settings.ACTION_DATE_SETTINGS)
             .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            CalendarDialog.this.dismiss();
            break;
        case R.id.popupwindow_calendar_next_month:
            mCalendarView.nextMonth();
            break;
        case R.id.popupwindow_calendar_last_month:
            mCalendarView.lastMonth();
            break;
        }
    }

    public String showMonth(String str) {
        String week = str.substring(WEEK_CUT_BEGIN, str.length());
        return str.substring(0, TIME_CUT_BEGIN) + " " + week;
    }

    public String showTime(String str) {
        Locale locale = mContext.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            String time = str.substring(TIME_CUT_THIRTEEN, TIME_CUT_EIGHTEEN);
            if (!DateFormat.is24HourFormat(mContext, ActivityManager.getCurrentUser())) {
                if (mCalendar.get(Calendar.AM_PM) != 0) {
                    int hhFormat24 = Integer.parseInt(time.substring(0, 2));
                    int hhFormat12 = hhFormat24 - TIME_FORMAT_TWELVE;
                    if (hhFormat24 <= 12) {
                        if (hhFormat24 == 00) {
                            String timeFormat12 = 12 + time.substring(2);
                            return mContext.getString(R.string.morning)+" "+timeFormat12;
                        }
                        return mContext.getString(R.string.morning) + " " + time;
                    }
                    String timeFormat12 = hhFormat12 + time.substring(2);
                    return mContext.getString(R.string.afternoon) + " " + timeFormat12;
                } else {
                    int hhFormat24 = Integer.parseInt(time.substring(0, 2));
                    if (hhFormat24 > 12) {
                        int hhFormat12 = hhFormat24 - TIME_FORMAT_TWELVE;
                        String timeFormat12 = hhFormat12 + time.substring(2);
                        return mContext.getString(R.string.afternoon) + " " + timeFormat12;
                    }
                    return mContext.getString(R.string.morning) + " " + time;
                }
            }
            return time;
        } else {
            String time = str.substring(TIME_CUT_THIRTEEN - NEXT_DATE,
                                          TIME_CUT_EIGHTEEN - NEXT_DATE);
            if (!DateFormat.is24HourFormat(mContext, ActivityManager.getCurrentUser())) {
                if (mCalendar.get(Calendar.AM_PM) != 0) {
                    int hhFormat24 = Integer.parseInt(time.substring(0, 2));
                    int hhFormat12 = hhFormat24 - TIME_FORMAT_TWELVE;
                    if (hhFormat24 <= 12) {
                        if (hhFormat24 == 00) {
                            String timeFormat12 = 12 + time.substring(2);
                            return timeFormat12+" "+mContext.getString(R.string.morning);
                        }
                        return time + " " + mContext.getString(R.string.morning);
                    }
                    String timeFormat12 = hhFormat12 + time.substring(2) + " " +
                                                   mContext.getString(R.string.afternoon);
                    return timeFormat12;
                } else {
                    int hhFormat24 = Integer.parseInt(time.substring(0, 2));
                    if (hhFormat24 > 12) {
                        int hhFormat12 = hhFormat24 - TIME_FORMAT_TWELVE;
                        String timeFormat12 = hhFormat12 + time.substring(2) + " " +
                                                    mContext.getString(R.string.afternoon);
                        return timeFormat12;
                    }
                    return time + " " + mContext.getString(R.string.morning);
                }
            }
            return time;
        }
    }

    @Override
    public void run() {
        try {
            while(true) {
                mYear = mContext.getString(R.string.year);
                mMonth = mContext.getString(R.string.month);
                mDay = mContext.getString(R.string.day);
                mMonthText = mContext.getString(R.string.text_month);
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
        SimpleDateFormat formatter = new SimpleDateFormat
                                    ("yyyy" + mYear + "MM" + mMonth + "dd" +
                                    mDay +"  "+ "HH:mm:ss EEEE" , Locale.getDefault());
        String str = formatter.format(new Date());
        int years = Integer.parseInt(str.substring(0, 4));
        int month = Integer.parseInt(str.substring(5, 7));
        mDate = str.substring(0, 10);
        mCalendarView.clearAll();
        mPopupwindowCalendarMonth.setText(years + mYear  + month + mMonthText);
        mCalendarView.showCalendar(years, month);
        mCalendarView.setCalendarDayBgColor(mDate, R.drawable.status_bar_calendar_background);
    }
}
