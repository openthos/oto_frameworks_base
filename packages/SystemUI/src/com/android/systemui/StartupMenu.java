package com.android.systemui;

import java.util.ArrayList;
import java.util.List;

import com.android.systemui.util.AppInfo;

import android.R.layout;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StartupMenu extends Activity {
	private GridView gv_view;
	private List<AppInfo> appInfos;
	private List<AppInfo> userappInfos;
	private List<AppInfo> systemappInfos;
	private MaAdapter adapter;
	private AppInfo appInfo;
	private TextView shoutdown_view;
	private TextView shout_text;
	private PopupWindow popupWindow;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			adapter = new MaAdapter();
			gv_view.setAdapter(adapter);
//			ll_loading.setVisibility(View.INVISIBLE);
			
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		fillData();
		gv_view = (GridView) findViewById(R.id.gv_view);
		gv_view.setOnItemClickListener(new MyItemclick());
		shoutdown_view=(TextView) findViewById(R.id.shoutdown_view);
		shoutdown_view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View contentView = View.inflate(StartupMenu.this,
						R.layout.activity_shoutdown, null);
				shout_text= (TextView) contentView.findViewById(R.id.sdowm_text);
				popupWindow = new PopupWindow(contentView, 200,
						100);
				popupWindow.setBackgroundDrawable(new ColorDrawable(
						Color.TRANSPARENT));
				int[] location = new int[2];
				shout_text.getLocationInWindow(location);
				popupWindow.showAtLocation(contentView,  Gravity.LEFT, 230,300);

				AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
				aa.setDuration(500);

				ScaleAnimation sa = new ScaleAnimation(0.5f, 1.0f, 0.5f,
						1.0f, Animation.RELATIVE_TO_SELF, 0,
						Animation.RELATIVE_TO_SELF, 0.5f);
				sa.setDuration(500);
				AnimationSet set = new AnimationSet(false);
				set.addAnimation(sa);
				set.addAnimation(aa);

				contentView.startAnimation(set);

				shout_text.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
					}
				});
			}
			
		});
	}
	public void button(View view){
		
		View vi= View.inflate(StartupMenu.this, R.layout.start_activity, null);
		
		vi.setVisibility(View.GONE);
	}
	class MyItemclick implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Object obj = gv_view.getItemAtPosition(position);
			if (obj != null) {
				appInfo = (AppInfo) obj;
				startApp();
			}
		}
		
	}
	class MaAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return userappInfos.size() + systemappInfos.size();
		}

		@Override
		public Object getItem(int position) {

			AppInfo appInfo;
			if (position == 0) {
				return null;
			} else if (position == (userappInfos.size() + 1)) {
				return null;
			} else if (position <= userappInfos.size()) {
				int newposition = position - 1;
				appInfo = userappInfos.get(newposition);
			} else {
				int newposition = position - userappInfos.size() - 1 - 1;
				appInfo = systemappInfos.get(newposition);
			}
			return appInfo;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AppInfo appInfo;
			if (position == 0) {
				TextView tv = new TextView(getApplicationContext());
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				return tv;
			} else if (position == (userappInfos.size() + 1)) {
				TextView tv = new TextView(getApplicationContext());
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				return tv;
			} else if (position <= userappInfos.size()) {
				int newposition = position - 1;
				appInfo = userappInfos.get(newposition);
			} else {
				int newposition = position - userappInfos.size() - 1 - 1;
				appInfo = systemappInfos.get(newposition);
			}
			View view;
			ViewHolder holder;
			if (convertView != null && convertView instanceof RelativeLayout) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(StartupMenu.this,
						R.layout.activity_gridview, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view
						.findViewById(R.id.package_image);
				holder.tv_name = (TextView) view
						.findViewById(R.id.package_name);

				view.setTag(holder);

			}
			holder.iv_icon.setImageDrawable(appInfo.getIcon());
			holder.tv_name.setText(appInfo.getName());
			return view;
		}

	}

	static class ViewHolder {
		ImageView iv_icon;
		TextView tv_name;
		// TextView tv_location;
	}

	private void fillData() {
		// ll_loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				userappInfos = new ArrayList<AppInfo>();
				systemappInfos = new ArrayList<AppInfo>();
				appInfos = com.android.systemui.util.AppInfoProvider.getAppInfos(StartupMenu.this);
				for (AppInfo appInfo : appInfos) {
					if (appInfo.isUser()) {
						userappInfos.add(appInfo);
					} else {
						systemappInfos.add(appInfo);
					}
				}
				handler.sendEmptyMessage(0);
			};
		}.start();

	}

	public void startApp() {
		
		try {
			Intent intent = new Intent();
			PackageManager pm = getPackageManager();
			String packName  = appInfo.getPackName();
			try {
				PackageInfo packInfo = pm.getPackageInfo(packName, PackageManager.GET_ACTIVITIES);
				ActivityInfo [] activitys = packInfo.activities;
				if(activitys!=null&&activitys.length>0){
					ActivityInfo activityInfo = activitys[0];
					String className = activityInfo.name;
					intent.setClassName(packName, className);
					startActivity(intent);
				}else{
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fillData();
	}
}
