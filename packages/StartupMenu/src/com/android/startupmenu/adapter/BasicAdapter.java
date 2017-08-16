package com.android.startupmenu.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.startupmenu.R;
import com.android.startupmenu.StartupMenuActivity;
import com.android.startupmenu.bean.AppInfo;
import com.android.startupmenu.util.Constants;
import com.android.startupmenu.util.SqliteOperate;

import java.util.List;

public abstract class BasicAdapter extends BaseAdapter
        implements View.OnTouchListener, View.OnHoverListener, View.OnLongClickListener {
    public List<AppInfo> mAppInfos;
    public StartupMenuActivity mActivity;
    private View mTempView;
    private boolean mIsLongPress;
    private boolean mIsLeftKey;
    private boolean mIsRightKey;
    private int mDownX;
    private int mDownY;

    public BasicAdapter(Context context, List<AppInfo> appInfos) {
        mActivity = (StartupMenuActivity) context;
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
            convertView = LayoutInflater.from(mActivity).inflate(getLayoutId(), null);
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

    public void openAppBroadcast(Context context) {
        Intent openAppIntent = new Intent();
        openAppIntent.setAction(Constants.ACTION_OPEN_APPLICATION);
        context.sendBroadcastAsUser(openAppIntent, UserHandle.ALL);
    }

    public void openApplication(AppInfo appInfo) {
        Intent intent = appInfo.getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);
        openAppBroadcast(mActivity);
        SqliteOperate.updateDataStorage(mActivity, appInfo);
        mActivity.killStartupMenu();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        AppInfo appInfo = (AppInfo) v.getTag();
        mActivity.setFocus(true);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsLongPress = false;
                mIsLeftKey = false;
                mIsRightKey = false;
                mDownX = (int) event.getRawX();
                mDownY = (int) event.getRawY();
                switch (event.getButtonState()) {
                    case MotionEvent.BUTTON_PRIMARY:
                        mIsLeftKey = true;
                        openApplication(appInfo);
                        break;
                    case MotionEvent.BUTTON_SECONDARY:
                        mIsRightKey = true;
                        showDialog(mDownX, mDownY, appInfo);
                        break;
                    default:
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!(mIsLeftKey || mIsRightKey || mIsLongPress)) {
                    openApplication(appInfo);
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_HOVER_ENTER:
                if (mTempView != null && mTempView != v) {
                    mTempView.setBackgroundResource(getExitResource());
                }
                v.setBackgroundResource(getEnterResource());
                mTempView = v;
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                if (mTempView != v) {
                    v.setBackgroundResource(getExitResource());
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        mIsLongPress = true;
        mActivity.setFocus(true);
        showDialog(mDownX, mDownY, (AppInfo) v.getTag());
        return false;
    }

    class ViewHolder {
        LinearLayout layout;
        ImageView appIcon;
        TextView tvAppLabel;

        public ViewHolder(View view) {
            layout = (LinearLayout)view.findViewById(R.id.layout);
            appIcon = (ImageView) view.findViewById(R.id.package_image);
            tvAppLabel = (TextView) view.findViewById(R.id.package_name);
            layout.setOnTouchListener(BasicAdapter.this);
            layout.setOnLongClickListener(BasicAdapter.this);
            layout.setOnHoverListener(BasicAdapter.this);
        }
    }

    public abstract int getLayoutId();

    public abstract int getExitResource();

    public abstract int getEnterResource();

    public abstract void showDialog(int x, int y, AppInfo appInfo);
}
