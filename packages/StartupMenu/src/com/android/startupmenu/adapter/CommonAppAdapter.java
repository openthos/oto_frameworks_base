package com.android.startupmenu.adapter;

import com.android.startupmenu.StartupMenuActivity;
import com.android.startupmenu.R;

import java.util.ArrayList;

import com.android.startupmenu.dialog.CommonAppDialog;
import com.android.startupmenu.bean.AppInfo;
import com.android.startupmenu.util.Constants;
import com.android.startupmenu.util.SqliteOperate;

import android.content.Context;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommonAppAdapter extends BaseAdapter {

    private ArrayList<AppInfo> mAppInfos;
    private int mStartMenuCommonlWidth;
    private int mStartMenuCommonlHeight;
    private StartupMenuActivity mActivity;

    public CommonAppAdapter(Context context, ArrayList<AppInfo> appInfos) {
        mAppInfos = appInfos;
        mActivity = (StartupMenuActivity) context;
        mStartMenuCommonlWidth = mActivity.getResources()
                .getDimensionPixelSize(R.dimen.start_menu_commonl_width);
        mStartMenuCommonlHeight = mActivity.getResources()
                .getDimensionPixelSize(R.dimen.start_menu_commonl_height);
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
            convertView =
                    LayoutInflater.from(mActivity).inflate(R.layout.activity_listview_item, null);
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
                        AppAdapter.openAppBroadcast(mActivity);
                        SqliteOperate.updateDataStorage(mActivity, appInfo);
                        mActivity.killStartupMenu();
                        break;
                    case MotionEvent.BUTTON_TERTIARY:
                        break;
                    case MotionEvent.BUTTON_SECONDARY:
                        if (position < 0 || position >= mAppInfos.size()) {
                            return false;
                        }
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

    View.OnHoverListener hoverListener = new View.OnHoverListener() {
        public boolean onHover(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundResource(R.drawable.power_background);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundResource(R.color.appUsuallyBackground);
                    break;
            }
            return false;
        }
    };

    private void showMenuDialog(MotionEvent motionEvent, AppInfo appInfo) {
        CommonAppDialog commonAppDialog =
                new CommonAppDialog(mActivity, R.style.dialog, appInfo);
        commonAppDialog.showDialog(
                (int) motionEvent.getRawX(),
                (int) motionEvent.getRawY() + Constants.START_MENU_RIGHT_MOUSE_UI_NUMBER,
                mStartMenuCommonlWidth,
                mStartMenuCommonlHeight);
    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.list_package_image);
            this.tvAppLabel = (TextView) view.findViewById(R.id.list_package_name);
        }
    }
}