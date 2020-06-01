package com.android.systemui.calendar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.systemui.R;
import com.android.systemui.calendar.view.adapter.CalendarAdapter;
import com.android.systemui.calendar.view.adapter.CalendarWeekAdapter;
import com.android.systemui.calendar.view.bean.DateBean;
import com.android.systemui.calendar.view.utils.CalendarUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class CalendarDialog extends com.android.systemui.dialog.BaseDialog {
//public class CalendarDialog extends Dialog {
    private Context mContext;
    private TextView mTvCurrentTime;
    private TextView mTvCurrentDate;
    private TextView mTvCurrentMeridiem;
    private TextView mTvCommitNotice;

    private TextView title;
    private SimpleDateFormat mSdfForDate;
    private SimpleDateFormat mSdfForTime;
    private SimpleDateFormat mSdfForMeridiem;
    private BroadcastReceiver mReceiver;

    private EditText mEtNoticeMessage;
    private EditText mEtNoticeTime;
    private TextView mTvNoticeMeridiem;
    private TextView mTvNoticeDate;

    private ImageView changeColor;
    private ImageView currentColor;
    private ImageView changeMeridiem;
    private int currentColorResource;

    private int[] mCDate = CalendarUtil.getCurrentDate();
    private int[] mDate = new int[]{mCDate[0], mCDate[1], mCDate[2]};
    private CalendarAdapter contentAdapter;
    private CalendarWeekAdapter titleAdapter;
    private ArrayList<DateBean> list;

    public CalendarDialog(Context context) {
        super(context);
        mContext = context;
        mContentView =
                LayoutInflater.from(mContext).inflate(R.layout.calendar_main, null);
        setContentView(mContentView);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mTvCurrentTime = findViewById(R.id.current_time);
        mTvCurrentDate = findViewById(R.id.current_date);
        mTvCurrentMeridiem = findViewById(R.id.current_meridiem);
        mEtNoticeMessage = findViewById(R.id.notice_message);
        mEtNoticeTime = findViewById(R.id.notice_time);
        mTvCommitNotice = findViewById(R.id.commit_notice);
        mTvNoticeMeridiem = findViewById(R.id.notice_meridiem);
        mTvNoticeDate = findViewById(R.id.notice_date);
        if (is24HourFormat()) {
            mSdfForTime = new SimpleDateFormat("HH:mm:ss");
        } else {
            mSdfForTime = new SimpleDateFormat("hh:mm:ss");
        }
        mSdfForDate = new SimpleDateFormat("YYYY年MM月dd日");
        mSdfForMeridiem = new SimpleDateFormat("a");
        updateMeridiem();
        updateTime();
        updateDate();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTime();
            }
        }, 0, 1000);
        findViewById(R.id.config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
        registerReceiver();
        GridView calendarTitle = findViewById(R.id.calendar_title);
        List<String> titles = Arrays.asList(mContext.getResources().getStringArray(R.array.simple_week));
        titleAdapter = new CalendarWeekAdapter(mContext, titles);
        calendarTitle.setAdapter(titleAdapter);
        GridView calendarContent = findViewById(R.id.calendar_view);
        list = CalendarUtil.getMonthDate(mDate[0], mDate[1]);
        contentAdapter = new CalendarAdapter(mContext, list, mCDate);
        calendarContent.setAdapter(contentAdapter);
        title = (TextView) findViewById(R.id.title);
        title.setText(mDate[0] + "年" + mDate[1] + "月");
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNotice();
            }
        });
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAddNotice();
            }
        });
        changeColor = findViewById(R.id.change_color);
        currentColor = findViewById(R.id.current_color);
        changeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                int x = location[0];
                int y = location[1];
                ColorDialog dialog = new ColorDialog(mContext);
                dialog.show(location);
            }
        });

        changeMeridiem = findViewById(R.id.change_meridiem);
        changeMeridiem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                int x = location[0];
                int y = location[1];
                MeridiemDialog dialog = new MeridiemDialog(mContext);
                dialog.show(location);
            }
        });

        mTvCommitNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEtNoticeTime.getText().toString();
                String[] texts = TextUtils.split(text.trim(), ":");
                try {
                    if (texts.length == 2 && Integer.valueOf(texts[0]) < 12 && Integer.valueOf(texts[1]) < 59) {
                        Log.e("wwww", "Success" + Integer.valueOf(texts[0]) + " " + Integer.valueOf(texts[1]));
                        insertNotice(Integer.valueOf(texts[0]), Integer.valueOf(texts[1]), "am", "message");
                    } else {
                        Toast.makeText(mContext, "格式不正确", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(mContext, "格式不正确", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.pre_month).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastMonth(v);
            }
        });

        findViewById(R.id.next_moonth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMonth(v);
            }
        });
    }

    private void insertNotice(Integer valueOf, Integer valueOf1, String am, String message) {
        cancelAddNotice();
    }

    private class MeridiemDialog extends Dialog {
        private TextView tvAm;
        private TextView tvPm;
        private OnClickListener onClickListener = new OnClickListener();

        public MeridiemDialog(Context context) {
            super(context, R.style.CalendarDialog);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.meridiem_dialog, null);
            tvAm = layout.findViewById(R.id.meridiem_am);
            tvPm = layout.findViewById(R.id.meridiem_pm);
            tvAm.setOnClickListener(onClickListener);
            tvPm.setOnClickListener(onClickListener);
            setContentView(layout);
        }

        public void show(int[] location) {
            changeMeridiem.setImageResource(R.mipmap.ic_pull_up);
            getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_DIALOG);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            DisplayMetrics dm = new DisplayMetrics();
            ((android.view.WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
	    int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;
            lp.x = location[0] - dip2px(mContext, 33);
            lp.y = location[1] - dip2px(mContext, 88);
            lp.dimAmount = 0.0f;
            lp.gravity = Gravity.TOP | Gravity.LEFT;
            super.show();
        }

        @Override
        public void cancel() {
            changeMeridiem.setImageResource(R.mipmap.ic_pull_down);
            super.cancel();
        }

        private class OnClickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                mTvNoticeMeridiem.setText(((TextView) v).getText());
                cancel();
            }
        }
    }

    private class ColorDialog extends Dialog {
        private ImageView tagRed, tagPurple, tagYellow, tagGreen, tagBlue;
        private List<ImageView> tagList = new ArrayList<>();
        private View currentView;

        public ColorDialog(Context context) {
            super(context, R.style.CalendarDialog);
        }

        private ColorClickListener colorClickListener = new ColorClickListener();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.color_dialog, null);
            tagRed = layout.findViewById(R.id.tag_red);
            tagRed.setTag(R.drawable.red);
            tagPurple = layout.findViewById(R.id.tag_purple);
            tagPurple.setTag(R.drawable.purple);
            tagYellow = layout.findViewById(R.id.tag_yellow);
            tagYellow.setTag(R.drawable.yellow);
            tagGreen = layout.findViewById(R.id.tag_green);
            tagGreen.setTag(R.drawable.green);
            tagBlue = layout.findViewById(R.id.tag_blue);
            tagBlue.setTag(R.drawable.blue);
            tagRed.setOnClickListener(colorClickListener);
            tagPurple.setOnClickListener(colorClickListener);
            tagYellow.setOnClickListener(colorClickListener);
            tagGreen.setOnClickListener(colorClickListener);
            tagBlue.setOnClickListener(colorClickListener);
            switch (currentColorResource) {
                case R.drawable.red:
                    currentView = tagRed;
                    break;
                case R.drawable.yellow:
                    currentView = tagYellow;
                    break;
                case R.drawable.blue:
                    currentView = tagBlue;
                    break;
                case R.drawable.purple:
                    currentView = tagPurple;
                    break;
                case R.drawable.green:
                    currentView = tagGreen;
                    break;
                default:
                    currentView = tagRed;
            }
            currentView.setSelected(true);
            setContentView(layout);
        }

        public void show(int[] location) {
            changeColor.setImageResource(R.mipmap.ic_pull_up);
            getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_DIALOG);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.dimAmount = 0.0f;
            lp.gravity = Gravity.TOP | Gravity.LEFT;
            DisplayMetrics dm = new DisplayMetrics();
            ((android.view.WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
	    int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;
            lp.x = location[0] - dip2px(mContext, 33);
            lp.y = location[1] - dip2px(mContext, 213);
            super.show();
        }

        @Override
        public void cancel() {
            changeColor.setImageResource(R.mipmap.ic_pull_down);
            super.cancel();
        }

        private class ColorClickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                currentView.setSelected(false);
                currentView = v;
                currentColorResource = (Integer) currentView.getTag();
                currentColor.setImageResource(currentColorResource);
                currentView.setSelected(true);
                cancel();
            }
        }
    }

    private void addNotice() {
        currentColorResource = R.drawable.red;
        currentColor.setImageResource(currentColorResource);
        mEtNoticeMessage.setText("");
        mEtNoticeTime.setText("");
        mTvNoticeMeridiem.setText("AM");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY/MM/dd");
        mTvNoticeDate.setText(mCDate[0] + "/" + mCDate[1] + "/" + mCDate[2]);


        ValueAnimator va = ValueAnimator.ofInt((int) mContext.getResources().getDimension(R.dimen.standard_height), (int) mContext.getResources().getDimension(R.dimen.higher_height));
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int h = (int) valueAnimator.getAnimatedValue();
                findViewById(R.id.content_calendar).getLayoutParams().height = h;
                findViewById(R.id.content_calendar).requestLayout();
            }
        });
        va.setDuration(200);
        va.start();
        final View v1 = findViewById(R.id.notice_list);
        final View v2 = findViewById(R.id.notice_add);
        v2.setAlpha(0f);
        v2.setVisibility(View.VISIBLE);
        v2.animate().alpha(1f).setDuration(200);
        v1.animate().alpha(0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v1.setVisibility(View.GONE);
                v2.setVisibility(View.VISIBLE);
            }
        });
    }

    private void cancelAddNotice() {
        ValueAnimator va = ValueAnimator.ofInt((int) mContext.getResources().getDimension(R.dimen.higher_height), (int) mContext.getResources().getDimension(R.dimen.standard_height));

        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int h = (int) valueAnimator.getAnimatedValue();
                findViewById(R.id.content_calendar).getLayoutParams().height = h;
                findViewById(R.id.content_calendar).requestLayout();
            }
        });
        va.setDuration(200);
        va.start();

        final View v1 = findViewById(R.id.notice_add);
        final View v2 = findViewById(R.id.notice_list);
        v2.setAlpha(0f);
        v2.setVisibility(View.VISIBLE);
        v2.animate().alpha(1f).setDuration(200);
        v1.animate().alpha(0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v1.setVisibility(View.GONE);
                v2.setVisibility(View.VISIBLE);
            }
        });
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void registerReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (Intent.ACTION_TIME_TICK.equals(action)
                        || Intent.ACTION_TIME_CHANGED.equals(action)
                        || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                        || Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                    if (is24HourFormat()) {
                        mSdfForTime = new SimpleDateFormat("HH:mm:ss");
                    } else {
                        mSdfForTime = new SimpleDateFormat("hh:mm:ss");
                    }
                    updateMeridiem();
                    updateTime();
                    updateDate();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        HandlerThread thread = new HandlerThread("TimeTick");
        thread.start();
        mContext.registerReceiver(mReceiver, filter, null, new Handler(thread.getLooper()));
        TextView currentMeridiem = findViewById(R.id.current_meridiem);
        currentMeridiem.setText("AM");
    }


    @Override
    public void cancel() {
        mContext.unregisterReceiver(mReceiver);
        super.cancel();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public void lastMonth(View view) {
        mDate[1] -= 1;
        if (mDate[1] == 0) {
            mDate[1] += 12;
            mDate[0] -= 1;
        }
        updateCalendarView();
    }

    public void nextMonth(View view) {
        mDate[1] += 1;
        if (mDate[1] == 13) {
            mDate[1] -= 12;
            mDate[0] += 1;
        }
        updateCalendarView();
    }

    private void updateCalendarView() {
        list = CalendarUtil.getMonthDate(mDate[0], mDate[1]);
        contentAdapter.setData(list);
        contentAdapter.notifyDataSetChanged();
        title.setText(mDate[0] + "年" + mDate[1] + "月");
    }

    public boolean is24HourFormat() {
        String value = Settings.System.getString(mContext.getContentResolver(), Settings.System.TIME_12_24);
        if (value == null) {
            Locale locale = mContext.getResources().getConfiguration().locale;
            DateFormat natural = DateFormat.getTimeInstance(DateFormat.LONG, locale);
            if (natural instanceof SimpleDateFormat) {
                SimpleDateFormat sdf = (SimpleDateFormat) natural;
                String pattern = sdf.toPattern();
                if (pattern.indexOf('H') >= 0) {
                    value = "24";
                } else {
                    value = "12";
                }
            } else {
                value = "12";
            }
        }
        return value.equals("24");
    }

    private void updateDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        final String week = mContext.getResources().getStringArray(R.array.week)[calendar.get(Calendar.DAY_OF_WEEK) - 1];
        mTvCurrentTime.post(new Runnable() {
            @Override
            public void run() {
                mTvCurrentDate.setText(mSdfForDate.format(new Date()) + " " + week);
            }
        });


    }

    private void updateTime() {
        mTvCurrentTime.post(new Runnable() {
            @Override
            public void run() {
                mTvCurrentTime.setText(mSdfForTime.format(new Date()));
                mTvCurrentMeridiem.setText(mSdfForMeridiem.format(new Date()));
            }
        });
    }

    private void updateMeridiem() {
        mTvCurrentMeridiem.post(new Runnable() {
            @Override
            public void run() {
                if (is24HourFormat()) {
                    mTvCurrentMeridiem.setVisibility(View.GONE);
                } else {
                    mTvCurrentMeridiem.setVisibility(View.VISIBLE);
                }
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        mContext.getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
}
