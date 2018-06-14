package com.android.startupmenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.startupmenu.bean.AppInfo;
import com.android.startupmenu.bean.Type;
import com.android.startupmenu.listener.OnClickCallback;

import java.util.List;

public class AppAdapter extends BaseAdapter {
    private List<AppInfo> mAppInfos;
    private Context mContext;
    private Type mType;
    private View mTempView;
    private OnClickCallback mOnClickCallback;

    public AppAdapter(Context context, Type type, List<AppInfo> appInfos) {
        mContext = context;
        mType = type;
        mAppInfos = appInfos;
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
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        holder.tvAppLabel.setText(appInfo.getAppLabel());
        holder.layout.setTag(appInfo);
        return convertView;
    }

    public void exit() {
        if (mTempView != null) {
            mTempView.setSelected(false);
            mTempView = null;
        }
    }

    public void setOnClickCallback(OnClickCallback onClickCallback) {
        mOnClickCallback = onClickCallback;
    }

    private class ViewHolder implements View.OnTouchListener, View.OnHoverListener {
        LinearLayout layout;
        ImageView appIcon;
        TextView tvAppLabel;

        public ViewHolder(View view) {
            layout = (LinearLayout) view.findViewById(R.id.layout);
            appIcon = (ImageView) view.findViewById(R.id.package_image);
            tvAppLabel = (TextView) view.findViewById(R.id.package_name);

            layout.setOnTouchListener(this);
            layout.setOnHoverListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mOnClickCallback == null) {
                return false;
            }
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
                switch (event.getButtonState()) {
                    case MotionEvent.BUTTON_PRIMARY:
                        mOnClickCallback.open((AppInfo) v.getTag());
                        break;
                    case MotionEvent.BUTTON_SECONDARY:
                        mOnClickCallback.showDialog(
                                (int) event.getRawX(), (int) event.getRawY(), (AppInfo) v.getTag());
                        break;
                }
            }
            return false;
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    if (mTempView != null && mTempView != v) {
                        mTempView.setSelected(false);
                    }
                    v.setSelected(true);
                    mTempView = v;
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    if (mTempView != v) {
                        v.setSelected(false);
                    }
                    break;
            }
            return false;
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
