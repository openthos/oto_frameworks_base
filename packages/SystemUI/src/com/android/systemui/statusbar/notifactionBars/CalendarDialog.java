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

import com.android.systemui.statusbar.notificationbars.CalendarView.OnCalendarClickListener;
import com.android.systemui.statusbar.notificationbars.CalendarView.OnCalendarDateChangedListener;


public class CalendarDialog extends BaseSettingDialog implements OnClickListener , Runnable{

    public static final int COLOR_VIEW_FOCUS = Color.parseColor("#2b1f52");
    String mDate;
    String mStr;
    private CalendarView mCalendar;
    private Handler mHandler;
    public static final int TIME_CUT_BEGIN = 11;
    public static final int TIME_CUT_THIRTEEN = 13;
    public static final int TIME_CUT_EIGHTEEN = 22;
    public static final int WEEK_CUT_BEGIN = 21;
    public static final int YEAR_BREAK_NUMBER = -11;// Across the years
    public static final int NEXT_DATE = 1;// year, month, and day to add 1
    public static final int THREAD_SEND_MESSAGE = 100;// send message to handler
    public static final int UPDATE_DATE_INTERVAL = 1000;// per 1 second
    public String mYear, mMonth, mDay;

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
        final TextView popupwindow_calendar_month = (TextView)
                                         mediaView.findViewById(R.id.popupwindow_calendar_month);
        mCalendar = (CalendarView) mediaView.findViewById(R.id.popupwindow_calendar);
        final TextView popupwindow_calendar_bt_enter = (TextView)
                                               mediaView.findViewById(R.id.popupwindow_calendar_bt_enter);
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                mStr = (String)msg.obj;
                calendarTime.setText(showTime(mStr));
                calendarDate.setText(showMonth(mStr));
                popupwindow_calendar_bt_enter.setText(R.string.set_date_and_time);
                popupwindow_calendar_month.setText(mCalendar.getCalendarYear() + mYear
                                           + mCalendar.getCalendarMonth());
            }
        };
        new Thread(this).start();

        if (null != mDate) {
            int years = Integer.parseInt(mDate.substring(0, mDate.indexOf("-")));
            int month = Integer.parseInt(mDate.substring(mDate.indexOf("-") + NEXT_DATE,
                                                         mDate.lastIndexOf("-")));
            popupwindow_calendar_month.setText(years + mYear  + month);
            mCalendar.showCalendar(years, month);
            mCalendar.setCalendarDayBgColor(mDate, R.drawable.status_bar_calendar_background);
        }

        List<String> list = new ArrayList<String>();
        list.add("2014-04-01");
        list.add("2014-04-02");
        mCalendar.addMarks(list, 0);

        mCalendar.setOnCalendarClickListener(new OnCalendarClickListener() {
            public void onCalendarClick(int row, int col, String dateFormat) {
                int month = Integer.parseInt(dateFormat.substring(
                                             dateFormat.indexOf("-") + NEXT_DATE,
                                             dateFormat.lastIndexOf("-")));
                if (mCalendar.getCalendarMonth() - month == NEXT_DATE
                    || mCalendar.getCalendarMonth() - month == YEAR_BREAK_NUMBER) {
                    mCalendar.lastMonth();
                } else if (month - mCalendar.getCalendarMonth() == NEXT_DATE
                           || month - mCalendar.getCalendarMonth() == YEAR_BREAK_NUMBER) {
                    mCalendar.nextMonth();
                } else {
                    mCalendar.removeAllBgColor();
                    mCalendar.setCalendarDayBgColor(dateFormat,
                    R.drawable.status_bar_calendar_background);
                    mDate = dateFormat;
                }
            }
        });

        mCalendar.setOnCalendarDateChangedListener(new OnCalendarDateChangedListener() {
            public void onCalendarDateChanged(int year, int month) {
                popupwindow_calendar_month.setText(year + mYear + month);
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
            mCalendar.nextMonth();
            break;
        case R.id.popupwindow_calendar_last_month:
            mCalendar.lastMonth();
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
            return str.substring(TIME_CUT_THIRTEEN, TIME_CUT_EIGHTEEN);
        } else {
            return str.substring(TIME_CUT_THIRTEEN - NEXT_DATE,
                                          TIME_CUT_EIGHTEEN - NEXT_DATE);
        }
    }

    @Override
    public void run() {
        try {
            while(true) {
                mYear = mContext.getString(R.string.year);
                mMonth = mContext.getString(R.string.month);
                mDay = mContext.getString(R.string.day);
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
}
