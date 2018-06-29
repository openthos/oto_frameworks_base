package com.android.systemui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.app.ActivityManagerNative;

import com.android.systemui.bean.AppInfo;
import com.android.systemui.bean.Type;
import com.android.systemui.listener.Callback;
import com.android.systemui.listener.OnClickCallback;
import com.android.systemui.listener.OnMenuClick;

public class StartupMenuView extends FrameLayout
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
    private LinearLayout mSortClickLayout;
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

    public StartupMenuView(Context context) {
        this(context, null);
    }

    public StartupMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StartupMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.activity_startup_menu, this);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.grid_view);
        mListView = (ListView) findViewById(R.id.list_view);
        mSortClickLayout = (LinearLayout) findViewById(R.id.sort_click_view);
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
                getContext().getSharedPreferences("clicks", Context.MODE_PRIVATE);
        mAllDatas = new ArrayList<>();
        mGridDatas = new ArrayList<>();
        mListDatas = new ArrayList<>();

        mGridAdapter = new AppAdapter(getContext(), Type.GRID);
        mListAdapter = new AppAdapter(getContext(), Type.LIST);
        mGridView.setAdapter(mGridAdapter);
        mListView.setAdapter(mListAdapter);
        mMenuDialog = MenuDialog.getInstance(getContext());

        mType = mSharedPreference.getInt("sortType", DEFAULT_SORT);
        init();

        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        installFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        installFilter.addDataScheme("package");
        getContext().registerReceiver(mInstallReceiver, installFilter);

        IntentFilter keyClickFilter = new IntentFilter();
        keyClickFilter.addAction("com.android.startupmenu.SQLITE_CHANGE");
        keyClickFilter.addAction(Intent.ACTION_SEND_CLICK_INFO);
        getContext().registerReceiver(mKeyClickReveiver, keyClickFilter);
    }

    private void init() {
        SqliteOpenHelper.getInstance(getContext()).queryAllDataStorage(new Callback() {
            @Override
            public void callback(Map<String, AppInfo> appInfoMap) {
                mAllDatas.clear();
                for (AppInfo appInfo : appInfoMap.values()) {
                    mAllDatas.add(appInfo);
                }
            }
        });
    }

    public void refresh() {
        mSearch.setText("");
        reloadGridAppInfos();
        reloadListAppInfos();
    }

    private void initListener() {
        mFileManager.setOnClickListener(this);
        mPowerOff.setOnClickListener(this);
        mSetting.setOnClickListener(this);

        mFileManager.setOnHoverListener(this);
        mPowerOff.setOnHoverListener(this);
        mSetting.setOnHoverListener(this);

        mSearch.addTextChangedListener(mTextWatcher);
        mSortClickLayout.setOnClickListener(this);
        mArrowShow.setOnClickListener(this);
        mMenuDialog.setOnMenuClick(mOnMenuClick);

        mGridView.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                    mGridAdapter.exit();
                }
                return false;
            }
        });

        mListView.setOnHoverListener(new OnHoverListener() {
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
                MenuDialog.getInstance(getContext()).show(Type.LIST, appInfo, x, y);
            }
        });

        mGridAdapter.setOnClickCallback(new OnClickCallback() {
            @Override
            public void open(AppInfo appInfo) {
                openApplication(appInfo);
            }

            @Override
            public void showDialog(int x, int y, AppInfo appInfo) {
                MenuDialog.getInstance(getContext()).show(Type.GRID, appInfo, x, y);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_manager:
                PackageManager pm = getContext().getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(
                        ApplicationInfo.APPNAME_OTO_FILEMANAGER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getContext().startActivity(intent);
                dismiss();
                break;
            case R.id.system_setting:
                getContext().startActivity(new Intent(Settings.ACTION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
        ActivityManagerNative.callPowerSource(getContext());
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
        StartupMenuManager.getInstance(getContext()).hideStartupMenu();
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
        mListAdapter.refresh(mListDatas);
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
        mGridAdapter.refresh(mGridDatas);
        mSharedPreference.edit().putInt("sortType", mType).commit();
    }

    private void defaultSort() {
        Collections.sort(mGridDatas, new Comparator<AppInfo>() {
            public int compare(AppInfo lhs, AppInfo rhs) {
                if (lhs.isSystemApp() && !rhs.isSystemApp()) {
                    return -1;
                } else if (!lhs.isSystemApp() && rhs.isSystemApp()) {
                    return 1;
                } else {
                    return lhs.getAppLabel().compareTo(rhs.getAppLabel());
                }
            }
        });
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
        Intent intent = appInfo.getIntent(getContext());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
        Intent openAppIntent = new Intent();
        openAppIntent.setAction(Intent.ACTION_OPEN_APPLICATION);
        getContext().sendBroadcastAsUser(openAppIntent, UserHandle.ALL);
        SqliteOpenHelper.getInstance(getContext())
                .updateDataStorage(updateClickCount(appInfo.getPkgName()));
        dismiss();
    }

    private void runPhoneMode(AppInfo appInfo) {
        Intent intent = appInfo.getIntent(getContext());
        intent.addFlags(Intent.FLAG_ACTIVITY_RUN_PHONE_MODE
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getContext().startActivity(intent);
        SqliteOpenHelper.getInstance(getContext()).updateDataStorage(updateClickCount(appInfo.getPkgName()));
        dismiss();
    }

    private void runDesktopMode(AppInfo appInfo) {
        Intent intent = appInfo.getIntent(getContext());
        intent.addFlags(Intent.FLAG_ACTIVITY_RUN_PC_MODE
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getContext().startActivity(intent);
        SqliteOpenHelper.getInstance(getContext()).updateDataStorage(updateClickCount(appInfo.getPkgName()));
        dismiss();
    }

    private void lockToTakbar(AppInfo appInfo) {
        Intent intentSend = new Intent();
        intentSend.putExtra("keyInfo", appInfo.getPkgName());
        intentSend.setAction(Intent.ACTION_STARTUPMENU_SEND_INFO_LOCK);
        getContext().sendBroadcast(intentSend);
    }

    private void unloackFromTaskbar(AppInfo appInfo) {
        Intent intentUnlock = new Intent();
        intentUnlock.putExtra("unlockapk", appInfo.getPkgName());
        intentUnlock.setAction(Intent.STARTMENU_UNLOCKED);
        getContext().sendBroadcast(intentUnlock);
    }

    private void uninstallApplication(AppInfo appInfo) {
        Uri uri = Uri.parse("package:" + appInfo.getPkgName());
        Intent intents = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intents);
        dismiss();
    }

    private void removeApplicaton(AppInfo appInfo) {
        Toast.makeText(getContext(),
                getContext().getString(R.string.remove_application) + appInfo.getAppLabel(),
                Toast.LENGTH_SHORT).show();
        for (AppInfo info : mAllDatas) {
            if (info.getPkgName().equals(appInfo.getPkgName())) {
                info.setClickCounts(0);
                break;
            }
        }
        mListDatas.remove(appInfo);
        mListAdapter.refresh(mListDatas);
        SqliteOpenHelper.getInstance(getContext()).deleteDataStorage(appInfo.getPkgName());
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
            if (menu.equals(getContext().getString(R.string.open))) {
                openApplication(appInfo);
            } else if (menu.equals(getContext().getString(R.string.phone_mode))) {
                runPhoneMode(appInfo);
            } else if (menu.equals(getContext().getString(R.string.desktop_mode))) {
                runDesktopMode(appInfo);
            } else if (menu.equals(getContext().getString(R.string.lock_to_task_bar))) {
                lockToTakbar(appInfo);
            } else if (menu.equals(getContext().getString(R.string.unlock_from_task_bar))) {
                unloackFromTaskbar(appInfo);
            } else if (menu.equals(getContext().getString(R.string.remove_from_list))) {
                removeApplicaton(appInfo);
            } else if (menu.equals(getContext().getString(R.string.uninstall))) {
                uninstallApplication(appInfo);
            }
            dialog.dismiss();
        }

        @Override
        public void sortShow(View view, Dialog dialog, String menu) {
            if (menu.equals(getContext().getString(R.string.default_sort))) {
                mType = DEFAULT_SORT;
            } else if (menu.equals(getContext().getString(R.string.name_sort))) {
                mType = NAME_SORT;
            } else if (menu.equals(getContext().getString(R.string.time_sort))) {
                mType = TIME_SORT;
            } else if (menu.equals(getContext().getString(R.string.click_sort))) {
                mType = CLICK_SORT;
            }
            reloadGridAppInfos();
            dialog.dismiss();
        }
    };

    private BroadcastReceiver mInstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                String pkName = intent.getData().getSchemeSpecificPart();
                SqliteOpenHelper.getInstance(context).deleteDataStorage(pkName);
            }
            init();
        }
    };

    private BroadcastReceiver mKeyClickReveiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SEND_CLICK_INFO)) {
                String pkgName = intent.getStringExtra("keyAddInfo");
                SqliteOpenHelper.getInstance(context).updateDataStorage(updateClickCount(pkgName));
            }
        }
    };
}