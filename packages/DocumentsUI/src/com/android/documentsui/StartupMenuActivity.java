package com.android.documentsui;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.android.documentsui.util.AppInfo;
import com.android.documentsui.util.MySqliteOpenHelper;

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
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.KeyEvent;
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
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StartupMenuActivity extends Activity implements OnClickListener,
                OnItemClickListener, OnEditorActionListener {

        public static final int FILTER_ALL_APP = 1;
        public static final int FILTER_SYSYTEM_APP = 2;
        public static final int FILTER_THIRD_APP = 3;

        private List<AppInfo> mlistAppInfo = null;
        private List<AppInfo> mlistViewAppInfo = null;

        private Context mContext;
        private PopupWindow mPopupWindow;
        private StartupMenuAdapter mBrowseAppAdapter, mBroAdapter;
        private StartupMenuUsuallyAdapter mUsuallyAdapter;
        private MySqliteOpenHelper mMsoh;
        private SQLiteDatabase mdb;

        private int mNumber;
        private int CLICKS = 0;
        private boolean mListViewOpen = false;
        private boolean mIsHasReayDb;
        private String mEtext;

        private GridView gv_view;
        private ListView mListView;
        private EditText mEditText;

        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            System.exit(0);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
            getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
            setContentView(R.layout.start_activity);
            mContext=this;

            mMsoh = new MySqliteOpenHelper(StartupMenuActivity.this, "Application_database.db", null, 1);
            mdb = mMsoh.getWritableDatabase();

            gv_view = (GridView) findViewById(R.id.gv_view);
            StartupMenuActivity.this.setFinishOnTouchOutside(true);

            mlistAppInfo = new ArrayList<AppInfo>();
            queryAppInfo();
            mBrowseAppAdapter = new StartupMenuAdapter(this, mlistAppInfo);
            gv_view.setAdapter(mBrowseAppAdapter);
            gv_view.setOnItemClickListener(this);

            TextView system_setting = (TextView) findViewById(R.id.system_setting);
            TextView my_computer = (TextView) findViewById(R.id.my_computer);
            my_computer.setOnClickListener(this);
            system_setting.setOnClickListener(this);

            ImageView imView = (ImageView) findViewById(R.id.iv_view);
            mEditText = (EditText) findViewById(R.id.et_text);
            imView.setOnClickListener(this);
            mEditText.setOnEditorActionListener(this);
            mEditText.addTextChangedListener(watcher);

            new mThread().start();
            mListView = (ListView) findViewById(R.id.lv_view);
            Cursor c = mdb.rawQuery("select distinct * from perpo", new String[] {});
            while (c.moveToNext()) {
                int number = c.getInt(c.getColumnIndex("int"));
                if (number != 0) {
                    mListViewOpen = true;
                }
            }
            if (mListViewOpen) {
                queryCommonlyUsedSoftware();
            }
        }
        class mThread extends Thread {
            public void run(){
                BackstageRenewalData();
            }
        };

        public void queryAppInfo() {
            PackageManager pm = this.getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
            Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
            if (mlistAppInfo != null) {
                mlistAppInfo.clear();
                for (ResolveInfo reInfo : resolveInfos) {
                    File file = new File(reInfo.activityInfo.applicationInfo.sourceDir);
                    Date systemDate = new Date(file.lastModified());
                    ApplicationInfo applicationInfo = reInfo.activityInfo.applicationInfo;
                    String activityName = reInfo.activityInfo.name;
                    String pkgName = reInfo.activityInfo.packageName;
                    String appLabel = (String) reInfo.loadLabel(pm);
                    Drawable icon = reInfo.loadIcon(pm);
                    Intent launchIntent = new Intent();
                    launchIntent.setComponent(new ComponentName(pkgName, activityName));
                    AppInfo appInfo = new AppInfo();
                    appInfo.setAppLabel(appLabel);
                    appInfo.setPkgName(pkgName);
                    appInfo.setDate(systemDate);
                    appInfo.setAppIcon(icon);
                    appInfo.setIntent(launchIntent);
                    mlistAppInfo.add(appInfo);
                }
            }
        }

        public void queryCommonlyUsedSoftware() {
            Cursor c = mdb.rawQuery("select distinct * from perpo", new String[] {});
            while (c.moveToNext()) {
                int number = c.getInt(c.getColumnIndex("int"));
                if (number != 0) {
                    mListViewOpen = true;
                }
            }
            if (mListViewOpen) {
                mlistViewAppInfo = new ArrayList<AppInfo>();
                Cursor cs = mdb.rawQuery("select distinct * from perpo", new String[] {});
                while (cs.moveToNext()) {
                    String label = cs.getString(cs.getColumnIndex("label"));
                    String pkgName = cs.getString(cs.getColumnIndex("pkname"));
                    String stringDate = cs.getString(cs.getColumnIndex("date"));
                    Drawable icon = null;
                    Date date = null;
                    try {
                        icon = getPackageManager().getApplicationIcon(pkgName);
                        date = ConverToDate(stringDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int number = cs.getInt(cs.getColumnIndex("int"));
                    if (number > 0) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(pkgName);
                        AppInfo appInfo = new AppInfo();
                        appInfo.setAppLabel(label);
                        appInfo.setPkgName(pkgName);
                        appInfo.setDate(date);
                        appInfo.setAppIcon(icon);
                        appInfo.setNumber(number);
                        appInfo.setIntent(intent);
                        mlistViewAppInfo.add(appInfo);
                    }
                }

                Collections.sort(mlistViewAppInfo, new Comparator<AppInfo>() {
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        Double rScore = (double) rhs.getNumber();
                        Double iScore = (double) lhs.getNumber();
                        return (rScore.compareTo(iScore));
                    }
                });

                mUsuallyAdapter = new StartupMenuUsuallyAdapter(StartupMenuActivity.this,
                                                                mlistViewAppInfo);
                mListView.setAdapter(mUsuallyAdapter);
                mListView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        String pkgName = mlistViewAppInfo.get(position).getPkgName();
                        Intent intent = mlistViewAppInfo.get(position).getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        doUpdate(pkgName);
                    }
                });
            }
        }

        public void BackstageRenewalData() {
            PackageManager pm = this.getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
            Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
            for (ResolveInfo reInfo : resolveInfos) {
                File file = new File(reInfo.activityInfo.applicationInfo.sourceDir);
                Date systemDate = new Date(file.lastModified());
                ApplicationInfo applicationInfo = reInfo.activityInfo.applicationInfo;
                String activityName = reInfo.activityInfo.name;
                String pkgName = reInfo.activityInfo.packageName;
                String appLabel = (String) reInfo.loadLabel(pm);
                Drawable icon = reInfo.loadIcon(pm);
                mIsHasReayDb = false;
                Cursor c = mdb.rawQuery("select * from perpo where pkname = ?",
                                        new String[] { pkgName });
                while (c.moveToNext()) {
                    String pkname = c.getString(c.getColumnIndex("pkname"));
                    if (pkgName.equals(pkname)) {
                        mIsHasReayDb = true;
                        break;
                    }
                }

                if (!mIsHasReayDb) {
                    mdb.execSQL("insert into perpo(label,pkname,date,int) "
                                + "values (?,?,?,?)",
                                new Object[] { appLabel, pkgName, systemDate,
                                              mNumber});
                }
                if(isEnglish(appLabel)) {
                    ContentValues contentvalues = new ContentValues();
                    contentvalues.put("label", appLabel);
                    mdb.update("perpo", contentvalues, "pkname = ?", new String[]{ pkgName });
                } else {
                    ContentValues contentvalues = new ContentValues();
                    contentvalues.put("label", appLabel);
                    mdb.update("perpo", contentvalues, "pkname = ?", new String[]{ pkgName });
                }
            }
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((actionId == EditorInfo.IME_ACTION_SEND)
                     || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                mEtext = mEditText.getText().toString().trim();
                mlistAppInfo.clear();
                mBrowseAppAdapter.notifyDataSetChanged();
                mlistAppInfo = new ArrayList<AppInfo>();
                querySqlAppinfo();
                mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this, mlistAppInfo);
                gv_view.setAdapter(mBrowseAppAdapter);
                return true;
            }
            return false;
        }

        private TextWatcher watcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                     int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                mEtext = mEditText.getText().toString().trim();
                mlistAppInfo.clear();
                mBrowseAppAdapter.notifyDataSetChanged();
                mlistAppInfo = new ArrayList<AppInfo>();
                querySqlAppinfo();
                mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this, mlistAppInfo);
                gv_view.setAdapter(mBrowseAppAdapter);
            }
        };

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
                try {
                    String cmd = "/system/xbin/echo mem > /sys/power/state";
                    Runtime.getRuntime().exec(new String[] {"/system/bin/su", "-c", cmd});
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.lock:
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                }
                Intent intentLock = new Intent("android.intent.action.LOCKNOW");
                intentLock.addFlags(Intent.FLAG_RUN_FULLSCREEN | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentLock);
                break;
            case R.id.iv_view:
                mEtext = mEditText.getText().toString().trim();
                mlistAppInfo.clear();
                mBrowseAppAdapter.notifyDataSetChanged();
                mlistAppInfo = new ArrayList<AppInfo>();
                querySqlAppinfo();
                mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this, mlistAppInfo);
                gv_view.setAdapter(mBrowseAppAdapter);
                break;
            case R.id.name_sort:
                mPopupWindow.dismiss();
                mEditText.setText("");
                nameSort();
                break;
            case R.id.time_sort:
                mPopupWindow.dismiss();
                mEditText.setText("");
                timeSort();
                break;
            /*case R.id.type_sort:
                mPopupWindow.dismiss();
                if (CLICKS == 3) {
                    CLICKS = 0;
                }
                CLICKS++;
                mEditText.setText("");
                mlistAppInfo.clear();
                mBrowseAppAdapter.notifyDataSetChanged();
                typeSort(CLICKS);
                break;*/
            case R.id.click_sort:
                mPopupWindow.dismiss();
                mEditText.setText("");
                clickSort();
                break;
            }
        }

        private void nameSort() {
            mlistAppInfo.clear();
            mBrowseAppAdapter.notifyDataSetChanged();
            mlistAppInfo = new ArrayList<AppInfo>();
            queryAppInfo();
            mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this, mlistAppInfo);
            gv_view.setAdapter(mBrowseAppAdapter);
        }

        private void timeSort() {
            mlistAppInfo.clear();
            mBrowseAppAdapter.notifyDataSetChanged();
            mlistAppInfo = new ArrayList<AppInfo>();
            queryAppInfo();
            timeAlgorithm();
            mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this, mlistAppInfo);
            gv_view.setAdapter(mBrowseAppAdapter);
        }

        private void typeSort(int a) {
            PackageManager pm = this.getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
            Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
            if (mlistAppInfo != null) {
                mlistAppInfo.clear();
                for (ResolveInfo reInfo : resolveInfos) {
                    ApplicationInfo applicationInfo = reInfo.activityInfo.applicationInfo;
                    if (a == FILTER_ALL_APP ) {
                        appData(pm, reInfo);
                    } else if (a == FILTER_SYSYTEM_APP && isSystemApp(applicationInfo)) {
                        appData(pm, reInfo);
                    } else if (a == FILTER_THIRD_APP && !isSystemApp(applicationInfo)) {
                        appData(pm, reInfo);
                        if (CLICKS == 3) {
                            CLICKS = 0;
                        }
                    }
                }
            }
            mBroAdapter = new StartupMenuAdapter(this, mlistAppInfo);
            gv_view.setAdapter(mBroAdapter);
        }

        private void clickSort() {
            mlistAppInfo.clear();
            mBrowseAppAdapter.notifyDataSetChanged();
            mlistAppInfo = new ArrayList<AppInfo>();
            querySqlAppinfo();
            Collections.sort(mlistAppInfo, new Comparator<AppInfo>() {
                public int compare(AppInfo lhs, AppInfo rhs) {
                    Double rScore = (double) rhs.getNumber();
                    Double iScore = (double) lhs.getNumber();
                    return (rScore.compareTo(iScore));
                }
            });
            mBroAdapter = new StartupMenuAdapter(this, mlistAppInfo);
            gv_view.setAdapter(mBroAdapter);
        }

        private void querySqlAppinfo() {
            Cursor c = mdb.rawQuery("select distinct * from perpo", new String[] {});
            while (c.moveToNext()) {
                String label = c.getString(c.getColumnIndex("label"));
                String  pkgName = c.getString(c.getColumnIndex("pkname"));
                String stringDate = c.getString(c.getColumnIndex("date"));
                Drawable icon = null;
                Date date = null;
                try {
                    icon = getPackageManager().getApplicationIcon(pkgName);
                    date = ConverToDate(stringDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int number = c.getInt(c.getColumnIndex("int"));
                if(label.toLowerCase().indexOf(mEtext.toLowerCase()) != -1) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(pkgName);
                    AppInfo appInfo = new AppInfo();
                    appInfo.setAppLabel(label);
                    appInfo.setPkgName(pkgName);
                    appInfo.setDate(date);
                    appInfo.setAppIcon(icon);
                    appInfo.setNumber(number);
                    appInfo.setIntent(intent);
                    mlistAppInfo.add(appInfo);
                } else if(TextUtils.isEmpty(mEtext)) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(pkgName);
                    AppInfo appInfo = new AppInfo();
                    appInfo.setAppLabel(label);
                    appInfo.setPkgName(pkgName);
                    appInfo.setDate(date);
                    appInfo.setAppIcon(icon);
                    appInfo.setNumber(number);
                    appInfo.setIntent(intent);
                    mlistAppInfo.add(appInfo);
                }
            }
        }

        public static boolean isEnglish(String str) {
            return str.matches("^[a-zA-Z]*");
        }

        public static Date ConverToDate(String StrDate) throws Exception {
            DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
            return df.parse(StrDate);
        }

        private void timeAlgorithm() {
            Collections.sort(mlistAppInfo, new Comparator<Object>() {
                public int compare(Object lhs, Object rhs) {
                    AppInfo p1 = (AppInfo) lhs;
                    AppInfo p2 = (AppInfo) rhs;
                    return p2.getDate().compareTo(p1.getDate());
                }
            });
        }

        public void appData(PackageManager pm, ResolveInfo reInfo) {
            File file = new File(reInfo.activityInfo.applicationInfo.sourceDir);
            Date systemDate = new Date(file.lastModified());
            String activityName = reInfo.activityInfo.name;
            String pkgName = reInfo.activityInfo.packageName;
            String appLabel = (String) reInfo.loadLabel(pm);
            Drawable icon = reInfo.loadIcon(pm);
            Intent launchIntent = new Intent();
            launchIntent.setComponent(new ComponentName(pkgName, activityName));
            AppInfo appInfo = new AppInfo();
            appInfo.setAppLabel(appLabel);
            appInfo.setPkgName(pkgName);
            appInfo.setDate(systemDate);
            appInfo.setAppIcon(icon);
            appInfo.setIntent(launchIntent);
            mlistAppInfo.add(appInfo);
        }

        private boolean isSystemApp(ApplicationInfo applicationInfo) {
            return (applicationInfo.flags & applicationInfo.FLAG_SYSTEM) > 0;
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String pkgName = mlistAppInfo.get(position).getPkgName();
            Intent intent = mlistAppInfo.get(position).getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            doUpdate(pkgName);
        }

        private void doUpdate(String pkgName) {
            Cursor c = mdb.rawQuery("select * from perpo where pkname = ?", new String[] { pkgName });
            c.moveToNext();
            int numbers = c.getInt(c.getColumnIndex("int"));
            numbers++;
            ContentValues values = new ContentValues();
            values.put("int", numbers);
            mdb.update("perpo", values, "pkname = ?", new String[] { pkgName });
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

            mPopupWindow = new PopupWindow(layout, 100, 200);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

            int[] location = new int[2];
            v.getLocationOnScreen(location);
            //popupWindow.showAtLocation(v, Gravity.NO_GRAVITY,location[0] + v.getWidth(), location[1]);
            mPopupWindow.showAtLocation(v, Gravity.RIGHT, 360, 200);
	}

        public void SortShow(View v) {
            LinearLayout layout = new LinearLayout(StartupMenuActivity.this);
            layout.setBackgroundColor(Color.WHITE);
            View tv = LayoutInflater.from(StartupMenuActivity.this).inflate(
                                                              R.layout.showsort_activity, null);
            tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                               LayoutParams.WRAP_CONTENT));
            TextView click_sort = (TextView) tv.findViewById(R.id.click_sort);
            TextView time_sort = (TextView) tv.findViewById(R.id.time_sort);
            TextView name_sort = (TextView) tv.findViewById(R.id.name_sort);
            //TextView type_sort = (TextView) tv.findViewById(R.id.type_sort);
            click_sort.setOnClickListener(this);
            time_sort.setOnClickListener(this);
            name_sort.setOnClickListener(this);
            //type_sort.setOnClickListener(this);
            layout.addView(tv);

            mPopupWindow = new PopupWindow(layout, 130, 65);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

            int[] location = new int[2];
            v.getLocationOnScreen(location);
            //popupWindow.showAtLocation(v, Gravity.NO_GRAVITY,location[0] + v.getWidth(), location[1]);
            mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 190, 907);
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
