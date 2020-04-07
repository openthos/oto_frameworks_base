package com.android.systemui.startupmenu;

import android.content.Context;
import android.util.Log;
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
import android.widget.Toast;

import com.android.systemui.R;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.OnClickCallback;
import com.android.systemui.startupmenu.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppAdapter extends BaseAdapter implements Filterable {
    private boolean mIsFirstItemLetter = true;
    private boolean mIsSearchEmpty = false;
    private Map<String, Integer> mIndexMap;
    private List<AppInfo> mDatas = new ArrayList<>();
    private int mItemLayoutId;
    private Context mContext;
    private OnClickCallback mOnClickCallback;
    private AppsFilter mAppsFilter;
    private int mDownX;
    private int mDownY;
    private View mListItem;

    public AppAdapter(Context context, List<AppInfo> datas, int itemLayoutId) {
        mDatas.addAll(datas);
        mIndexMap = new HashMap<>();
        mItemLayoutId = itemLayoutId;
        mContext = context;
        traverseList();
    }

    public void traverseList() {
        if (mDatas.isEmpty()) {
            return;
        }
        String current = mDatas.get(0).getFirstLetter();

        for ( int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).getFirstLetter() == null) {
                continue;
            }
            char tempChar = mDatas.get(i).getFirstLetter().charAt(0);
            String tempFirstLetter = mDatas.get(i).getFirstLetter();

            if (tempFirstLetter.equals(current) || (tempChar < 'A' || tempChar > 'Z')) {
                if (mIsFirstItemLetter) {
                    mIndexMap.put(current, i);
                }
            } else {
                current = mDatas.get(i).getFirstLetter();
                mIndexMap.put(current, i);
            }
            mIsFirstItemLetter = false;
        }
    }

    public void updateAppsList(List<AppInfo> mAppsData) {
        mDatas.clear();
        mDatas.addAll(mAppsData);
        Log.e("lxx--appadapter","size="+mAppsData.size()+"-----"+mDatas.size());
        traverseList();
        notifyDataSetChanged();
    }

    public int getCurrentLetterPosition(String currentLetter) {
        if (mIndexMap.get(currentLetter) != null) {
            return mIndexMap.get(currentLetter);
        } else
            return - 1;
    }

    @Override
    public int getCount() {
        return mIsSearchEmpty ? 1 : mDatas.size();
    }

    @Override
    public Object getItem( int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent) {
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
        Log.e("lxx-cc","isempty="+mDatas.isEmpty()+" mIsSearchEmpty="+mIsSearchEmpty);

        if (mIndexMap.get(mDatas.get(position).getFirstLetter()) != null
                && mIndexMap.get(mDatas .get(position).getFirstLetter()).equals(position)) {
            viewHolder.txtFirstLetter.setVisibility(View.VISIBLE);
            viewHolder.txtFirstLetter.setText( mDatas.get(position).getFirstLetter());
        } else {
            viewHolder.txtFirstLetter.setVisibility(View.GONE);
        }

        viewHolder.txtName.setText(mDatas.get(position).getLabel());
        try {
            viewHolder.icon.setImageDrawable(mContext.getPackageManager().
                    getApplicationIcon(mDatas.get(position).getPackageName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        viewHolder.appItem.setTag(mDatas.get(position));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (mAppsFilter == null) {
            mAppsFilter = new AppsFilter(mDatas);
        }
        return mAppsFilter;
    }

    public class ViewHolder implements View.OnHoverListener, View.OnTouchListener,
            View.OnLongClickListener, View.OnClickListener {

        LinearLayout appItem, search, appList;
        TextView txtFirstLetter, txtName, searchBack;
        ImageView icon;

        public ViewHolder(View convertView) {
            txtFirstLetter = convertView.findViewById(R.id.txt_letter_category);
            txtName = convertView.findViewById(R.id.txt_name);
            searchBack = convertView.findViewById(R.id.search_back);
            icon = convertView.findViewById(R.id.image);
            appItem = convertView.findViewById(R.id.app_item);
            search = convertView.findViewById(R.id.ll_search);
            appList = convertView.findViewById(R.id.ll_applist);

            appItem.setOnTouchListener(this);
            appItem.setOnHoverListener(this);
            appItem.setOnLongClickListener(this);
            appItem.setOnClickListener(this);
            search.setOnClickListener(this);
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    if (mListItem != null && mListItem != v) {
                        mListItem.setSelected(false);
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
                        //mOnClickCallback.open((AppInfo) v.getTag());
                        Log.e("lxx---","applist---mDownx="+mDownX+"  mDownY="+mDownY);
                        break;
                    case MotionEvent.BUTTON_SECONDARY:
                        mListItem = v;
                        v.setSelected(true);
                        mOnClickCallback.showDialog(mDownX, mDownY, (AppInfo) v.getTag());
                        Toast.makeText(v.getContext(), "右键菜单", Toast.LENGTH_LONG).show();
                        break;
                }
            }
            return false;
        }

        @Override
        public boolean onLongClick(View v) {
            mListItem = v;
            v.setSelected(true);
            mOnClickCallback.showDialog(mDownX, mDownY, (AppInfo) v.getTag());
            Toast.makeText(v.getContext(), "右键菜单", Toast.LENGTH_LONG).show();
            return true;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.ll_search) {
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

        private List<AppInfo> original = new ArrayList<>();

        public AppsFilter(List<AppInfo> list) {
            this.original.addAll(list);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = original;
                results.count = original.size();
                Log.e("lxx-cc1","size="+results.count+"--original="+original.size()+" str="+constraint.toString());
            } else {
                List<AppInfo> appsList = new ArrayList<>();
                Util.filtDatas(constraint.toString(), original, appsList);
                results.values = appsList;
                results.count = appsList.size();
                Log.e("lxx-cc2","size="+results.count+" --"+appsList.size()+"---original="+original.size()
                        +"  str="+constraint.toString());
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Log.e("lxx-cc3","size="+mDatas.size()+"--"+results.count);
            mDatas.clear();
            mIsSearchEmpty = results.count == 0;
            Log.e("lxx-cc4", "size=" + mDatas.size() + "--" + results.count);
            mDatas.addAll(mIsSearchEmpty ? original : (List<AppInfo>) results.values);
            traverseList();
            notifyDataSetChanged();
        }

    }

}
