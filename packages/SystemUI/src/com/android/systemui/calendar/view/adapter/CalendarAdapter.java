package com.android.systemui.calendar.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.android.systemui.R;
import com.android.systemui.calendar.view.bean.DateBean;

import java.util.ArrayList;

public class CalendarAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<DateBean> list;
    private int lastMonthDays;
    private int nextMonthDays;
    private int[] mCDate;
    private View mCurrentDayView;
    private final static String HOVER_COLOR = "#10000000";
    private final static String HOVER_AND_SELECT_COLOR = "#30000000";
    private View.OnHoverListener mOnHoverListener;
    private View.OnTouchListener mOnTouchListener;
    private View mCurrentSelectedView;

    public CalendarAdapter(final Context context, ArrayList<DateBean> list, int[] cDate) {
        this.context = context;
        this.list = list;
        this.mCDate = cDate;
        mOnHoverListener = new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        if (v.isSelected()) {
                            v.setBackgroundColor(Color.parseColor(HOVER_AND_SELECT_COLOR));
                        } else {
                            v.setBackgroundColor(Color.parseColor(HOVER_COLOR));
                        }
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        v.setBackground(context.getResources().getDrawable(R.drawable.cover));
                        break;
                }
                return false;
            }
        };
        mOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mCurrentSelectedView == null) {
                        mCurrentSelectedView = v;
                        mCurrentSelectedView.setBackground(context.getResources().getDrawable(R.drawable.cover));
                        mCurrentSelectedView.setSelected(true);
                    } else if (mCurrentSelectedView != null && mCurrentSelectedView != v) {
                        mCurrentSelectedView.setBackground(context.getResources().getDrawable(R.drawable.cover));
                        mCurrentSelectedView.setSelected(false);
                        mCurrentSelectedView = v;
                        mCurrentSelectedView.setBackground(context.getResources().getDrawable(R.drawable.cover));
                        mCurrentSelectedView.setSelected(true);
                    }
                }
                return false;
            }
        };
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_month_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            viewHolder.bakcground = convertView.findViewById(R.id.bound);
            viewHolder.cover = convertView.findViewById(R.id.cover);
            viewHolder.cover.setOnHoverListener(mOnHoverListener);
            viewHolder.cover.setOnTouchListener(mOnTouchListener);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final DateBean date = list.get(position);
        if (date.getType() == 0) {
            lastMonthDays++;
        }

        if (date.getType() == 2) {
            nextMonthDays++;
        }

        if (date.getType() == 0 || date.getType() == 2) {
            viewHolder.text.setTextColor(context.getResources().getColor(R.color.calendar_text_lighter));
        } else {
            viewHolder.text.setTextColor(context.getResources().getColor(R.color.calendar_text));
        }
        viewHolder.text.setText(String.valueOf(date.getSolar()[2]));
        if (mCDate[0] == date.getSolar()[0] && mCDate[1] == date.getSolar()[1] && mCDate[2] == date.getSolar()[2]) {
            viewHolder.bakcground.setBackground(context.getResources().getDrawable(R.drawable.date_current_bg));
            mCurrentDayView = viewHolder.bakcground;
        }
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        lastMonthDays = 0;
        nextMonthDays = 0;
        if (mCurrentDayView != null) {
            mCurrentDayView.setBackground(null);
            mCurrentDayView = null;
        }
        if (mCurrentSelectedView != null) {
            mCurrentSelectedView.setSelected(false);
            mCurrentSelectedView = null;
        }

        super.notifyDataSetChanged();
    }

    public void setData(ArrayList<DateBean> list) {
        this.list = list;
    }

    private class ViewHolder {
        TextView text;
        View bakcground;
        View cover;
    }
}
