package com.android.startupmenu.adapter;

import java.util.List;

import com.android.startupmenu.R;
import com.android.startupmenu.StartupMenuActivity;
import com.android.startupmenu.bean.AppInfo;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.startupmenu.dialog.AppDialog;
import com.android.startupmenu.util.Constants;
import com.android.startupmenu.util.SqliteOperate;

public class AppAdapter extends BaseAdapter {

    private List<AppInfo> mAppInfos;
    private StartupMenuActivity mActivity;

    public AppAdapter(Context context, List<AppInfo> appInfos) {
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
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.activity_gridview, null);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (holder == null) {
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        final AppInfo appInfo = mAppInfos.get(position);
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        holder.tvAppLabel.setText(appInfo.getAppLabel());

        convertView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                int what = motionEvent.getButtonState();
                mActivity.setFocus(true);
                switch (what) {
                    case MotionEvent.BUTTON_PRIMARY:
                        Intent intent = appInfo.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mActivity.startActivity(intent);
                        openAppBroadcast(mActivity);
                        SqliteOperate.updateDataStorage(mActivity, appInfo);
                        mActivity.killStartupMenu();
                        break;
                    case MotionEvent.BUTTON_TERTIARY:
                        break;
                    case MotionEvent.BUTTON_SECONDARY:
                        showMenuDialog(motionEvent, appInfo);
                        break;
                    default:
                        mActivity.setFocus(false);
                        break;
                }
                return false;
            }
        });
        convertView.setOnHoverListener(hoverListener);
        return convertView;
    }

    public static void openAppBroadcast(Context context) {
        Intent openAppIntent = new Intent();
        openAppIntent.setAction(Constants.ACTION_OPEN_APPLICATION);
        context.sendBroadcastAsUser(openAppIntent, UserHandle.ALL);
    }

    View.OnHoverListener hoverListener = new View.OnHoverListener() {
        private View mTempView;
        public boolean onHover(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    if (mTempView != null && mTempView != v) {
                        mTempView.setBackgroundResource(android.R.color.transparent);
                    }
                    v.setBackgroundResource(R.color.app_background);
                    mTempView = v;
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    if (mTempView != v) {
                        v.setBackgroundResource(android.R.color.transparent);
                    }
                    break;
            }
            return false;
        }
    };

    private void showMenuDialog(MotionEvent motionEvent, AppInfo appInfo) {
        AppDialog appDialog = new AppDialog(mActivity, R.style.dialog, appInfo);
        appDialog.showDialog((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.package_image);
            this.tvAppLabel = (TextView) view.findViewById(R.id.package_name);
        }
    }
}
