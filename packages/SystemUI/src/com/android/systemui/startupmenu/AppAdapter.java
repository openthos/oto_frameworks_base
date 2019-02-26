package com.android.systemui.startupmenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.bean.Type;
import com.android.systemui.startupmenu.listener.OnClickCallback;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends BaseAdapter {
    private List<AppInfo> mAppInfos;
    private Context mContext;
    private Type mType;
    private OnClickCallback mOnClickCallback;
    private int mDownX;
    private int mDownY;
    public static View mLastView;

    public AppAdapter(Context context, Type type) {
        mContext = context;
        mType = type;
        mAppInfos = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mAppInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mAppInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(getLayoutId(), null);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (holder == null) {
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        AppInfo appInfo = mAppInfos.get(position);
        try {
            holder.appIcon.setImageDrawable(
                mContext.getPackageManager().getApplicationIcon(appInfo.getPackageName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.tvAppLabel.setText(appInfo.getLabel());
        holder.layout.setTag(appInfo);
        return convertView;
    }

    public void setOnClickCallback(OnClickCallback onClickCallback) {
        mOnClickCallback = onClickCallback;
    }

    public void refresh(List<AppInfo> appInfos) {
        mAppInfos.clear();
        if (appInfos != null) {
            mAppInfos.addAll(appInfos);
        }
        notifyDataSetChanged();
    }

    private class ViewHolder implements View.OnTouchListener,
            View.OnHoverListener, View.OnClickListener, View.OnLongClickListener {
        LinearLayout layout;
        ImageView appIcon;
        TextView tvAppLabel;

        public ViewHolder(View view) {
            layout = (LinearLayout) view.findViewById(R.id.layout);
            appIcon = (ImageView) view.findViewById(R.id.package_image);
            tvAppLabel = (TextView) view.findViewById(R.id.package_name);

            layout.setOnTouchListener(this);
            layout.setOnHoverListener(this);
            layout.setOnClickListener(this);
            layout.setOnLongClickListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mOnClickCallback == null) {
                return false;
            }
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
                mDownX = (int) event.getRawX();
                mDownY = (int) event.getRawY();
                switch (event.getButtonState()) {
                    case MotionEvent.BUTTON_PRIMARY:
//                        mOnClickCallback.open((AppInfo) v.getTag());
                        mLastView = v;
                        v.setSelected(true);
                        break;
                    case MotionEvent.BUTTON_SECONDARY:
                        mLastView = v;
                        v.setSelected(true);
                        mOnClickCallback.showDialog(mDownX, mDownY, (AppInfo) v.getTag());
                        return true;
                }
            }
            return false;
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    if (mLastView != null && mLastView != v) {
                        mLastView.setSelected(false);
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
        public void onClick(View v) {
            mOnClickCallback.open((AppInfo) v.getTag());
        }

        @Override
        public boolean onLongClick(View v) {
            mLastView = v;
            v.setSelected(true);
            mOnClickCallback.showDialog(mDownX, mDownY, (AppInfo) v.getTag());
            return true;
        }
    }

    private int getLayoutId() {
        if (mType == Type.GRID) {
            return R.layout.startmenu_item_row;
        } else {
            return R.layout.startmenu_item_column;
        }
    }
}
