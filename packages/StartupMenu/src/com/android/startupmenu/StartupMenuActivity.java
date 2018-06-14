package com.android.startupmenu;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.startupmenu.bean.AppInfo;
import com.android.startupmenu.bean.Type;
import com.android.startupmenu.listener.Callback;
import com.android.startupmenu.listener.OnClickCallback;
import com.android.startupmenu.listener.OnMenuClick;
import com.android.startupmenu.task.DataTask;
import com.android.startupmenu.util.Constants;
import com.android.startupmenu.util.SqliteOpenHelper;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ActivityManagerNative;

public class StartupMenuActivity extends Activity
        implements View.OnClickListener, View.OnHoverListener {

    public static final int DEFAULT_SORT = 0;
    public static final int NAME_SORT = 1;
    public static final int NAME_SORT_REVERSE = -1;
    public static final int TIME_SORT = 2;
    public static final int TIME_SORT_REVERSE = -2;
    public static final int CLICK_SORT = 3;
    public static final int CLICK_SORT_REVERSE = -3;

    public static final int LIST_APP_NUM = 8;

    private GridView mGridView;
    private ListView mListView;
    private LinearLayout mSortclickLayout;
    private ImageView mArrowDirect;
    private TextView mSortType;
    private EditText mSearch;
    private ImageView mArrowShow;
    private LinearLayout mFileManager;
    private LinearLayout mPowerOff;
    private LinearLayout mSetting;

    private SharedPreferences mSharedPreference;
    private ArrayList<AppInfo> mAllDatas;
    private ArrayList<AppInfo> mGridDatas;
    private ArrayList<AppInfo> mListDatas;
    private AppAdapter mGridAdapter;
    private AppAdapter mListAdapter;
    private MenuDialog mMenuDialog;
    private int mType;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        setContentView(R.layout.activity_startup_menu);
        setFinishOnTouchOutside(true);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.grid_view);
        mListView = (ListView) findViewById(R.id.list_view);
        mSortclickLayout = (LinearLayout) findViewById(R.id.sort_click_view);
        mArrowDirect = (ImageView) findViewById(R.id.arrow_direct);
        mSortType = (TextView) findViewById(R.id.sort_type);
        mSearch = (EditText) findViewById(R.id.search);
        mArrowShow = (ImageView) findViewById(R.id.arrow_show);
        mFileManager = (LinearLayout) findViewById(R.id.file_manager);
        mPowerOff = (LinearLayout) findViewById(R.id.power_off);
        mSetting = (LinearLayout) findViewById(R.id.system_setting);
    }

    private void initData() {
        mSharedPreference =
                getSharedPreferences("clicks", Context.MODE_PRIVATE);
        mAllDatas = new ArrayList<>();
        mGridDatas = new ArrayList<>();
        mListDatas = new ArrayList<>();

        mGridAdapter = new AppAdapter(this, Type.GRID, mGridDatas);
        mListAdapter = new AppAdapter(this, Type.LIST, mListDatas);
        mGridView.setAdapter(mGridAdapter);
        mListView.setAdapter(mListAdapter);
        mMenuDialog = MenuDialog.getInstance(this);

        mType = mSharedPreference.getInt("sortType", DEFAULT_SORT);
        init();

        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        installFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        installFilter.addDataScheme("package");
        registerReceiver(mInstallReceiver, installFilter);

        IntentFilter keyClickFilter = new IntentFilter();
        keyClickFilter.addAction("com.android.startupmenu.SQLITE_CHANGE");
        keyClickFilter.addAction(Constants.ACTION_SEND_CLICK_INFO);
        registerReceiver(mKeyClickReveiver, keyClickFilter);
    }

    public void init() {
        new DataTask(this, new Callback() {
            @Override
            public void callback(List<AppInfo> appInfos) {
                mAllDatas.clear();
                mAllDatas.addAll(appInfos);
                reloadGridAppInfos();
                reloadListAppInfos();
            }
        }).execute();
    }

    private void initListener() {
        mFileManager.setOnClickListener(this);
        mPowerOff.setOnClickListener(this);
        mSetting.setOnClickListener(this);

        mFileManager.setOnHoverListener(this);
        mPowerOff.setOnHoverListener(this);
        mSetting.setOnHoverListener(this);

        mSearch.addTextChangedListener(mTextWatcher);
        mSortclickLayout.setOnClickListener(this);
        mArrowShow.setOnClickListener(this);
        mMenuDialog.setOnMenuClick(mOnMenuClick);

        mGridView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                    mGridAdapter.exit();
                }
                return false;
            }
        });

        mListView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                    mListAdapter.exit();
                }
                return false;
            }
        });

        mListAdapter.setOnClickCallback(new OnClickCallback() {
            @Override
            public void open(AppInfo appInfo) {
                openApplication(appInfo);
            }

            @Override
            public void showDialog(int x, int y, AppInfo appInfo) {
                MenuDialog.getInstance(StartupMenuActivity.this).show(Type.LIST, appInfo, x, y);
            }
        });

        mGridAdapter.setOnClickCallback(new OnClickCallback() {
            @Override
            public void open(AppInfo appInfo) {
                openApplication(appInfo);
            }

            @Override
            public void showDialog(int x, int y, AppInfo appInfo) {
                MenuDialog.getInstance(StartupMenuActivity.this).show(Type.GRID, appInfo, x, y);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_manager:
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(Constants.APPNAME_OTO_FILEMANAGER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                dismiss();
                break;
            case R.id.system_setting:
                startActivity(new Intent(Settings.ACTION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                dismiss();
                break;
            case R.id.power_off:
                powerOff();
                dismiss();
                break;
            case R.id.sort_click_view:
                mType = mType * -1;
                sortOrder();
                break;
            case R.id.arrow_show:
                mMenuDialog.showSort(mArrowShow);
                break;
        }
    }

    private void powerOff() {
        ActivityManagerNative.callPowerSource(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            dismiss();
        }
        return super.onTouchEvent(event);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus && mMenuDialog != null && !mMenuDialog.isShowing()) {
            dismiss();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private void dismissMenuDialog() {
        if (mMenuDialog != null && mMenuDialog.isShowing()) {
            mMenuDialog.dismiss();
        }
    }

    public void dismiss() {
        dismissMenuDialog();
        try {
            ActivityManagerNative.getDefault().killStartupMenu();
            finish();
        } catch (RemoteException e) {
        }
    }

    private void reloadListAppInfos() {
        mListDatas.clear();
        Collections.sort(mAllDatas, new Comparator<AppInfo>() {
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
        int min = Math.min(mAllDatas.size(), LIST_APP_NUM);
        for (int i = 0; i < min; i++) {
            AppInfo appInfo = mAllDatas.get(i);
            if (appInfo.getClickCounts() != 0) {
                mListDatas.add(appInfo);
            } else {
                break;
            }
        }
        mListAdapter.notifyDataSetChanged();
    }

    private void reloadGridAppInfos() {
        mGridDatas.clear();
        String searchText = mSearch.getText().toString().trim();
        if (TextUtils.isEmpty(searchText)) {
            mGridDatas.addAll(mAllDatas);
        } else {
            for (AppInfo appInfo : mAllDatas) {
                if (appInfo.getAppLabel().toLowerCase().contains(searchText.toLowerCase())) {
                    mGridDatas.add(appInfo);
                }
            }
        }
        sortOrder();
    }

    private void sortOrder() {
        switch (mType) {
            case DEFAULT_SORT:
                mArrowDirect.setVisibility(View.GONE);
                mSortType.setText(R.string.default_sort);
                defaultSort();
                break;
            case NAME_SORT:
                mArrowDirect.setVisibility(View.VISIBLE);
                mArrowDirect.setImageResource(R.drawable.startmenu_up_arrow);
                mSortType.setText(R.string.name_sort);
                nameSort();
                break;
            case NAME_SORT_REVERSE:
                mArrowDirect.setVisibility(View.VISIBLE);
                mArrowDirect.setImageResource(R.drawable.startmenu_down_arrow);
                mSortType.setText(R.string.name_sort);
                nameSort();
                Collections.reverse(mGridDatas);
                break;
            case TIME_SORT:
                mArrowDirect.setVisibility(View.VISIBLE);
                mArrowDirect.setImageResource(R.drawable.startmenu_up_arrow);
                mSortType.setText(R.string.time_sort);
                timeSort();
                break;
            case TIME_SORT_REVERSE:
                mArrowDirect.setVisibility(View.VISIBLE);
                mArrowDirect.setImageResource(R.drawable.startmenu_down_arrow);
                mSortType.setText(R.string.time_sort);
                timeSort();
                Collections.reverse(mGridDatas);
                break;
            case CLICK_SORT:
                mArrowDirect.setVisibility(View.VISIBLE);
                mArrowDirect.setImageResource(R.drawable.startmenu_up_arrow);
                mSortType.setText(R.string.click_sort);
                clickSort();
                break;
            case CLICK_SORT_REVERSE:
                mArrowDirect.setVisibility(View.VISIBLE);
                mArrowDirect.setImageResource(R.drawable.startmenu_down_arrow);
                mSortType.setText(R.string.click_sort);
                clickSort();
                Collections.reverse(mGridDatas);
                break;
            default:
                break;
        }
        mGridAdapter.notifyDataSetChanged();
        mSharedPreference.edit().putInt("sortType", mType).commit();
    }

    private void defaultSort() {
        nameSort();
    }

    private void nameSort() {
        List<AppInfo> listEnglish = new ArrayList<>();
        List<AppInfo> listChina = new ArrayList<>();
        List<AppInfo> listNumber = new ArrayList<>();
        for (AppInfo appInfo : mGridDatas) {
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
        mGridDatas.clear();
        mGridDatas.addAll(listNumber);
        mGridDatas.addAll(listEnglish);
        mGridDatas.addAll(listChina);
    }

    private void timeSort() {
        Collections.sort(mGridDatas, new Comparator<AppInfo>() {
            public int compare(AppInfo lhs, AppInfo rhs) {
                if (rhs.getInstallTime() == lhs.getInstallTime()) {
                    return rhs.getPkgName().compareTo(lhs.getPkgName());
                }
                return rhs.getInstallTime() > lhs.getInstallTime() ? 1 : -1;
            }
        });
    }

    private void clickSort() {
        Collections.sort(mGridDatas, new Comparator<AppInfo>() {
            public int compare(AppInfo lhs, AppInfo rhs) {
                if (rhs.getClickCounts() == lhs.getClickCounts()) {
                    return rhs.getInstallTime() > lhs.getInstallTime() ? 1 : -1;
                }
                return rhs.getClickCounts() > lhs.getClickCounts() ? 1 : -1;
            }
        });
    }

    public AppInfo updateClickCount(String pkgName) {
        for (int i = 0; i < mAllDatas.size(); i++) {
            AppInfo appInfo = mAllDatas.get(i);
            if (pkgName.equals(appInfo.getPkgName())) {
                appInfo.updateClickCount();
                reloadListAppInfos();
                return appInfo;
            }
        }
        return null;
    }

    private void openApplication(AppInfo appInfo) {
        Intent intent = appInfo.getIntent(this);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Intent openAppIntent = new Intent();
        openAppIntent.setAction(Constants.ACTION_OPEN_APPLICATION);
        sendBroadcastAsUser(openAppIntent, UserHandle.ALL);
        SqliteOpenHelper.getInstance(this).updateDataStorage(updateClickCount(appInfo.getPkgName()));
        dismiss();
    }

    private void runPhoneMode(AppInfo appInfo) {
        Intent intent = appInfo.getIntent(this);
        intent.addFlags(Constants.FLAG_ACTIVITY_RUN_PHONE_MODE
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SqliteOpenHelper.getInstance(this).updateDataStorage(updateClickCount(appInfo.getPkgName()));
        dismiss();
    }

    private void runDesktopMode(AppInfo appInfo) {
        Intent intent = appInfo.getIntent(this);
        intent.addFlags(Constants.FLAG_ACTIVITY_RUN_PC_MODE
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SqliteOpenHelper.getInstance(this).updateDataStorage(updateClickCount(appInfo.getPkgName()));
        dismiss();
    }

    private void lockToTakbar(AppInfo appInfo) {
        Intent intentSend = new Intent();
        intentSend.putExtra("keyInfo", appInfo.getPkgName());
        intentSend.setAction(Constants.ACTION_STARTUPMENU_SEND_INFO_LOCK);
        sendBroadcast(intentSend);
    }

    private void unloackFromTaskbar(AppInfo appInfo) {
        Intent intentUnlock = new Intent();
        intentUnlock.putExtra("unlockapk", appInfo.getPkgName());
        intentUnlock.setAction(Constants.STARTMENU_UNLOCKED);
        sendBroadcast(intentUnlock);
    }

    private void uninstallApplication(AppInfo appInfo) {
        Uri uri = Uri.parse("package:" + appInfo.getPkgName());
        Intent intents = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intents);
        dismiss();
    }

    private void removeApplicaton(AppInfo appInfo) {
        String label = appInfo.getAppLabel();
        Toast.makeText(this,
                getString(R.string.remove_application) + appInfo.getAppLabel(),
                Toast.LENGTH_SHORT).show();
        mListDatas.remove(appInfo);
        mListAdapter.notifyDataSetChanged();
        SqliteOpenHelper.getInstance(this).deleteDataStorage(appInfo.getPkgName());
    }

    @Override
    protected void onDestroy() {
        if (mInstallReceiver != null) {
            unregisterReceiver(mInstallReceiver);
        }
        if (mKeyClickReveiver != null) {
            unregisterReceiver(mKeyClickReveiver);
        }
        dismissMenuDialog();
        super.onDestroy();
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            reloadGridAppInfos();
        }
    };

    private OnMenuClick mOnMenuClick = new OnMenuClick() {
        @Override
        public void menuClick(View view, Dialog dialog, AppInfo appInfo, String menu) {
            if (menu.equals(getString(R.string.open))) {
                openApplication(appInfo);
            } else if (menu.equals(getString(R.string.phone_mode))) {
                runPhoneMode(appInfo);
            } else if (menu.equals(getString(R.string.desktop_mode))) {
                runDesktopMode(appInfo);
            } else if (menu.equals(getString(R.string.lock_to_task_bar))) {
                lockToTakbar(appInfo);
            } else if (menu.equals(getString(R.string.unlock_from_task_bar))) {
                unloackFromTaskbar(appInfo);
            } else if (menu.equals(getString(R.string.remove_from_list))) {
                removeApplicaton(appInfo);
            } else if (menu.equals(getString(R.string.uninstall))) {
                uninstallApplication(appInfo);
            }
            dialog.dismiss();
        }

        @Override
        public void sortShow(View view, Dialog dialog, String menu) {
            if (menu.equals(getString(R.string.default_sort))) {
                mType = DEFAULT_SORT;
            } else if (menu.equals(getString(R.string.name_sort))) {
                mType = NAME_SORT;
            } else if (menu.equals(getString(R.string.time_sort))) {
                mType = TIME_SORT;
            } else if (menu.equals(getString(R.string.click_sort))) {
                mType = CLICK_SORT;
            }
            reloadGridAppInfos();
            dialog.dismiss();
        }
    };

    private BroadcastReceiver mInstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                String pkName = intent.getData().getSchemeSpecificPart();
                SqliteOpenHelper.getInstance(context).deleteDataStorage(pkName);
            }
            init();
        }
    };

    private BroadcastReceiver mKeyClickReveiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.ACTION_SEND_CLICK_INFO)) {
                String pkgName = intent.getStringExtra("keyAddInfo");
                SqliteOpenHelper.getInstance(context).updateDataStorage(updateClickCount(pkgName));
            }
        }
    };
}