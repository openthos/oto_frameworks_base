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

public class StartupMenuAdapter extends BaseAdapter {

	private List<AppInfo> mlistAppInfo = null;

	LayoutInflater infater = null;

	public StartupMenuAdapter(Context context, List<AppInfo> apps) {
		infater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mlistAppInfo = apps;
	}

	@Override
	public int getCount() {
		System.out.println("size" + mlistAppInfo.size());
		return mlistAppInfo.size();
	}

	@Override
	public Object getItem(int position) {
		return mlistAppInfo.get(position);
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
			view = infater.inflate(R.layout.activity_gridview, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertview;
			holder = (ViewHolder) convertview.getTag();
		}
		AppInfo appInfo = (AppInfo) getItem(position);
		holder.appIcon.setImageDrawable(appInfo.getAppIcon());
		holder.tvAppLabel.setText(appInfo.getAppLabel());
		// holder.tvPkgName.setText(appInfo.getPkgName());
		return view;
	}

	class ViewHolder {
		ImageView appIcon;
		TextView tvAppLabel;
//		TextView tvPkgName;

		public ViewHolder(View view) {
			this.appIcon = (ImageView) view.findViewById(R.id.package_image);
			this.tvAppLabel = (TextView) view.findViewById(R.id.package_name);
			// this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
		}
	}
}
