package com.android.systemui.startupmenu;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.systemui.R;

/**
 * Created by ljh on 17-9-19.
 */

public class SortSelectPopupWindow extends PopupWindow implements View.OnClickListener {
    private Context mContext;
    private TextView mDefaultSort;
    private TextView mNameSort;
    private TextView mTimeSort;
    private TextView mClickSort;

    private SortSelectListener mSortSelectListener;

    public SortSelectPopupWindow(Context context) {
        super(context);
        mContext = context;
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        initView();
        initListener();
    }

    private void initView() {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.sort_pop_layout, null, false);
        mDefaultSort = (TextView) inflate.findViewById(R.id.default_sort);
        mClickSort = (TextView) inflate.findViewById(R.id.click_sort);
        mTimeSort = (TextView) inflate.findViewById(R.id.time_sort);
        mNameSort = (TextView) inflate.findViewById(R.id.name_sort);
        setContentView(inflate);
    }

    public void setSortSelectListener(SortSelectListener sortSelectListener){
        mSortSelectListener = sortSelectListener;
    }

    private void initListener() {
        mDefaultSort.setOnHoverListener(mHoverListener);
        mClickSort.setOnHoverListener(mHoverListener);
        mTimeSort.setOnHoverListener(mHoverListener);
        mNameSort.setOnHoverListener(mHoverListener);

        mDefaultSort.setOnClickListener(this);
        mClickSort.setOnClickListener(this);
        mTimeSort.setOnClickListener(this);
        mNameSort.setOnClickListener(this);
    }

    View.OnHoverListener mHoverListener = new View.OnHoverListener() {
        public boolean onHover(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundResource(R.color.common_hover_bg);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundResource(R.color.grid_unhover_bg);
                    break;
            }
            return false;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.default_sort:
                mSortSelectListener.defaultSort(v);
                break;
            case R.id.click_sort:
                mSortSelectListener.clickSort(v);
                break;
            case R.id.time_sort:
                mSortSelectListener.timeSort(v);
                break;
            case R.id.name_sort:
                mSortSelectListener.nameSort(v);
                break;
        }
        dismiss();
    }

    public interface SortSelectListener{
        void defaultSort(View v);
        void clickSort(View v);
        void timeSort(View v);
        void nameSort(View v);
    }
}
