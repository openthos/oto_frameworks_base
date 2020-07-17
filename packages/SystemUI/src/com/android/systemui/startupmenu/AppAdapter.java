package com.android.systemui.startupmenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.OnClickCallback;
import com.android.systemui.startupmenu.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppAdapter extends BaseAdapter implements Filterable {
    private boolean mIsSearchEmpty = false;
    private Map<String, Integer> mIndexMap;
    private List<AppInfo> mDatas;
    private int mItemLayoutId;
    private Context mContext;
    private OnClickCallback mOnClickCallback;
    private AppsFilter mAppsFilter;
    private int mDownX;
    private int mDownY;
    private View mSelectedListItem;

    public AppAdapter(Context context, List<AppInfo> datas, int itemLayoutId) {
        mDatas = datas;
        mIndexMap = new HashMap<>();
        mItemLayoutId = itemLayoutId;
        mContext = context;
        setInitialLetterIndexMap();
    }

    private void setInitialLetterIndexMap() {
        boolean mIsInitialItemLetter = true;
        if (mDatas.isEmpty()) {
            return;
        }
        String current = mDatas.get(0).getInitialLetter();

        for ( int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).getInitialLetter() == null) {
                continue;
            }
            char tempChar = mDatas.get(i).getInitialLetter().charAt(0);
            String tempInitialLetter = mDatas.get(i).getInitialLetter();

            if (tempInitialLetter.equals(current) || (tempChar < 'A' || tempChar > 'Z')) {
                if (mIsInitialItemLetter) {
                    mIndexMap.put(current, i);
                }
            } else {
                current = mDatas.get(i).getInitialLetter();
                mIndexMap.put(current, i);
            }
            mIsInitialItemLetter = false;
        }
    }

    public void updateAppsList() {
        setInitialLetterIndexMap();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mIsSearchEmpty ? 1 : mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mItemLayoutId, null);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        viewHolder.search.setVisibility(mIsSearchEmpty ? View.VISIBLE : View.GONE);
        viewHolder.appList.setVisibility(mIsSearchEmpty ? View.GONE : View.VISIBLE);

        if (mIndexMap.get(mDatas.get(position).getInitialLetter()) != null
                && mIndexMap.get(mDatas.get(position).getInitialLetter()).equals(position)) {
            viewHolder.initialLetter.setVisibility(View.VISIBLE);
            viewHolder.initialLetter.setText(mDatas.get(position).getInitialLetter());
        } else {
            viewHolder.initialLetter.setVisibility(View.GONE);
        }

        viewHolder.appLabel.setText(mDatas.get(position).getLabel());
        try {
            viewHolder.icon.setImageDrawable(mContext.getPackageManager().
                    getApplicationIcon(mDatas.get(position).getPackageName()));
        } catch (Exception e) {
            viewHolder.icon.setImageResource(R.drawable.ic_launcher);
            e.printStackTrace();
        }
        viewHolder.appItem.setTag(mDatas.get(position));

        return convertView;
    }

    @Override
    public synchronized Filter getFilter() {
        if (mAppsFilter == null) {
            mAppsFilter = new AppsFilter(mDatas);
        }
        return mAppsFilter;
    }

    public class ViewHolder implements View.OnHoverListener, View.OnTouchListener,
            View.OnLongClickListener, View.OnClickListener {

        LinearLayout appItem, search, appList;
        TextView initialLetter, appLabel, searchBack;
        ImageView icon;

        public ViewHolder(View convertView) {
            initialLetter = convertView.findViewById(R.id.txt_letter_category);
            appLabel = convertView.findViewById(R.id.txt_name);
            searchBack = convertView.findViewById(R.id.search_back);
            icon = convertView.findViewById(R.id.image);
            appItem = convertView.findViewById(R.id.app_item);
            search = convertView.findViewById(R.id.ll_search);
            appList = convertView.findViewById(R.id.ll_applist);

            appItem.setOnTouchListener(this);
            appItem.setOnHoverListener(this);
            appItem.setOnLongClickListener(this);
            searchBack.setOnTouchListener(this);
            appItem.setOnClickListener(this);
            searchBack.setOnClickListener(this);
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    if (mSelectedListItem != null && mSelectedListItem != v) {
                        mSelectedListItem.setSelected(false);
                    }
                    v.setSelected(true);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setSelected(false);
                    break;
            }
            return false;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mOnClickCallback == null) {
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mDownX = (int) event.getRawX();
                mDownY = (int) event.getRawY();
                switch (event.getButtonState()) {
                    case MotionEvent.BUTTON_PRIMARY:
                        //clickEvent(v);
                        break;
                    case MotionEvent.BUTTON_SECONDARY:
                        mSelectedListItem = v;
                        v.setSelected(true);
                        mOnClickCallback.showDialog(mDownX, mDownY, (AppInfo) v.getTag());
                        break;
                }
            }
            return false;
        }

        @Override
        public boolean onLongClick(View v) {
            mSelectedListItem = v;
            v.setSelected(true);
            mOnClickCallback.showDialog(mDownX, mDownY, (AppInfo) v.getTag());
            return true;
        }

        @Override
        public void onClick(View v) {
            clickEvent(v);
        }

        private void clickEvent(View v) {
            if (v.getId() == R.id.search_back) {
                mOnClickCallback.updateSearchState();
            } else {
                mOnClickCallback.open((AppInfo) v.getTag());
            }
        }
    }

    public void setOnClickCallback(OnClickCallback callback) {
        mOnClickCallback = callback;
    }

    private class AppsFilter extends android.widget.Filter {

        private List<AppInfo> original;

        public AppsFilter(List<AppInfo> list) {
            original = list;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = original;
                results.count = original.size();
            } else {
                List<AppInfo> appsList = new ArrayList<>();
                Util.filtDatas(constraint.toString(), original, appsList);
                results.values = appsList;
                results.count = appsList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mIsSearchEmpty = results.count == 0;
            mDatas = mIsSearchEmpty ? original : (List<AppInfo>) results.values;
            setInitialLetterIndexMap();
            notifyDataSetChanged();
        }

    }

}
