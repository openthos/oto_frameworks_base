package com.android.systemui.startupmenu;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.systemui.R;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.OnRecentClickCallback;

import java.util.ArrayList;
import java.util.List;

public class AppRecentAdapter extends BaseAdapter {
    private Context mContext;
    private List<AppInfo> mAppsUseCountData = new ArrayList<>();
    private int mLayoutId;
    private int mDownX;
    private int mDownY;
    private OnRecentClickCallback mRecentOnClickCallback;

    public AppRecentAdapter(Context context, List<AppInfo> appsUseCountData, int layoutId) {
        mContext = context;
        mAppsUseCountData.addAll(appsUseCountData);
        mLayoutId = layoutId;
    }

    @Override
    public int getCount() {
        return mAppsUseCountData.size() < 11 ? mAppsUseCountData.size() : 10;
    }

    @Override
    public Object getItem(int position) {
        return mAppsUseCountData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mLayoutId, null);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        try {
            viewHolder.icon.setImageDrawable(mContext.getPackageManager().
                    getApplicationIcon(mAppsUseCountData.get(position).getPackageName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        viewHolder.recentApp.setTag(mAppsUseCountData.get(position));

        return convertView;
    }

    public void updateRecentAppsList(List<AppInfo> appsUseCountData) {
        if (!appsUseCountData.isEmpty()) {
            mAppsUseCountData.clear();
            mAppsUseCountData.addAll(appsUseCountData);
            notifyDataSetChanged();
        }
    }

    public void setRecentOnClickCallback(OnRecentClickCallback callback) {
        mRecentOnClickCallback = callback;
    }

    public class ViewHolder implements View.OnHoverListener, View.OnTouchListener,
            View.OnLongClickListener, View.OnClickListener {
        FrameLayout recentApp;
        ImageView icon;

        public ViewHolder(View convertView) {
            recentApp = convertView.findViewById(R.id.startupmenu_recent_ll);
            icon = convertView.findViewById(R.id.startupmenu_recent_img);
            recentApp.setOnClickListener(this);
            recentApp.setOnLongClickListener(this);
            recentApp.setOnHoverListener(this);
            recentApp.setOnTouchListener(this);
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
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
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mDownX = (int) event.getRawX();
                mDownY = (int) event.getRawY();
                switch (event.getButtonState()) {
                    case MotionEvent.BUTTON_PRIMARY:
                        //mRecentOnClickCallback.open((AppInfo) v.getTag());
                        Log.e("lxx---","recent---mDownx="+mDownX+"  mDownY="+mDownY);
                        break;
                    case MotionEvent.BUTTON_SECONDARY:
                        mRecentOnClickCallback.showDialog(mDownX, mDownY, (AppInfo) v.getTag());
                        Toast.makeText(v.getContext(), "右键菜单", Toast.LENGTH_LONG).show();
                        break;
                }
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            mRecentOnClickCallback.open((AppInfo) v.getTag());
        }

        @Override
        public boolean onLongClick(View v) {
            mRecentOnClickCallback.showDialog(mDownX, mDownY, (AppInfo) v.getTag());
            Toast.makeText(v.getContext(), "右键菜单", Toast.LENGTH_LONG).show();
            return true;
        }
    }
}
