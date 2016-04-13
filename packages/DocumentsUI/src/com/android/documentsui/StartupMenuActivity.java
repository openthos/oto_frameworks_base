package com.android.documentsui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.android.documentsui.util.AppInfo;

import android.R.layout;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.View.OnClickListener;
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
import android.widget.AdapterView.OnItemClickListener;

public class StartupMenuActivity extends Activity implements OnClickListener,
                OnItemClickListener {

        private GridView gv_view = null;
        private List<AppInfo> mlistAppInfo = null;
        LayoutInflater infater = null;

        private Context mContext;
        private View contentView;
        private LinearLayout ll_layout;
        private TextView shut_text;
        private TextView my_computer, system_setting, power_off;
        private PopupWindow popupWindow;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
                getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
                setContentView(R.layout.start_activity);

                mContext = this;
                ll_layout = (LinearLayout)findViewById(R.id.ll_layout);
                gv_view = (GridView) findViewById(R.id.gv_view);
                StartupMenuActivity.this.setFinishOnTouchOutside(true);

                mlistAppInfo = new ArrayList<AppInfo>();
                queryAppInfo();
                StartupMenuAdapter browseAppAdapter = new StartupMenuAdapter(this, mlistAppInfo);
                gv_view.setAdapter(browseAppAdapter);
                gv_view.setOnItemClickListener(this);

                my_computer = (TextView) findViewById(R.id.my_computer);
                system_setting = (TextView) findViewById(R.id.system_setting);
                power_off = (TextView) findViewById(R.id.power_off);
                my_computer.setOnClickListener(this);
                power_off.setOnClickListener(this);
                system_setting.setOnClickListener(this);
        }

        public void queryAppInfo() {
                PackageManager pm = this.getPackageManager();
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> resolveInfos = pm
                                .queryIntentActivities(mainIntent, 0);
                Collections.sort(resolveInfos,
                                new ResolveInfo.DisplayNameComparator(pm));
                if (mlistAppInfo != null) {
                        mlistAppInfo.clear();
                        for (ResolveInfo reInfo : resolveInfos) {
                                String activityName = reInfo.activityInfo.name;
                                String pkgName = reInfo.activityInfo.packageName;
                                String appLabel = (String) reInfo.loadLabel(pm);
                                Drawable icon = reInfo.loadIcon(pm);
                                Intent launchIntent = new Intent();
                                launchIntent.setComponent(new ComponentName(pkgName,
                                                activityName));
                                AppInfo appInfo = new AppInfo();
                                appInfo.setAppLabel(appLabel);
                                appInfo.setPkgName(pkgName);
                                appInfo.setAppIcon(icon);
                                appInfo.setIntent(launchIntent);
                                mlistAppInfo.add(appInfo);
                        }
                }
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                        AppInfo appInfo = mlistAppInfo.get(position);
                        PackageManager pm = this.getPackageManager();
                        String packName  = appInfo.getPkgName();
                        try {
                                PackageInfo packInfo = pm.getPackageInfo(packName, PackageManager.GET_ACTIVITIES);
                                ActivityInfo [] activitys = packInfo.activities;
                                if(activitys!=null&&activitys.length>0){
                                        ActivityInfo activityInfo = activitys[0];
                                        String className = activityInfo.name;
                                        Log.e("LADEHUNTER",packName+" "+className,null);
                                        Runtime.getRuntime().exec("am start -n "+packName+"/"+className);
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
        public void onClick(View v) {
                switch (v.getId()) {
                case R.id.my_computer:
                        /* start FileManager */
                        break;
                case R.id.system_setting:
                        if (android.os.Build.VERSION.SDK_INT > 13) {
                                startActivity(new Intent(
                                                android.provider.Settings.ACTION_SETTINGS));
                        } else {
                                Intent intent = new Intent(
                                                android.provider.Settings.ACTION_WIFI_IP_SETTINGS);
                                startActivity(intent);
                        }
                        break;
                case R.id.power_off:
                        repotOff();
                        break;

                }
        }

        private boolean ONCK_DOWN;

        private void repotOff() {
                ONCK_DOWN = !ONCK_DOWN;
                if (ONCK_DOWN) {
                        contentView = View.inflate(StartupMenuActivity.this,
                                        R.layout.activity_shutdown, null);
                        shut_text = (TextView) contentView.findViewById(R.id.sdowm_text);
                        popupWindow = new PopupWindow(contentView, 100, 200);
                        popupWindow.setBackgroundDrawable(new ColorDrawable(
                                        Color.TRANSPARENT));
                        int[] location = new int[2];
                        shut_text.getLocationInWindow(location);
                        popupWindow.showAtLocation(contentView, Gravity.LEFT, 100, 300);

                        AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
                        aa.setDuration(500);

                        ScaleAnimation sa = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
                                        Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                                        0.5f);
                        sa.setDuration(500);
                        AnimationSet set = new AnimationSet(false);
                        set.addAnimation(sa);
                        set.addAnimation(aa);
                        contentView.startAnimation(set);
                        shut_text.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                        try {
                                                Runtime.getRuntime().exec("poweroff");
                                        } catch (IOException e) {
                                                e.printStackTrace();
                                        }
                                }
                        });
                } else {
                        popupWindow.dismiss();
                }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
                //finish();
                try {
                    ActivityManagerNative.getDefault().killStartupMenu();
                    System.exit(0);
                } catch (RemoteException e) {
                }
            }

            // Delegate everything else to Activity.
            return super.onTouchEvent(event);
        }
}
