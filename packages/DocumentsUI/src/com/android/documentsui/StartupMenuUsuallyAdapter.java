package com.android.documentsui;

import java.util.List;
import com.android.documentsui.R;
import com.android.documentsui.util.AppInfo;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StartupMenuUsuallyAdapter extends BaseAdapter {

    private List<AppInfo> mlistViewAppInfo = null;
    LayoutInflater mInfater = null;

    public StartupMenuUsuallyAdapter(Context context, List<AppInfo> apps) {
        mInfater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mlistViewAppInfo = apps;
    }

    @Override
    public int getCount() {
        return mlistViewAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mlistViewAppInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertview, ViewGroup arg2) {
        View view = null;
        ViewHolder holder = null;
        if (convertview == null || convertview.getTag() == null) {
            view = mInfater.inflate(R.layout.activity_listview_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertview;
            holder = (ViewHolder) convertview.getTag();
        }
        AppInfo appInfo = (AppInfo) getItem(position);
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        holder.tvAppLabel.setText(getAppName(appInfo.getAppLabel()));
        // holder.tvPkgName.setText(appInfo.getPkgName());
        return view;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;
        //TextView tvPkgName;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.list_package_image);
            this.tvAppLabel = (TextView) view.findViewById(R.id.list_package_name);
            // this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
        }
    }

    private String getAppName(String str) {
        String appName = str;
        if (str.length() > 10) {
            appName = str.substring(0,10) + "...";
        }
        return appName;
    }
}
