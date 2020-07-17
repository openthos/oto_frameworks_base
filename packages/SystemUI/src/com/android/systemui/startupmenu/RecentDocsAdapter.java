package com.android.systemui.startupmenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.OnRecentDocClickCallback;
import com.android.systemui.startupmenu.utils.Util;

import java.util.List;

public class RecentDocsAdapter extends BaseAdapter {
    private static int MAX_COUNT = 10;

    private Context mContext;
    private int mLayoutId;
    private List<AppInfo> mRecentDocsData;
    private OnRecentDocClickCallback mOnRecentDocClickCallback;

    public RecentDocsAdapter(Context context, List<AppInfo> recentDocsData, int layoutId) {
        mContext = context;
        mLayoutId = layoutId;
        mRecentDocsData = recentDocsData;
    }

    @Override
    public int getCount() {
        return mRecentDocsData.size() < MAX_COUNT ? mRecentDocsData.size() : MAX_COUNT - 1;
    }

    @Override
    public Object getItem(int position) {
        return mRecentDocsData.get(position);
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

        AppInfo appInfo = mRecentDocsData.get(position);
        viewHolder.recentDocName.setText(appInfo.getLabel().split("\\.")[0]);
        if (appInfo.getLabel().contains("doc")) {
            viewHolder.recentDocImg.setImageResource(R.drawable.word);
        } else if (appInfo.getLabel().contains("xls")) {
            viewHolder.recentDocImg.setImageResource(R.drawable.excel);
        } else if (appInfo.getLabel().contains("ppt")) {
            viewHolder.recentDocImg.setImageResource(R.drawable.ppt);
        }
        viewHolder.recentDocTime.setText(Util.getTimeFormatText(mContext, appInfo));
        viewHolder.recentDocs.setTag(mRecentDocsData.get(position));

        return convertView;
    }

    public void setOnRecentDocClickCallback(OnRecentDocClickCallback callback) {
        mOnRecentDocClickCallback = callback;
    }

    public class ViewHolder implements View.OnHoverListener, View.OnClickListener {
        RelativeLayout recentDocs;
        ImageView recentDocImg;
        TextView recentDocName, recentDocTime;
        public ViewHolder(View convertView) {
            recentDocs = convertView.findViewById(R.id.startupmenu_recent_docs_ll);
            recentDocImg = convertView.findViewById(R.id.startupmenu_recent_docs_img);
            recentDocName = convertView.findViewById(R.id.startupmenu_recent_docs_name);
            recentDocTime = convertView.findViewById(R.id.startupmenu_recent_docs_time);

            recentDocs.setOnHoverListener(this);
            recentDocs.setOnClickListener(this);
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
        public void onClick(View v) {
            mOnRecentDocClickCallback.open((AppInfo) v.getTag());
        }
    }
}
