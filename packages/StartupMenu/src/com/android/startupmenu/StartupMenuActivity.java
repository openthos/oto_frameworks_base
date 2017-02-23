/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
 * See the License for the specific language governing permissions and
 *
 */

package com.android.startupmenu;

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
import java.util.HashMap;
import java.util.Map;

import com.android.startupmenu.R;
import com.android.startupmenu.util.AppInfo;
import com.android.startupmenu.util.MySqliteOpenHelper;
import com.android.startupmenu.dialog.BaseSettingDialog;
import com.android.startupmenu.dialog.StartMenuDialog;
import com.android.startupmenu.dialog.StartMenuUsuallyDialog;
import com.android.startupmenu.adapter.StartupMenuAdapter;
import com.android.startupmenu.adapter.StartupMenuUsuallyAdapter;

import android.content.ActivityNotFoundException;
import android.content.pm.ApplicationInfo;
import android.util.Slog;
import android.R.layout;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.app.Dialog;
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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Window;
import android.view.Display;
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
import android.view.MotionEvent;
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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.IntentFilter;
import android.widget.Toast;
import android.os.Bundle;

import java.util.Comparator;
import java.text.Collator;
import java.io.UnsupportedEncodingException;

/*
 * Annotation
 *
 * File name :  StartupMenuActivity
 * Author    :  Systemui group
 * Date      :  2016-10-11
 *
 * Describe
 *    The original Documentui start menu
 *    On October 11, 2016 amended as separate modules
 *
 */

