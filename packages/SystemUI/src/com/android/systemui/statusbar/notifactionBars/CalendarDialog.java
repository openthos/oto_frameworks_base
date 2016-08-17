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
    public static final int WEEK_CUT_BEGIN = 22;
    public static final int WEEK_CUT_END = 24;
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
        TextView calendarDate = (TextView) mediaView.findViewById(R.id.calendar_date);
        final TextView popupwindow_calendar_month = (TextView)
                                         mediaView.findViewById(R.id.popupwindow_calendar_month);
        mCalendar = (CalendarView) mediaView.findViewById(R.id.popupwindow_calendar);
        TextView popupwindow_calendar_bt_enter = (TextView)
                                               mediaView.findViewById(R.id.popupwindow_calendar_bt_enter);
        popupwindow_calendar_month.setText(mCalendar.getCalendarYear() + "年"
                                           + mCalendar.getCalendarMonth() + "月");
        SimpleDateFormat formatter = new SimpleDateFormat
                                           ("yyyy年MM月dd日  HH:mm:s EEEE" , Locale.getDefault());
        mStr = formatter.format(new Date());
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                calendarTime.setText((String)msg.obj);
            }
        };
        new Thread(this).start();
        calendarDate.setText(showMonth(mStr));

        if (null != mDate) {
            int years = Integer.parseInt(mDate.substring(0, mDate.indexOf("-")));
            int month = Integer.parseInt(mDate.substring(mDate.indexOf("-") + 1,
                                                         mDate.lastIndexOf("-")));
            popupwindow_calendar_month.setText(years + "年" + month +"月");
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
                                             dateFormat.indexOf("-") + 1,
                                             dateFormat.lastIndexOf("-")));
                if (mCalendar.getCalendarMonth() - month == 1
                    || mCalendar.getCalendarMonth() - month == -11) {
                    mCalendar.lastMonth();
                } else if (month - mCalendar.getCalendarMonth() == 1
                           || month - mCalendar.getCalendarMonth() == -11) {
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
                popupwindow_calendar_month.setText(year + "年" + month +"月");
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
        String hh = str.substring(8, 10);
        String mm = str.substring(10, 12);
        return hh + ":" + mm;
    }

    @Override
    public void run() {
        try {
            while(true) {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                String str = formatter.format(new Date());
                mHandler.sendMessage(mHandler.obtainMessage(100, str));
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
