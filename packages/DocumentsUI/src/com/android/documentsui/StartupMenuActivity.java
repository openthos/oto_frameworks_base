package com.android.documentsui;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.android.documentsui.util.AppInfo;

import android.content.ActivityNotFoundException;
import android.util.Slog;
import android.R.layout;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.View.OnGenericMotionListener;
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

        public static final int FILTER_TIME_SORT = 0;
        public static final int FILTER_ALL_APP = 1;
        public static final int FILTER_SYSYTEM_APP = 2;
        public static final int FILTER_THIRD_APP = 3;

        private GridView gv_view = null;
        private List<AppInfo> mlistAppInfo = null;
        LayoutInflater infater = null;

        private Context mContext;
        private View contentView;
        private LinearLayout ll_layout;
        private TextView shut_text;
        private TextView my_computer, system_setting,name_sort,time_sort,frequency_sort;
        private PopupWindow popupWindow;
        private StartupMenuAdapter browseAppAdapter;
        private int CLICKS = 0;

        private String converToString;
        private Date sysDate;

        private Handler handler = new Handler () {
            public void handleMessage (android.os.Message msg) {
                switch (msg.what) {
                case FILTER_ALL_APP:
                    concealProgressBar();
                    queryAppInfo(FILTER_ALL_APP);
                    dapterReload();
                    break;
                case FILTER_SYSYTEM_APP:
                    concealProgressBar();
                    queryAppInfo(FILTER_SYSYTEM_APP);
                    dapterReload();
                    break;
                case FILTER_THIRD_APP:
                    concealProgressBar();
                    queryAppInfo(FILTER_THIRD_APP);
                    dapterReload();
                    break;
                case FILTER_TIME_SORT:
                    concealProgressBar();
                    queryAppInfo(FILTER_ALL_APP);
                    timeSort();
                    dapterReload();
                    break;
                }
            }
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
            getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
            setContentView(R.layout.start_activity);
            mContext=this;
            ll_layout = (LinearLayout)findViewById(R.id.ll_layout);

            gv_view = (GridView) findViewById(R.id.gv_view);
            StartupMenuActivity.this.setFinishOnTouchOutside(true);

            mlistAppInfo = new ArrayList<AppInfo>();
            queryAppInfo(FILTER_ALL_APP);
            browseAppAdapter = new StartupMenuAdapter(this, mlistAppInfo);
            gv_view.setAdapter(browseAppAdapter);
            gv_view.setOnItemClickListener(this);

            my_computer = (TextView) findViewById(R.id.my_computer);
            system_setting = (TextView) findViewById(R.id.system_setting);
            my_computer.setOnClickListener(this);
            system_setting.setOnClickListener(this);

            name_sort = (TextView) findViewById(R.id.name_sort);
            time_sort = (TextView) findViewById(R.id.time_sort);
            frequency_sort = (TextView) findViewById(R.id.frequency_sort);
            name_sort.setOnClickListener(this);
            time_sort.setOnClickListener(this);
            frequency_sort.setOnClickListener(this);
        }

        private void timeSort() {
            Collections.sort(mlistAppInfo, new Comparator<Object>() {
                public int compare(Object lhs, Object rhs) {
                    AppInfo p1 = (AppInfo) lhs;
                    AppInfo p2 = (AppInfo) rhs;
                    return p2.getDate().compareTo(p1.getDate());
                };
            });
        }

        private void dapterReload() {
            browseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this, mlistAppInfo);
            gv_view.setAdapter(browseAppAdapter);
        }

        private void concealProgressBar() {
            mlistAppInfo = new ArrayList<AppInfo>();
        }

        public static String ConverToString(Date date) {
            DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
            return df.format(date);
        }

        public void appType(PackageManager pm, ResolveInfo reInfo) {
            String activityName = reInfo.activityInfo.name;
            String pkgName = reInfo.activityInfo.packageName;
            String appLabel = (String) reInfo.loadLabel(pm);
            Drawable icon = reInfo.loadIcon(pm);
            Intent launchIntent = new Intent();
            launchIntent.setComponent(new ComponentName(pkgName,activityName));
            AppInfo appInfo = new AppInfo();
            appInfo.setAppLabel(appLabel);
            appInfo.setPkgName(pkgName);
            appInfo.setDate(sysDate);
            appInfo.setAppIcon(icon);
            appInfo.setIntent(launchIntent);
            mlistAppInfo.add(appInfo);
        }

        public void queryAppInfo(int a) {
            PackageManager pm = this.getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
            Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
            if (mlistAppInfo != null) {
                mlistAppInfo.clear();
                for (ResolveInfo reInfo : resolveInfos) {
                    File file = new File(reInfo.activityInfo.applicationInfo.sourceDir);
                    sysDate = new Date(file.lastModified());
                    ApplicationInfo applicationInfo = reInfo.activityInfo.applicationInfo;
                    if (a == FILTER_ALL_APP ) {
                        appType(pm, reInfo);
                    }
                    if (a == FILTER_SYSYTEM_APP && isSystemApp(applicationInfo)) {
                        appType(pm, reInfo);
                    }
                    if (a == FILTER_THIRD_APP && !isSystemApp(applicationInfo)) {
                        appType(pm, reInfo);
                        if (CLICKS == 3) {
                            CLICKS = 0;
                        }
                    }
                }
            }
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = mlistAppInfo.get(position).getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.my_computer:
                /* start FileManager */
                for (int i=0;i<mlistAppInfo.size();i++) {
                    AppInfo appInfo = mlistAppInfo.get(i);
                    PackageManager pm = this.getPackageManager();
                    String packName  = appInfo.getPkgName();
                    if (packName.compareTo("com.cyanogenmod.filemanager") == 0) {
                        Intent intent = appInfo.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
                break;
            case R.id.system_setting:
                if (android.os.Build.VERSION.SDK_INT > 13) {
                    startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                } else {
                    startActivity(new Intent(android.provider.Settings.ACTION_APN_SETTINGS)
                   .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }
                break;
            case R.id.shut_power_off:
                Log.v("LADEHUNTER", "broadcast->shutdown");
                Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
                intent.putExtra(Intent.EXTRA_KEY_CONFIRM, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_RUN_FULLSCREEN);
                startActivity(intent);
                killStartupMenu();
                break;
	    case R.id.restart:
                try {
                    Runtime.getRuntime().exec("su -c \"/system/bin/reboot\"");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.sleep:
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                Log.v("This sleep : ", "COMING SOON...");
                Toast.makeText(this, "This sleep: COMING SOON...", 0).show();
                break;
            case R.id.lock:
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                Intent intentLock = new Intent("android.intent.action.LOCKNOW");
                intentLock.addFlags(Intent.FLAG_RUN_FULLSCREEN | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentLock);
                break;
            case R.id.name_sort:
                mlistAppInfo.clear();
                browseAppAdapter.notifyDataSetChanged();
                thread(FILTER_ALL_APP);
                break;
            case R.id.time_sort:
                mlistAppInfo.clear();
                browseAppAdapter.notifyDataSetChanged();
                thread(FILTER_TIME_SORT);
                break;
            case R.id.frequency_sort:
                if (CLICKS == 3) {
                    CLICKS = 0;
                }
                CLICKS++;
                mlistAppInfo.clear();
                browseAppAdapter.notifyDataSetChanged();
                thread(CLICKS);
                break;
            }
        }

        private void thread (final int a) {
            new Thread () {
                public void run () {
                    handler.sendEmptyMessage(a);
                };
            }.start();
        }
        private boolean isSystemApp(ApplicationInfo applicationInfo) {
            return (applicationInfo.flags & applicationInfo.FLAG_SYSTEM) > 0;
        }

        public void powerOff(View v) {
            LinearLayout layout = new LinearLayout(StartupMenuActivity.this);
            layout.setBackgroundColor(Color.WHITE);
            View tv = LayoutInflater.from(StartupMenuActivity.this).inflate(R.layout.shutdown_activity,
                                                                            null);
            tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                               LayoutParams.WRAP_CONTENT));
            TextView shut_power_off = (TextView) tv.findViewById(R.id.shut_power_off);
            TextView restart = (TextView) tv.findViewById(R.id.restart);
            TextView sleep = (TextView) tv.findViewById(R.id.sleep);
            TextView lock = (TextView) tv.findViewById(R.id.lock);
            shut_power_off.setOnClickListener(this);
            restart.setOnClickListener(this);
            sleep.setOnClickListener(this);
            lock.setOnClickListener(this);
            layout.addView(tv);

            popupWindow = new PopupWindow(layout,100,230);
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new BitmapDrawable());

            int[] location = new int[2];
            v.getLocationOnScreen(location);
            //popupWindow.showAtLocation(v, Gravity.NO_GRAVITY,location[0] + v.getWidth(), location[1]);
            popupWindow.showAtLocation(v, Gravity.RIGHT, 100, 230);
	}

        private void killStartupMenu() {
            try {
                ActivityManagerNative.getDefault().killStartupMenu();
                System.exit(0);
            } catch (RemoteException e) {
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
                killStartupMenu();
            }

            // Delegate everything else to Activity.
            return super.onTouchEvent(event);
        }
}