public class StartupMenuActivity extends Activity implements OnClickListener,
                 OnEditorActionListener, View.OnHoverListener {

        public static final int FILTER_ALL_APP = 1;
        public static final int FILTER_SYSYTEM_APP = 2;
        public static final int FILTER_THIRD_APP = 3;
        public static final int EDITTEXT_LENGTH_MAX = 10;

        public static StartMenuDialog mStartMenuDialog;
        public static StartMenuUsuallyDialog mStartMenuUsuallyDialog;
        public static ArrayList<AppInfo> mlistAppInfo = null;
        public static StartupMenuActivity StartupMenuActivity;
        public static ArrayList<AppInfo> mlistViewAppInfo = null;
        private Map<Integer, Boolean> isCheckedMap = null;

        private Context mContext;
        private PopupWindow mPopupWindow;
        private StartupMenuAdapter mBrowseAppAdapter, mBroAdapter;
        public static StartupMenuUsuallyAdapter mUsuallyAdapter;
        private MySqliteOpenHelper mMsoh;
        private SQLiteDatabase mdb;
        BaseSettingDialog mPowerSourceDialog;
        BaseSettingDialog targetDialog;

        private int mNumber;
        private int CLICKS = 0;
        private boolean mListViewOpen = false;
        private boolean mIsHasReayDb;
        private static boolean mFocus;
        private String mEtext;

        private GridView gv_view;
        private ListView mListView;
        private EditText mEditText;
        private View  my_computer;
        private ImageView mIvArrowGray;
        private TextView mTvSortShow;
        private LinearLayout mIvArrowWhite;
        public static List<AppInfo> mListViewEight;
        private TextView mClickSort;
        private TextView mTimeSort;
        private TextView mNameSort;
        private int mClickSortStatus = 1;
        private int mTimeSortStatus = 1;
        private int mNameSortStatus = 1;
        private boolean mOnlyNameSort = false;
        private LinearLayout mSelectLayout;
        private LinearLayout mSortClickView;
        private View mSelectView;
        private SharedPreferences sharedPreference;
        private String mNameSortText;
        private String mTimeSortText;
        private String mClickSortText;
        private String mType;
        private int mOrder;
        private Handler mHandler;
        private int mFinishFlag = 0;
        private int mGetValueFlag = 1;
        private int mGridViewFlag = 2;
        private int mIsClick;
        private int mStrCount;
        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            System.exit(0);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                                 WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                                 WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
            getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
            setContentView(R.layout.start_activity);
            mContext=this;

            mMsoh = new MySqliteOpenHelper(StartupMenuActivity.this,
                                           "Application_database.db", null, 1);
            mdb = mMsoh.getWritableDatabase();
            sharedPreference = getSharedPreferences("click", Context.MODE_PRIVATE);

            gv_view = (GridView) findViewById(R.id.gv_view);
            mListView = (ListView) findViewById(R.id.lv_view);
            StartupMenuActivity.this.setFinishOnTouchOutside(true);
            mlistAppInfo = new ArrayList<AppInfo>();
            isCheckedMap = new HashMap<Integer, Boolean>();
            mBrowseAppAdapter = new StartupMenuAdapter(this, mlistAppInfo ,isCheckedMap);
            //gv_view.setOnItemClickListener(this);

            mSortClickView = (LinearLayout) findViewById(R.id.sort_click_view);
            TextView system_setting = (TextView) findViewById(R.id.system_setting);
            my_computer = (TextView) findViewById(R.id.my_computer);
            TextView powerOff = (TextView) findViewById(R.id.power_off);
            mStartMenuDialog = new StartMenuDialog(this, R.style.dialog);
            mStartMenuUsuallyDialog = new StartMenuUsuallyDialog(this, R.style.dialog);
            my_computer.setOnClickListener(this);
            system_setting.setOnClickListener(this);
            my_computer.setOnHoverListener(hoverListener);
            system_setting.setOnHoverListener(hoverListener);
            powerOff.setOnHoverListener(hoverListener);

            ImageView imView = (ImageView) findViewById(R.id.iv_view);
            mIvArrowGray = (ImageView) findViewById(R.id.iv_arrow_gray);
            mTvSortShow = (TextView) findViewById(R.id.tv_sort_show);
            mEditText = (EditText) findViewById(R.id.et_text);
            mIvArrowWhite = (LinearLayout) findViewById(R.id.iv_arrow_white);
            mIvArrowWhite.setOnClickListener(this);
            mTvSortShow.setOnClickListener(this);
            imView.setOnClickListener(this);
            mEditText.setOnEditorActionListener(this);
            mEditText.addTextChangedListener(watcher);

            mTvSortShow.setText("");
            mIvArrowGray.setImageResource(R.drawable.ic_start_menu_down_arrow);
            mHandler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == mFinishFlag) {
                        mUsuallyAdapter = new StartupMenuUsuallyAdapter(StartupMenuActivity.this,
                                                                mListViewEight);
                        mListView.setAdapter(mUsuallyAdapter);
                    } else if (msg.what == mGetValueFlag) {
                        selectAppShow();
                        queryCommonlyUsedSoftware();
                    } else if (msg.what == mGridViewFlag) {
                        mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this,
                                                                   mlistAppInfo ,isCheckedMap);
                        gv_view.setAdapter(mBrowseAppAdapter);
                    }
                }
            };
            new mThread().start();
            initSelectLayout();
            // selectAppShow();
            LinearLayout ll = (LinearLayout) findViewById(R.id.ll_layout);
            ll.setOnHoverListener(this);
            mFocus = false;

            showStatusBar();
        }

        private void showStatusBar() {
            Intent intent = new Intent();
            intent.setAction(Intent.STATUS_BAR_SHOW_SUGGEST);
            sendBroadcast(intent);
        }

        @Override
        public boolean onHover(View view, MotionEvent motionEvent) {
            int what = motionEvent.getAction();
            int isSql = sharedPreference.getInt("isSql", 0);
            switch(what) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    if (mIsClick == 0) {
                        if (isSql == 0) {
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_STARTMENU_SEND_SQLITE_INFO);
                            sendBroadcast(i);
                            SharedPreferences.Editor edit = sharedPreference.edit();
                            edit.putInt("isSql", 1);
                            edit.commit();
                        }
                    }
                    break;
            }
            return false;
        }

        public void initSelectLayout() {
            mSelectLayout =  new LinearLayout(StartupMenuActivity.this);
            mSelectLayout.setBackgroundColor(Color.WHITE);
            mSelectView = LayoutInflater.from(StartupMenuActivity.this).inflate(
                                         R.layout.showsort_activity, null);
            mSelectView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                                            LayoutParams.WRAP_CONTENT));
            mClickSort = (TextView) mSelectView.findViewById(R.id.click_sort);
            mTimeSort = (TextView) mSelectView.findViewById(R.id.time_sort);
            mNameSort = (TextView) mSelectView.findViewById(R.id.name_sort);

            mClickSortText = mClickSort.getText().toString();
            mTimeSortText = mTimeSort.getText().toString();
            mNameSortText = mNameSort.getText().toString();
            //TextView type_sort = (TextView) tv.findViewById(R.id.type_sort);

            mClickSort.setOnClickListener(this);
            mTimeSort.setOnClickListener(this);
            mNameSort.setOnClickListener(this);
            //type_sort.setOnClickListener(this);
            mClickSort.setOnHoverListener(hoverListeners);
            mTimeSort.setOnHoverListener(hoverListeners);
            mNameSort.setOnHoverListener(hoverListeners);
            mSelectLayout.addView(mSelectView);
        }

        View.OnHoverListener hoverListeners= new View.OnHoverListener() {
            public boolean onHover(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        v.setBackgroundResource(R.color.rightMenuFocus);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        v.setBackgroundResource(R.color.showSortBackground);
                        break;
                }
                return false;
            }
        };

        public void selectAppShow() {
            if (mType.equals("sortName")) {
                mTvSortShow.setText(R.string.name_sort);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
                nameSort();
            } else if (mType.equals(mNameSortText) && mOrder == 1) {
                mTvSortShow.setText(R.string.name_sort);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
                nameSort();
            } else if (mType.equals(mNameSortText) && mOrder == -1) {
                mTvSortShow.setText(R.string.name_sort);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_down_arrow);
                mNameSortStatus = -1;
                mOnlyNameSort = true;
                nameSort();
            } else if (mType.equals(mTimeSortText) && mOrder == 1) {
                mTvSortShow.setText(R.string.time_sort);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
                timeSort();
            } else if (mType.equals(mTimeSortText) && mOrder == -1) {
                mTvSortShow.setText(R.string.time_sort);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_down_arrow);
                mTimeSortStatus = -1;
                timeSort();
            } else if (mType.equals(mClickSortText) && mOrder == 1) {
                mTvSortShow.setText(R.string.click_sort);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
                mEditText.setText("");
                clickSort();
            } else if (mType.equals(mClickSortText) && mOrder == -1) {
                mTvSortShow.setText(R.string.click_sort);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_down_arrow);
                mEditText.setText("");
                mClickSortStatus=-1;
                clickSort();
            } else {
                queryAppInfo();
                mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this,
                                                                   mlistAppInfo ,isCheckedMap);
                gv_view.setAdapter(mBrowseAppAdapter);
            }
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

        class mThread extends Thread {
            public void run(){
                mIsClick = sharedPreference.getInt("isClick", 0);
                mType = sharedPreference.getString("type", "sortName");
                mOrder = sharedPreference.getInt("order", 0);
                if (mIsClick == 1) {
                    Message m = new Message();
                    m.what = mGetValueFlag;
                    mHandler.sendMessage(m);
                    mListViewOpen = true;
                } else {
                    queryAppInfo();
                    Message msg = new Message();
                    msg.what = mGridViewFlag;
                    mHandler.sendMessage(msg);
                }
            }
        }

        public void queryAppInfo() {
            PackageManager pm = this.getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
            // Sort to resolveInfos :Matthew
            List<ResolveInfo> listEnglish = new ArrayList<>();
            List<ResolveInfo> listChina = new ArrayList<>();
            List<ResolveInfo> listNumber = new ArrayList<>();
            for (ResolveInfo info : resolveInfos) {
                String str = info.loadLabel(pm).toString().trim();
                int ch = str.charAt(0);
                if (ch >= '0' && ch <= '9') {
                    listNumber.add(info);
                } else {
                    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
                        listEnglish.add(info);
                    } else {
                        listChina.add(info);
                    }
                }
            }
            Collections.sort(listEnglish, new ResolveInfo.DisplayNameComparator(pm));
            Collections.sort(listChina, new ResolveInfo.DisplayNameComparator(pm));
            Collections.sort(listNumber, new ResolveInfo.DisplayNameComparator(pm));
            resolveInfos.clear();
            for (ResolveInfo number : listNumber) {
                resolveInfos.add(number);
            }
            for (ResolveInfo english : listEnglish) {
                resolveInfos.add(english);
            }
            for (ResolveInfo china : listChina) {
                resolveInfos.add(china);
            }
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
            if (!TextUtils.isEmpty(mEtext)) {
                List<AppInfo> list = new ArrayList<>();
                for (AppInfo app : mlistAppInfo) {
                    if (app.getAppLabel().toLowerCase().indexOf(mEtext.toLowerCase()) != -1) {
                        list.add(app);
                    }
                }
                mlistAppInfo.clear();
                mlistAppInfo.addAll(list);
            }

        }

        // Used left numbers
        public void queryCommonlyUsedSoftware() {
            if (mListViewOpen) {
                mlistViewAppInfo = new ArrayList<AppInfo>();
                Cursor cs = mdb.rawQuery("select distinct * from perpo", new String[] {});
                while (cs.moveToNext()) {
                    String pkgName = cs.getString(cs.getColumnIndex("pkname"));
                    String label = "application";
                    for (AppInfo info : mlistAppInfo) {
                        if (info.getPkgName().equals(pkgName)) {
                            label = info.getAppLabel();
                        }
                    }
                    String stringDate = cs.getString(cs.getColumnIndex("date"));
                    Drawable icon = null;
                    Date date = null;
                    try {
                        icon = getPackageManager().getApplicationIcon(pkgName);
                        date = ConverToDate(stringDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int number = cs.getInt(cs.getColumnIndex("click"));
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
                mListViewEight = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    if (i >= mlistViewAppInfo.size()) {
		        break;
                    }
                    AppInfo appInfo = mlistViewAppInfo.get(i);
                    mListViewEight.add(appInfo);
                }
                //selectAppShow();
                Message m = new Message();
                m.what = mFinishFlag;
                mHandler.sendMessage(m);
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
                querySort();
                isCheckedMap = new HashMap<Integer, Boolean>();
                mBrowseAppAdapter = new StartupMenuAdapter(this, mlistAppInfo ,isCheckedMap);
                //mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this,
                //                                            mlistAppInfo);
                gv_view.setAdapter(mBrowseAppAdapter);
                return true;
            }
            return false;
        }

        private TextWatcher watcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mStrCount = before + count;
                if (mStrCount > EDITTEXT_LENGTH_MAX) {
                    mEditText.setSelection(mEditText.length());
                }
                try {
                     mStrCount = mEditText.getText().toString().getBytes("GBK").length;
                } catch (UnsupportedEncodingException e) {
                     e.printStackTrace();
                }
            }

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
                querySort();
                //mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this,
                //                                           mlistAppInfo);
                isCheckedMap = new HashMap<Integer, Boolean>();
                mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this,
                                                           mlistAppInfo ,isCheckedMap);
                gv_view.setAdapter(mBrowseAppAdapter);
                if (mStrCount > EDITTEXT_LENGTH_MAX) {
                    CharSequence subSequence = null;
                    for (int i = 0; i < s.length(); i++) {
                        subSequence = s.subSequence(0, i);
                        try {
                            if (subSequence.toString().getBytes("GBK").length == mStrCount) {
                                mEditText.setText(subSequence.toString());
                                break;
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    mEditText.setText(subSequence.toString());
                }

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
                    if (packName.compareTo(ApplicationInfo.APPNAME_OTO_FILEMANAGER) == 0) {
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
            case R.id.iv_view:
                mEtext = mEditText.getText().toString().trim();
                mlistAppInfo.clear();
                mBrowseAppAdapter.notifyDataSetChanged();
                mlistAppInfo = new ArrayList<AppInfo>();
                querySqlAppinfo();
                querySort();
                //mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this,
                //                                           mlistAppInfo);
                isCheckedMap = new HashMap<Integer, Boolean>();
                mBrowseAppAdapter = new StartupMenuAdapter(this, mlistAppInfo ,isCheckedMap);
                gv_view.setAdapter(mBrowseAppAdapter);
                break;
            case R.id.tv_sort_show:
                //sortShow();
                selectShow(v);
                break;
            case R.id.name_sort:
                mEditText.setText("");
                mTvSortShow.setText(R.string.name_sort);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
                mPopupWindow.dismiss();
                nameSort();
                break;
            case R.id.time_sort:
                mEditText.setText("");
                mTvSortShow.setText(R.string.time_sort);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
                mPopupWindow.dismiss();
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
                mTvSortShow.setText(R.string.click_sort);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
                mPopupWindow.dismiss();
                mEditText.setText("");
                clickSort();
                break;
            case R.id.iv_arrow_white:
                mFocus = true;
                sortShow();
                break;
            }
        }

        private void nameSort() {
            queryAppInfo();
            SharedPreferences.Editor edit = sharedPreference.edit();
            edit.putString("type", mNameSortText);
            edit.putInt("order", 1);
            edit.commit();
            if (mNameSortStatus == -1 && mOnlyNameSort) {
                Collections.reverse(mlistAppInfo);
                mOnlyNameSort = false;
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_down_arrow);
                edit.putString("type", mNameSortText);
                edit.putInt("order", -1);
                edit.commit();
            }

            //mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this, mlistAppInfo);
            isCheckedMap = new HashMap<Integer, Boolean>();
            mBrowseAppAdapter = new StartupMenuAdapter(this, mlistAppInfo ,isCheckedMap);
            gv_view.setAdapter(mBrowseAppAdapter);
        }

        private void timeSort() {
            mlistAppInfo.clear();
            mBrowseAppAdapter.notifyDataSetChanged();
            mlistAppInfo = new ArrayList<AppInfo>();
            queryAppInfo();
            SharedPreferences.Editor edit = sharedPreference.edit();
            edit.putString("type", mTimeSortText);
            edit.putInt("order", 1);
            edit.commit();

            timeAlgorithm();
            //mBrowseAppAdapter = new StartupMenuAdapter(StartupMenuActivity.this, mlistAppInfo);
            isCheckedMap = new HashMap<Integer, Boolean>();
            mBrowseAppAdapter = new StartupMenuAdapter(this, mlistAppInfo ,isCheckedMap);
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
            //mBroAdapter = new StartupMenuAdapter(this, mlistAppInfo);
            isCheckedMap = new HashMap<Integer, Boolean>();
            mBroAdapter = new StartupMenuAdapter(this, mlistAppInfo ,isCheckedMap);
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
            SharedPreferences.Editor editor = sharedPreference.edit();
            editor.putString("type", mClickSortText);
            editor.putInt("order", 1);
            editor.commit();
            if (mClickSortStatus == -1) {
                Collections.reverse(mlistAppInfo);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_down_arrow);
                SharedPreferences.Editor edit = sharedPreference.edit();
                edit.putString("type", mClickSortText);
                edit.putInt("order", -1);
                edit.commit();
            }
            //mBroAdapter = new StartupMenuAdapter(this, mlistAppInfo);
            isCheckedMap = new HashMap<Integer, Boolean>();
            mBroAdapter = new StartupMenuAdapter(this, mlistAppInfo ,isCheckedMap);
            gv_view.setAdapter(mBroAdapter);
        }

        private void selectShow(View v) {
            mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
            if (v instanceof TextView) {
                TextView textView = (TextView) v;
                String textViewText = textView.getText().toString();
                if (textViewText.equals(mNameSortText)) {
                    mNameSortStatus = mNameSortStatus * -1;
                    mOnlyNameSort = true;
                    nameSort();
                } else if (textViewText.equals(mTimeSortText)) {
                    mTimeSortStatus = mTimeSortStatus * -1;
                    timeSort();
                } else if (textViewText.equals(mClickSortText)) {
                    mClickSortStatus = mClickSortStatus * -1;
                    clickSort();
                }
            }
        }

        private void querySqlAppinfo() {
            PackageManager pm = this.getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
            Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
            mlistAppInfo.clear();
            for (ResolveInfo reInfo : resolveInfos) {
                File file = new File(reInfo.activityInfo.applicationInfo.sourceDir);
                Date systemDate = new Date(file.lastModified());
                String activityName = reInfo.activityInfo.name;
                String pkgName = reInfo.activityInfo.packageName;
                String appLabel = (String) reInfo.loadLabel(pm);
                Drawable icon = reInfo.loadIcon(pm);
                Intent launchIntent = new Intent();
                launchIntent.setComponent(new ComponentName(pkgName, activityName));
                Cursor cursor = mdb.rawQuery("select * from perpo where pkname = ?",
                        new String[] { pkgName });
                cursor.moveToNext();
                if (cursor.moveToFirst()) {
                    int numbers = cursor.getInt(cursor.getColumnIndex("int"));
                    if (appLabel.toLowerCase().indexOf(mEtext.toLowerCase()) != -1
                            || TextUtils.isEmpty(mEtext)) {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(pkgName);
                        AppInfo appInfo = new AppInfo();
                        appInfo.setAppLabel(appLabel);
                        appInfo.setPkgName(pkgName);
                        appInfo.setDate(systemDate);
                        appInfo.setAppIcon(icon);
                        appInfo.setNumber(numbers);
                        appInfo.setIntent(intent);
                        mlistAppInfo.add(appInfo);
                    }
                }
            }
        }

        private void querySort() {
            String tvSortShow = mTvSortShow.getText().toString();
            if (tvSortShow.equals(mClickSortText)) {
                Collections.sort(mlistAppInfo, new Comparator<AppInfo>() {
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        Double rScore = (double) rhs.getNumber();
                        Double iScore = (double) lhs.getNumber();
                        return (rScore.compareTo(iScore));
                    }
                });
                if (mClickSortStatus == -1) {
                    Collections.reverse(mlistAppInfo);
                }
            } else if (tvSortShow.equals(mTimeSortText)) {
                Collections.sort(mlistAppInfo, new Comparator<Object>() {
                    public int compare(Object lhs, Object rhs) {
                        AppInfo p1 = (AppInfo) lhs;
                        AppInfo p2 = (AppInfo) rhs;
                        return p2.getDate().compareTo(p1.getDate());
                    }
                });
                if (mTimeSortStatus == -1) {
                    Collections.reverse(mlistAppInfo);
                }
            } else {
                queryAppInfo();
                if (mNameSortStatus == -1) {
                    Collections.reverse(mlistAppInfo);
                }
            }
        }

        public static boolean isEnglish(String str) {
            return str.matches("^[a-zA-Z]*");
        }

        public static Date ConverToDate(String StrDate) throws Exception {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
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
            if (mTimeSortStatus == -1) {
                Collections.reverse(mlistAppInfo);
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_down_arrow);
                SharedPreferences.Editor editor = sharedPreference.edit();
                editor.putString("type", mTimeSortText);
                editor.putInt("order", -1);
                editor.commit();
            }

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

        /*public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String pkgName = mlistAppInfo.get(position).getPkgName();
            Intent intent = mlistAppInfo.get(position).getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            doUpdate(pkgName);
        }*/

        public void doUpdate(String pkgName) {
            Cursor c = mdb.rawQuery("select * from perpo where pkname = ?", new String[] { pkgName });
            c.moveToNext();
            int numbers = c.getInt(c.getColumnIndex("int"));
            numbers++;
            ContentValues values = new ContentValues();
            values.put("int", numbers);
            mdb.update("perpo", values, "pkname = ?", new String[] { pkgName });
        }

        public void powerOff(View v) {
            ActivityManagerNative.callPowerSource(mContext);
            finish();
	}

        public void sortShow() {
            mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
            int mSortLyoutWidth = mContext.getResources()
                                          .getDimensionPixelSize(R.dimen.sort_layout_width);
            int mSortLyoutHeight = mContext.getResources()
                                          .getDimensionPixelSize(R.dimen.sort_layout_height);
            mPopupWindow = new PopupWindow(mSelectLayout, mSortLyoutWidth, mSortLyoutHeight);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

            int[] location = new int[2];
            mSortClickView.getLocationOnScreen(location);
            mPopupWindow.showAsDropDown(mSortClickView);
        }

        private void dismisTargetDialog(BaseSettingDialog newDialog){
            if(targetDialog != null) {
                targetDialog.dismiss();
            }
            targetDialog = newDialog;
        }

        public void killStartupMenu() {
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

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            outState.putParcelableArrayList("gridview", mlistAppInfo);
            outState.putParcelableArrayList("listview", mlistViewAppInfo);
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            if (!hasFocus) {
                if (!mFocus) {
                    finish();
                }
            }
            super.onWindowFocusChanged(hasFocus);
        }

        public static void setFocus(boolean hFocus) {
            mFocus = hFocus;
        }
}
