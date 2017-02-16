package com.android.systemui.statusbar.notificationbars;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.systemui.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

@SuppressWarnings("deprecation")
public class CalendarView extends ViewFlipper implements
                                        android.view.GestureDetector.OnGestureListener {
    public static final int COLOR_BG_WEEK_TITLE = Color.parseColor("#ffeeeeee");
    public static final int COLOR_TX_WEEK_TITLE = Color.parseColor("#3f2d76");
    public static final int COLOR_TX_THIS_MONTH_DAY = Color.parseColor("#aa564b4b");
    public static final int COLOR_TX_OTHER_MONTH_DAY = Color.parseColor("#ffcccccc");
    public static final int COLOR_TX_THIS_DAY = Color.parseColor("#ff008000");
    public static final int COLOR_BG_THIS_DAY = Color.parseColor("#ffcccccc");
    public static final int COLOR_BG_CALENDAR = Color.parseColor("#ffeeeeee");
    public static final int COLOR_BK_TRANSPARENT = Color.parseColor("#2b1f52");
    public static final int COLOR_BK_DAGE_GRAY = Color.parseColor("#786C9f");
    public static final int COLOR_BK_DAGE_WHITE = Color.parseColor("#FFFFFF");
    public static final int COLOR_TX_WEEK_RED = Color.parseColor("#ffcc3333");

    private GestureDetector mGD;
    private Animation mPushLeftIn;
    private Animation mPushLeftOut;
    private Animation mPushRightIn;
    private Animation mPushRightOut;

    private int ROWS_TOTAL = 6;
    private int COLS_TOTAL = 7;
    private String[][] mDates = new String[6][7];
    private float mTB;

    private OnCalendarClickListener mOnCalendarClickListener;
    private OnCalendarDateChangedListener mOnCalendarDateChangedListener;
    private int mCalendarYear;
    private int mCalendarMonth;
    private Date mThisday = new Date();
    private Date mCalendarday;

    private LinearLayout mFirstCalendar;
    private LinearLayout mSecondCalendar;
    private LinearLayout mCurrentCalendar;

    private Map<String, Integer> mMarksMap = new HashMap<String, Integer>();
    private Map<String, Integer> mDayBgColorMap = new HashMap<String, Integer>();

    private int TEXT_SIZE=12;

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public CalendarView(Context context) {
        super(context);
        init();
    }

    private void init() {
        //setBackgroundColor(COLOR_BG_CALENDAR);
        setBackgroundColor(COLOR_TX_WEEK_TITLE);
        mGD = new GestureDetector(this);
        mPushLeftIn = AnimationUtils.loadAnimation(getContext(),
                                                   R.anim.push_left_in);
        mPushLeftOut = AnimationUtils.loadAnimation(getContext(),
                                                    R.anim.push_left_out);
        mPushRightIn = AnimationUtils.loadAnimation(getContext(),
                                                    R.anim.push_right_in);
        mPushRightOut = AnimationUtils.loadAnimation(getContext(),
                                                     R.anim.push_right_out);
        mPushLeftIn.setDuration(400);
        mPushLeftOut.setDuration(400);
        mPushRightIn.setDuration(400);
        mPushRightOut.setDuration(400);
        mFirstCalendar = new LinearLayout(getContext());
        mFirstCalendar.setOrientation(LinearLayout.VERTICAL);
        mFirstCalendar.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        mSecondCalendar = new LinearLayout(getContext());
        mSecondCalendar.setOrientation(LinearLayout.VERTICAL);
        mSecondCalendar.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        mCurrentCalendar = mFirstCalendar;
        addView(mFirstCalendar);
        addView(mSecondCalendar);
        drawFrame(mFirstCalendar);
        drawFrame(mSecondCalendar);
        mCalendarYear = mThisday.getYear() + 1900;
        mCalendarMonth = mThisday.getMonth();
        mCalendarday = new Date(mCalendarYear - 1900, mCalendarMonth, 1);
        setCalendarDate();
    }

    private void drawFrame(LinearLayout oneCalendar) {
        String[] weekday = new String[] { "日", "一", "二", "三", "四", "五", "六" };
        LinearLayout title = new LinearLayout(getContext());
        title.setBackgroundColor(COLOR_TX_WEEK_TITLE);
        title.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(-1,  0, 1);
        Resources res = getResources();
        mTB = res.getDimension(R.dimen.linear_length_unit_over_the_weekend_padding);
        title.setLayoutParams(layout);
        oneCalendar.addView(title);

        for (int i = 0; i < COLS_TOTAL; i++) {
            TextView view = new TextView(getContext());
            view.setGravity(Gravity.CENTER);
            view.setText(weekday[i]);
            view.setTextColor(COLOR_BK_DAGE_GRAY);
            view.setTextSize(TEXT_SIZE);
            view.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1));
            title.addView(view);
        }

        LinearLayout content = new LinearLayout(getContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 7f));
        oneCalendar.addView(content);

        for (int i = 0; i < ROWS_TOTAL; i++) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, 0, 1));
            content.addView(row);
            for (int j = 0; j < COLS_TOTAL; j++) {
                RelativeLayout col = new RelativeLayout(getContext());
                col.setLayoutParams(new LinearLayout.LayoutParams(0,
                LayoutParams.MATCH_PARENT, 1));
                //col.setBackgroundResource(R.drawable.statusbar_sound);
                row.addView(col);
                col.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewGroup parent = (ViewGroup) v.getParent();
                        int row = 0, col = 0;
                        for (int i = 0; i < parent.getChildCount(); i++) {
                            if (v.equals(parent.getChildAt(i))) {
                                col = i;
                                break;
                            }
                        }
                        ViewGroup pparent = (ViewGroup) parent.getParent();
                        for (int i = 0; i < pparent.getChildCount(); i++) {
                            if (parent.equals(pparent.getChildAt(i))) {
                                row = i;
                                break;
                            }
                        }
                        if (mOnCalendarClickListener != null) {
                            mOnCalendarClickListener.onCalendarClick(row, col,
                                                                     mDates[row][col]);
                        }
                    }
                });
            }
        }
    }

    private void setCalendarDate() {
        int weekday = mCalendarday.getDay();
        int firstDay = 1;
        int day = firstDay;
        int lastDay = getDateNum(mCalendarday.getYear(), mCalendarday.getMonth());
        int nextMonthDay = 1;
        int lastMonthDay = 1;
        for (int i = 0; i < ROWS_TOTAL; i++) {
            for (int j = 0; j < COLS_TOTAL; j++) {
                if (i == 0 && j == 0 && weekday != 0) {
                    int year = 0;
                    int month = 0;
                    int lastMonthDays = 0;
                    if (mCalendarday.getMonth() == 0) {
                        year = mCalendarday.getYear() - 1;
                        month = Calendar.DECEMBER;
                    } else {
                        year = mCalendarday.getYear();
                        month = mCalendarday.getMonth() - 1;
                    }
                lastMonthDays = getDateNum(year, month);
                int firstShowDay = lastMonthDays - weekday + 1;
                for (int k = 0; k < weekday; k++) {
                    lastMonthDay = firstShowDay + k;
                    RelativeLayout group = getDateView(0, k);
                    group.setGravity(Gravity.CENTER);
                    TextView view = null;
                    if (group.getChildCount() > 0) {
                        view = (TextView) group.getChildAt(0);
                    } else {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        -1, -1);
                        view = new TextView(getContext());
                        view.setLayoutParams(params);
                        view.setGravity(Gravity.CENTER);
                        group.addView(view);
                    }
                    view.setText(Integer.toString(lastMonthDay));
                    view.setTextColor(COLOR_BK_DAGE_GRAY);
                    view.setTextSize(TEXT_SIZE);
                    mDates[0][k] = format(new Date(year, month, lastMonthDay));
                    if (mDayBgColorMap.get(mDates[0][k]) != null) {
                    } else {
                        view.setBackgroundColor(Color.TRANSPARENT);
                    }
                    setMarker(group, 0, k);
                }
                j = weekday - 1;
            } else {
                RelativeLayout group = getDateView(i, j);
                group.setGravity(Gravity.CENTER);
                TextView view = null;
                if (group.getChildCount() > 0) {
                    view = (TextView) group.getChildAt(0);
                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    -1, -1);
                    view = new TextView(getContext());
                    view.setLayoutParams(params);
                    view.setGravity(Gravity.CENTER);
                    view.setTextSize(TEXT_SIZE);
                    group.addView(view);
                }
                if (day <= lastDay) {
                    mDates[i][j] = format(new Date(mCalendarday.getYear(),
                    mCalendarday.getMonth(), day));
                    view.setText(Integer.toString(day));
                    if (mThisday.getDate() == day
                        && mThisday.getMonth() == mCalendarday.getMonth()
                        && mThisday.getYear() == mCalendarday.getYear()) {
                        view.setBackgroundColor(COLOR_BK_TRANSPARENT);
                    } else {
                         //COLOR_BK_DAGE_GRAY
                        //view.setTextColor(COLOR_TX_THIS_MONTH_DAY);
                        view.setTextColor(COLOR_BK_DAGE_WHITE);
                        view.setBackgroundColor(Color.TRANSPARENT);
                    }
                    if (mDayBgColorMap.get(mDates[i][j]) != null) {
                        //view.setTextColor(Color.WHITE);
                        view.setTextColor(COLOR_BK_DAGE_GRAY);
                        view.setBackgroundResource(mDayBgColorMap.get(mDates[i][j]));
                    }
                    setMarker(group, i, j);
                    day++;
                } else {
                    if (mCalendarday.getMonth() == Calendar.DECEMBER) {
                        mDates[i][j] = format(new Date(
                        mCalendarday.getYear() + 1,
                        Calendar.JANUARY, nextMonthDay));
                    } else {
                        mDates[i][j] = format(new Date(
                        mCalendarday.getYear(),
                        mCalendarday.getMonth() + 1, nextMonthDay));
                    }
                    view.setText(Integer.toString(nextMonthDay));
                    //view.setTextColor(COLOR_TX_OTHER_MONTH_DAY);
                    view.setTextColor(COLOR_BK_DAGE_GRAY);
                    if (mDayBgColorMap.get(mDates[i][j]) != null) {
                    } else {
                        view.setBackgroundColor(Color.TRANSPARENT);
                        //view.setBackgroundColor(COLOR_BK_DAGE_GRAY);
                    }
                    setMarker(group, i, j);
                    nextMonthDay++;
                    }
                }
            }
        }
    }

    public interface OnCalendarClickListener {
        void onCalendarClick(int row, int col, String dateFormat);
    }

    public interface OnCalendarDateChangedListener {
        void onCalendarDateChanged(int year, int month);
    }

    public void showCalendar(int year, int month) {
        mCalendarYear = year;
        mCalendarMonth = month - 1;
        mCalendarday = new Date(mCalendarYear - 1900, mCalendarMonth, 1);
        setCalendarDate();
    }

    public void showCalendar() {
        Date now = new Date();
        mCalendarYear = now.getYear() + 1900;
        mCalendarMonth = now.getMonth();
        mCalendarday = new Date(mCalendarYear - 1900, mCalendarMonth, 1);
        setCalendarDate();
    }

    public synchronized void nextMonth() {
        if (mCurrentCalendar == mFirstCalendar) {
            mCurrentCalendar = mSecondCalendar;
        } else {
            mCurrentCalendar = mFirstCalendar;
        }
        setInAnimation(mPushLeftIn);
        setOutAnimation(mPushLeftOut);
        if (mCalendarMonth == Calendar.DECEMBER) {
            mCalendarYear++;
            mCalendarMonth = Calendar.JANUARY;
        } else {
            mCalendarMonth++;
        }
        mCalendarday = new Date(mCalendarYear - 1900, mCalendarMonth, 1);
        setCalendarDate();
        showNext();
        if (mOnCalendarDateChangedListener != null) {
            mOnCalendarDateChangedListener.onCalendarDateChanged(mCalendarYear,
                                                                 mCalendarMonth + 1);
        }
    }

    public synchronized void lastMonth() {
        if (mCurrentCalendar == mFirstCalendar) {
            mCurrentCalendar = mSecondCalendar;
        } else {
            mCurrentCalendar = mFirstCalendar;
        }
        setInAnimation(mPushRightIn);
        setOutAnimation(mPushRightOut);
        if (mCalendarMonth == Calendar.JANUARY) {
            mCalendarYear--;
            mCalendarMonth = Calendar.DECEMBER;
        } else {
            mCalendarMonth--;
        }
        mCalendarday = new Date(mCalendarYear - 1900, mCalendarMonth, 1);
        setCalendarDate();
        showPrevious();
        if (mOnCalendarDateChangedListener != null) {
            mOnCalendarDateChangedListener.onCalendarDateChanged(mCalendarYear,
                                                                 mCalendarMonth + 1);
        }
    }

    public int getCalendarYear() {
        return mCalendarday.getYear() + 1900;
    }

    public int getCalendarMonth() {
        return mCalendarday.getMonth() + 1;
    }

    public void addMark(Date date, int id) {
        addMark(format(date), id);
    }

    void addMark(String date, int id) {
        mMarksMap.put(date, id);
        setCalendarDate();
    }

    public void addMarks(Date[] date, int id) {
        for (int i = 0; i < date.length; i++) {
            mMarksMap.put(format(date[i]), id);
        }
        setCalendarDate();
    }

    public void addMarks(List<String> date, int id) {
        for (int i = 0; i < date.size(); i++) {
            mMarksMap.put(date.get(i), id);
        }
        setCalendarDate();
    }

    public void removeMark(Date date) {
        removeMark(format(date));
    }

    public void removeMark(String date) {
        mMarksMap.remove(date);
        setCalendarDate();
    }

    public void removeAllMarks() {
        mMarksMap.clear();
        setCalendarDate();
    }

    public void setCalendarDayBgColor(Date date, int color) {
        setCalendarDayBgColor(format(date), color);
    }

    void setCalendarDayBgColor(String date, int color) {
        mDayBgColorMap.put(date, color);
        setCalendarDate();
    }

    public void setCalendarDaysBgColor(List<String> date, int color) {
        for (int i = 0; i < date.size(); i++) {
            mDayBgColorMap.put(date.get(i), color);
        }
        setCalendarDate();
    }

    public void setCalendarDayBgColor(String[] date, int color) {
        for (int i = 0; i < date.length; i++) {
            mDayBgColorMap.put(date[i], color);
        }
        setCalendarDate();
    }

    public void removeCalendarDayBgColor(Date date) {
        removeCalendarDayBgColor(format(date));
    }

    public void removeCalendarDayBgColor(String date) {
        mDayBgColorMap.remove(date);
        setCalendarDate();
    }

    public void removeAllBgColor() {
        mDayBgColorMap.clear();
        setCalendarDate();
    }

    public String getDate(int row, int col) {
        return mDates[row][col];
    }

    public boolean hasMarked(String date) {
        return mMarksMap.get(date) == null ? false : true;
    }

    public void clearAll() {
        mMarksMap.clear();
        mDayBgColorMap.clear();
    }

    private void setMarker(RelativeLayout group, int i, int j) {
        int childCount = group.getChildCount();
        if (mMarksMap.get(mDates[i][j]) != null) {
            if (childCount < 2) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (int) (mTB * 0.7), (int) (mTB * 0.7));
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.setMargins(0, 0, 1, 1);
                ImageView markView = new ImageView(getContext());
                markView.setImageResource(mMarksMap.get(mDates[i][j]));
                markView.setLayoutParams(params);
                //markView.setBackgroundResource(R.drawable.statusbar_sound);
                group.addView(markView);
            }
        } else {
            if (childCount > 1) {
                group.removeView(group.getChildAt(1));
            }
        }
    }

    private int getDateNum(int year, int month) {
        Calendar time = Calendar.getInstance();
        time.clear();
        time.set(Calendar.YEAR, year + 1900);
        time.set(Calendar.MONTH, month);
        return time.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private RelativeLayout getDateView(int row, int col) {
        return (RelativeLayout) ((LinearLayout) ((LinearLayout) mCurrentCalendar
                                                  .getChildAt(1)).getChildAt(row)).getChildAt(col);
    }

    private String format(Date d) {
        return addZero(d.getYear() + 1900, 4) + "-"
                       + addZero(d.getMonth() + 1, 2) + "-" + addZero(d.getDate(), 2);
    }

    private static String addZero(int i, int count) {
        if (count == 2) {
            if (i < 10) {
                return "0" + i;
            }
        } else if (count == 4) {
            if (i < 10) {
                return "000" + i;
            } else if (i < 100 && i > 10) {
                return "00" + i;
            } else if (i < 1000 && i > 100) {
                return "0" + i;
            }
        }
        return "" + i;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mGD != null) {
            if (mGD.onTouchEvent(ev))
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return mGD.onTouchEvent(event);
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public void onShowPress(MotionEvent e) {}

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > 20) {
            nextMonth();
        } else if (e1.getX() - e2.getX() < -20) {
            lastMonth();
        }
        return false;
    }

    public OnCalendarClickListener getOnCalendarClickListener() {
        return mOnCalendarClickListener;
    }

    public void setOnCalendarClickListener(OnCalendarClickListener listener) {
            mOnCalendarClickListener = listener;
    }

    public OnCalendarDateChangedListener getOnCalendarDateChangedListener() {
        return mOnCalendarDateChangedListener;
    }

    public void setOnCalendarDateChangedListener(
                    OnCalendarDateChangedListener listener) {
        mOnCalendarDateChangedListener = listener;
    }

    public Date getThisday() {
        return mThisday;
    }

    public void setThisday(Date thisday) {
        mThisday = thisday;
    }

    public Map<String, Integer> getDayBgColorMap() {
        return mDayBgColorMap;
    }

    public void setDayBgColorMap(Map<String, Integer> map) {
        mDayBgColorMap = map;
    }
}
