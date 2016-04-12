package com.android.documentsui;

import java.util.ArrayList;
import java.util.List;

import com.android.documentsui.util.AppInfo;

import android.R.layout;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.View.OnFocusChangeListener;
//import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.ViewGroup;
import android.view.LayoutInflater;
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

public class StartupMenuActivity extends Activity {
        private Context mContext;
        private GridView gv_view;
        private List<AppInfo> appInfos;
        private List<AppInfo> userappInfos;
        private List<AppInfo> systemappInfos;
        private LinearLayout ll_layout;
        private MaAdapter adapter;
        private AppInfo appInfo;
        private TextView shoutdown_view;
        private TextView shout_text;
        private PopupWindow popupWindow;
        private SharedPreferences sp;
        private boolean STARTUP_MENU;
        private Editor editor;
        private Handler handler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                        adapter = new MaAdapter();
                        gv_view.setAdapter(adapter);
//                        ll_loading.setVisibility(View.INVISIBLE);
                };
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                sp = getSharedPreferences("config",MODE_PRIVATE);
                //STARTUP_MENU = !STARTUP_MENU;
                editor = sp.edit();
                //editor.putBoolean("showsystem",STARTUP_MENU);
                //editor.commit();
                try{
                    STARTUP_MENU = sp.getBoolean("showsystem",true);
                }catch(Exception e){
                    STARTUP_MENU = true;
                }
                if(!STARTUP_MENU){
                    //STARTUP_MENU = !STARTUP_MENU;
                    //editor.putBoolean("showsystem",STARTUP_MENU);
                    //editor.commit();
                    finish();
                }
                STARTUP_MENU = !STARTUP_MENU;
                editor.putBoolean("showsystem",STARTUP_MENU);
                editor.commit();
                super.onCreate(savedInstanceState);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		 getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
                setContentView(R.layout.start_activity);
                mContext = this;
                fillData();
                int stmm = 0;
                if(STARTUP_MENU)
                        stmm = 1;
                else
                        stmm = 0;
                Log.e("LADEHUNTER","OnCREATE a STM!!!!!!!!"+" "+stmm,null);
                ll_layout = (LinearLayout)findViewById(R.id.ll_layout);
                StartupMenuActivity.this.setFinishOnTouchOutside(true);
                //gv_view = (GridView) findViewById(R.id.gv_view);
                //final View view = inflater.inflate(R.layout.fragment_directory, mContext, false);
                //gv_view = (GridView) view.findViewById(R.id.grid);
                gv_view = (GridView) findViewById(R.id.gv_view);

                gv_view.setOnItemClickListener(new MyItemclick());
                shoutdown_view=(TextView) findViewById(R.id.shoutdown_view);
                shoutdown_view.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                View contentView = View.inflate(StartupMenuActivity.this,
                                                R.layout.activity_shutdown, null);
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
                View vi= View.inflate(StartupMenuActivity.this, R.layout.start_activity, null);
                vi.setVisibility(View.GONE);
        }
	 /*@Override
        public boolean onKeyDown(int keyCode , KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_MENU) {
                finish();
            }
            return super.onKeyDown(keyCode,event);
        }*/

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
                try{
                    Runtime.getRuntime().exec("input keyevent KEYCODE_MENU");
                } catch(Exception e) {

                }
                //finish();
                return true;
            }

            // Delegate everything else to Activity.
            return super.onTouchEvent(event);
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
                                view = View.inflate(StartupMenuActivity.this,
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
                                appInfos = com.android.documentsui.util.AppInfoProvider.getAppInfos(StartupMenuActivity.this);
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

        private void startApp() {

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
                                        Log.e("LADEHUNTER",packName+" "+className,null);
                                        Runtime.getRuntime().exec("am start -n "+packName+"/"+className);
                                        /*STARTUP_MENU = !STARTUP_MENU;
                                        editor.putBoolean("showsystem",STARTUP_MENU);
                                        editor.commit();
                                        //ll_layout.setVisibility(View.GONE);*/
                                        Runtime.getRuntime().exec("input keyevent KEYCODE_MENU");
                                        //StartupMenuActivity.this.finish();
                                        //intent.setClassName(packName, className);
                                        //startActivity(intent);
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
