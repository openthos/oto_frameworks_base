package com.android.systemui.startupmenu;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.systemui.R;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecentDocsAdapter extends BaseAdapter {
    private Context mContext;
    private int mLayoutId;
    private List<AppInfo> mRecentDocsData = new ArrayList<>();

    public RecentDocsAdapter(Context context, List<AppInfo> recentDocsData, int layoutId) {
        mContext = context;
        mLayoutId = layoutId;
        mRecentDocsData.addAll(recentDocsData);
    }

    @Override
    public int getCount() {
        return mRecentDocsData.size() < 10 ? mRecentDocsData.size() : 9;
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
        } else if (appInfo.getLabel().contains("txt")) {
            viewHolder.recentDocImg.setImageResource(R.drawable.txt);
        } else if (appInfo.getLabel().contains("pdf")) {
            viewHolder.recentDocImg.setImageResource(R.drawable.startupmenu_poweroff);
        }
        viewHolder.recentDocTime.setText(Util.getTimeFormatText(mContext, appInfo));
        viewHolder.recentDocs.setTag(mRecentDocsData.get(position));

        return convertView;
    }

    public void updateRecentDocsData(List<AppInfo> recentDocsData) {
        if (!recentDocsData.isEmpty()) {
            mRecentDocsData.clear();
            mRecentDocsData.addAll(recentDocsData);
            Log.e("lxx-File-adapter", "updateRecentDocsData: size="+mRecentDocsData.size()+"-besize="+recentDocsData.size());
            notifyDataSetChanged();
        }
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
            String path = ((AppInfo) v.getTag()).getPath();
            String fileType = Util.getMIMEType(new File(path));
            List<ResolveInfo> resolveInfoList = new ArrayList<>();
            PackageManager manager = mContext.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = null;
            if (android.os.Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(mContext,
                        "com.android.systemui.fileprovider", new File(path));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(new File(path));
            }
            intent.setDataAndType(uri, fileType);
            resolveInfoList = manager.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            Log.e("lxx-manager","path="+path+" resolveInfoList.size="+resolveInfoList.size()+" uri="+uri+" filetype="+fileType);
            if (resolveInfoList.size() > 0) {
                if (android.os.Build.VERSION.SDK_INT >= 24) {
	                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                mContext.startActivity(intent);
            } else {
            //    OpenWithDialog openWithDialog = new OpenWithDialog(mContext, path);
            //    openWithDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //    openWithDialog.showDialog();
                Toast.makeText(mContext, "无法打开最近文档", Toast.LENGTH_LONG).show();
            }
        }
    }
}
