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

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.startupmenu.adapter.AppAdapter;
import com.android.startupmenu.bean.AppInfo;
import com.android.startupmenu.bean.Type;
import com.android.startupmenu.util.Constants;
import com.android.startupmenu.util.SqliteOpenHelper;
import com.android.startupmenu.util.TableIndexDefine;

import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.database.sqlite.SQLiteDatabase;
import android.content.SharedPreferences;

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

public class StartupMenuActivity extends Activity implements OnClickListener {

    public static final int NAME_SORT = 1;
    public static final int NAME_SORT_REVERSE = -1;
    public static final int TIME_SORT = 2;
    public static final int TIME_SORT_REVERSE = -2;
    public static final int CLICK_SORT = 3;
    public static final int CLICK_SORT_REVERSE = -3;

    private ArrayList<AppInfo> mAllAppInfos;
    private ArrayList<AppInfo> mDisplayAppInfos;
    public ArrayList<AppInfo> mCommonAppInfos;
    private List<String> mFilterAppPkgNames;

    private AppAdapter mGridAdapter;
    public AppAdapter mListAdapter;

    private boolean mFocus;

    private PopupWindow mPopupWindow;

    private LinearLayout mSelectLayout;
    private LinearLayout mSortClickView;
    private GridView mGridView;
    private ListView mListView;
    private EditText mEditText;
    private ImageView mIvArrowGray;
    private TextView mTvSortShow;
    private TextView mClickSort;
    private TextView mTimeSort;
    private TextView mNameSort;
    private View mSelectView;
    private SharedPreferences mSharedPreference;
    private int mType;
    private int mStrCount;
    private ImageView mSearch;
    private ImageView mArrowWhite;
    private TextView mFileManager;
    private TextView mPowerOff;
    private TextView mSystemSetting;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
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
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.gv_view);
        mListView = (ListView) findViewById(R.id.lv_usually_view);
        mSortClickView = (LinearLayout) findViewById(R.id.sort_click_view);
        mSearch = (ImageView) findViewById(R.id.iv_view_search);
        mIvArrowGray = (ImageView) findViewById(R.id.iv_arrow_gray);
        mTvSortShow = (TextView) findViewById(R.id.tv_sort_show);
        mEditText = (EditText) findViewById(R.id.et_text);
        mArrowWhite = (ImageView) findViewById(R.id.iv_arrow_white);
        mFileManager = (TextView) findViewById(R.id.openthos_file_manager);
        mPowerOff = (TextView) findViewById(R.id.power_off);
        mSystemSetting = (TextView) findViewById(R.id.system_setting);
    }

    private void initData() {
        mSharedPreference = getSharedPreferences("clicks", Context.MODE_PRIVATE);
        mAllAppInfos = new ArrayList<>();
        mDisplayAppInfos = new ArrayList<>();
        mCommonAppInfos = new ArrayList<>();

        mGridAdapter = new AppAdapter(this, Type.GRID, mDisplayAppInfos);
        mListAdapter = new AppAdapter(this, Type.LIST, mCommonAppInfos);
        mGridView.setAdapter(mGridAdapter);
        mListView.setAdapter(mListAdapter);

        initSelectLayout();

        mTvSortShow.setText("");
        mIvArrowGray.setImageResource(R.drawable.ic_start_menu_down_arrow);
        mType = mSharedPreference.getInt("sortType", NAME_SORT);
        mFocus = false;
        mFilterAppPkgNames = new ArrayList<>();
        String[] array = getResources().getStringArray(
                 com.android.internal.R.array.poor_quality_apps);
        mFilterAppPkgNames.addAll(Arrays.asList(array));

        showStatusBar();
        new Thread() {
            @Override
            public void run() {
                super.run();
                queryAppInfo();
                queryCommonAppInfo();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sortOrder();
                        mGridAdapter.notifyDataSetChanged();
                        mListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    public void initListener() {
        setFinishOnTouchOutside(true);

        mFileManager.setOnHoverListener(hoverListener);
        mSystemSetting.setOnHoverListener(hoverListener);
        mPowerOff.setOnHoverListener(hoverListener);

        mSystemSetting.setOnClickListener(this);
        mFileManager.setOnClickListener(this);
        mPowerOff.setOnClickListener(this);

        mArrowWhite.setOnClickListener(this);
        mTvSortShow.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        mEditText.addTextChangedListener(watcher);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            killStartupMenu();
        }
        // Delegate everything else to Activity.
        return super.onTouchEvent(event);
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

    View.OnHoverListener hoverListenerSort = new View.OnHoverListener() {
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

    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mStrCount = before + count;
            if (mStrCount > Constants.EDITTEXT_LENGTH_MAX) {
                mEditText.setSelection(mEditText.length());
            }
            try {
                mStrCount = mEditText.getText().toString().getBytes("GBK").length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            initDisplayAppInfos();
            sortOrder();
            mGridAdapter.notifyDataSetChanged();
            if (mStrCount > Constants.EDITTEXT_LENGTH_MAX) {
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
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            if (!mFocus) {
                finish();
            }
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openthos_file_manager:
                /* start FileManager */
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(Constants.APPNAME_OTO_FILEMANAGER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
            case R.id.system_setting:
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                break;
            case R.id.power_off:
                powerOff();
                break;
            case R.id.tv_sort_show:
                mType = mType * -1;
                sortOrder();
                mGridAdapter.notifyDataSetChanged();
                break;
            case R.id.name_sort:
                mEditText.setText("");
                mPopupWindow.dismiss();
                mType = NAME_SORT;
                sortOrder();
                mGridAdapter.notifyDataSetChanged();
                break;
            case R.id.time_sort:
                mEditText.setText("");
                mPopupWindow.dismiss();
                mType = TIME_SORT;
                sortOrder();
                mGridAdapter.notifyDataSetChanged();
                break;
            case R.id.click_sort:
                mPopupWindow.dismiss();
                mEditText.setText("");
                mType = CLICK_SORT;
                sortOrder();
                mGridAdapter.notifyDataSetChanged();
                break;
            case R.id.iv_arrow_white:
                mFocus = true;
                sortShow();
                break;
        }
    }

    /*
    * Choose 'install time' 'A - Z ' 'click numbers' to sort.
    * Follow up also needs to expand.
    */
    public void initSelectLayout() {
        mSelectLayout = new LinearLayout(StartupMenuActivity.this);
        mSelectLayout.setBackgroundColor(Color.WHITE);
        mSelectView = LayoutInflater.from(StartupMenuActivity.this).inflate(
                R.layout.showsort_activity, null);
        mSelectView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        // The frequency about running app.
        mClickSort = (TextView) mSelectView.findViewById(R.id.click_sort);
        mTimeSort = (TextView) mSelectView.findViewById(R.id.time_sort);
        mNameSort = (TextView) mSelectView.findViewById(R.id.name_sort);

        mClickSort.setOnClickListener(this);
        mTimeSort.setOnClickListener(this);
        mNameSort.setOnClickListener(this);

        mClickSort.setOnHoverListener(hoverListenerSort);
        mTimeSort.setOnHoverListener(hoverListenerSort);
        mNameSort.setOnHoverListener(hoverListenerSort);

        mSelectLayout.addView(mSelectView);
    }

    private void showStatusBar() {
        Intent intent = new Intent();
        intent.setAction(Constants.STATUS_BAR_SHOW_SUGGEST);
        sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void queryAppInfo() {
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        AppInfo appInfo;
        for (ResolveInfo reInfo : resolveInfos) {
            if (!isDisplayApp(reInfo.activityInfo.packageName)) {
                continue;
            }
            appInfo = new AppInfo();
            appInfo.setAppLabel((String) reInfo.loadLabel(pm));
            appInfo.setPkgName(reInfo.activityInfo.packageName);
            appInfo.setInstallTime(
                    new File(reInfo.activityInfo.applicationInfo.sourceDir).lastModified());
            appInfo.setAppIcon(reInfo.loadIcon(pm));
            appInfo.setActivityName(reInfo.activityInfo.name);
            appInfo.setClickCounts(0);
            mAllAppInfos.add(appInfo);
        }
        initDisplayAppInfos();
    }

    /* query sqlDataBase , sort by getClickCounts.
     * then show in left side listView.
     */
    private void queryCommonAppInfo() {
        SqliteOpenHelper sqliteOpenHelper = SqliteOpenHelper.getInstance(this);
        SQLiteDatabase db = sqliteOpenHelper.getWritableDatabase();
        List<AppInfo> tempAppInfo = new ArrayList<>();
        mCommonAppInfos.clear();
        Cursor cs = db.rawQuery("select distinct * from " + TableIndexDefine.TABLE_APP_PERPO
                + " order by " + TableIndexDefine.COLUMN_PERPO_CLICK_NUM + " desc", new String[]{});
        int i = 0;
        while (cs.moveToNext() && i < Constants.COMMON_APP_NUM) {
            String pkgName = cs.getString(cs.getColumnIndex(TableIndexDefine.COLUMN_PERPO_PKGNAME));
            int number = cs.getInt(cs.getColumnIndex(TableIndexDefine.COLUMN_PERPO_CLICK_NUM));
            if (number > 0) {
                for (AppInfo info : mAllAppInfos) {
                    if (info.getPkgName().equals(pkgName)) {
                        info.setClickCounts(number);
                        tempAppInfo.add(info);
                        break;
                    }
                }
            }
            i++;
        }
        cs.close();
        db.close();

        Collections.sort(tempAppInfo, new Comparator<AppInfo>() {
            public int compare(AppInfo lhs, AppInfo rhs) {
                int rhsNumber = rhs.getClickCounts();
                int lhsNumber = lhs.getClickCounts();
                if (rhsNumber > lhsNumber) {
                    return 1;
                } else if (lhsNumber == rhsNumber) {
                    return rhs.getInstallTime() > lhs.getInstallTime() ? 1 : -1;
                } else {
                    return -1;
                }
            }
        });
        mCommonAppInfos.addAll(
                tempAppInfo.subList(0, Math.min(tempAppInfo.size(), Constants.COMMON_APP_NUM)));
    }

    private void initDisplayAppInfos() {
        mDisplayAppInfos.clear();
        String searText = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(searText)) {
            mDisplayAppInfos.addAll(mAllAppInfos);
        } else {
            for (AppInfo appInfo : mAllAppInfos) {
                if (appInfo.getAppLabel().toLowerCase().contains(searText.toLowerCase())) {
                    mDisplayAppInfos.add(appInfo);
                }
            }
        }
    }

    private void sortOrder() {
        switch (mType) {
            case NAME_SORT:
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
                mTvSortShow.setText(R.string.name_sort);
                nameSort();
                break;
            case NAME_SORT_REVERSE:
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_down_arrow);
                mTvSortShow.setText(R.string.name_sort);
                nameSort();
                Collections.reverse(mDisplayAppInfos);
                break;
            case TIME_SORT:
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
                mTvSortShow.setText(R.string.time_sort);
                timeSort();
                break;
            case TIME_SORT_REVERSE:
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_down_arrow);
                mTvSortShow.setText(R.string.time_sort);
                timeSort();
                Collections.reverse(mDisplayAppInfos);
                break;
            case CLICK_SORT:
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_up_arrow);
                mTvSortShow.setText(R.string.click_sort);
                clickSort();
                break;
            case CLICK_SORT_REVERSE:
                mIvArrowGray.setImageResource(R.drawable.ic_start_menu_down_arrow);
                mTvSortShow.setText(R.string.click_sort);
                clickSort();
                Collections.reverse(mDisplayAppInfos);
                break;
            default:
                break;
        }
        saveSortTypeToSP(mType);
    }

    private void nameSort() {
        List<AppInfo> listEnglish = new ArrayList<>();
        List<AppInfo> listChina = new ArrayList<>();
        List<AppInfo> listNumber = new ArrayList<>();
        for (AppInfo appInfo : mDisplayAppInfos) {
            String appLabel = appInfo.getAppLabel();
            char ch = appLabel.charAt(0);
            if (ch >= '0' && ch <= '9') {
                listNumber.add(appInfo);
            } else {
                if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
                    listEnglish.add(appInfo);
                } else {
                    listChina.add(appInfo);
                }
            }
        }
        Collections.sort(listEnglish, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return o1.getAppLabel().compareTo(o2.getAppLabel());
            }
        });

        Collections.sort(listNumber, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return o1.getAppLabel().compareTo(o2.getAppLabel());
            }
        });
        final Collator collator = Collator.getInstance();
        Collections.sort(listChina, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return collator.getCollationKey(o1.getAppLabel()).
                        compareTo(collator.getCollationKey(o2.getAppLabel()));
            }
        });
        mDisplayAppInfos.clear();
        mDisplayAppInfos.addAll(listNumber);
        mDisplayAppInfos.addAll(listEnglish);
        mDisplayAppInfos.addAll(listChina);
    }

    private void timeSort() {
        Collections.sort(mDisplayAppInfos, new Comparator<AppInfo>() {
            public int compare(AppInfo lhs, AppInfo rhs) {
                return rhs.getInstallTime() > lhs.getInstallTime() ? 1 : -1;
            }
        });
    }

    private void clickSort() {
        Collections.sort(mDisplayAppInfos, new Comparator<AppInfo>() {
            public int compare(AppInfo lhs, AppInfo rhs) {
                int rhsNumber = rhs.getClickCounts();
                int lhsNumber = lhs.getClickCounts();
                if (rhsNumber > lhsNumber) {
                    return 1;
                } else if (lhsNumber == rhsNumber) {
                    return rhs.getInstallTime() > lhs.getInstallTime() ? 1 : -1;
                } else {
                    return -1;
                }
            }
        });
    }

    private void saveSortTypeToSP(int type) {
        SharedPreferences.Editor edit = mSharedPreference.edit();
        edit.putInt("sortType", type);
        edit.commit();
    }

    private boolean isDisplayApp(String pkgName) {
        for (int i = 0; i < mFilterAppPkgNames.size(); i++) {
            if (pkgName.equals(mFilterAppPkgNames.get(i))) {
                mFilterAppPkgNames.remove(i);
                return false;
            }
        }
        return true;
    }

    private void powerOff() {
        ActivityManagerNative.callPowerSource(this);
        finish();
    }

    public void killStartupMenu() {
        try {
            ActivityManagerNative.getDefault().killStartupMenu();
            finish();
        } catch (RemoteException e) {
        }
    }

    private void sortShow() {
        mPopupWindow = new PopupWindow(mSelectLayout,
                          LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        mPopupWindow.showAsDropDown(mSortClickView);
    }

    public void setFocus(boolean hFocus) {
        mFocus = hFocus;
    }
}
