package com.android.systemui.dialog;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.systemui.R;
import com.android.systemui.sql.SqliteOpenHelper;
import com.android.systemui.sql.StartMenuDatabaseField;
import com.android.systemui.startupmenu.AppEntry;
import com.android.systemui.startupmenu.ShowType;
import com.android.systemui.startupmenu.SortSelectPopupWindow;
import com.android.systemui.startupmenu.StartMenuAdapter;
import com.android.systemui.startupmenu.U;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ljh on 17-9-26.
 */

public class StartupMenuDialog extends BaseDialog
        implements View.OnClickListener, View.OnHoverListener,
        SortSelectPopupWindow.SortSelectListener, TextWatcher {
    public static final int DEFAULT_SORT = 0;
    public static final int NAME_SORT = 1;
    public static final int NAME_SORT_REVERSE = -1;
    public static final int TIME_SORT = 2;
    public static final int TIME_SORT_REVERSE = -2;
    public static final int CLICK_SORT = 3;
    public static final int CLICK_SORT_REVERSE = -3;

    private static StartupMenuDialog mStartupMenuDialog;

    private GridView mStartMenu;
    private EditText mSearchView;
    private ListView mCommonList;
    private LinearLayout mFileManager;
    private LinearLayout mSetting;
    private LinearLayout mPower;
    private LinearLayout mSortLayout;
    private ImageView mSortDirection;
    private TextView mSortText;

    private ImageView mSortSelect;
    public List<AppEntry> mCommonDatas;
    private List<AppEntry> mShowAppData;

    private List<AppEntry> mAppEntries;
    public StartMenuAdapter mCommonAdapter;
    private StartMenuAdapter mStartMenuAdapter;
    private SortSelectPopupWindow mSortSelectPopupWindow;
    private int mType;
    private Handler mHandler;

    private BroadcastReceiver mAppStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshApps(true);
        }
    };

    public static StartupMenuDialog getInstance(Context context) {
        if (mStartupMenuDialog == null) {
            mStartupMenuDialog = new StartupMenuDialog(context);
        }
        return mStartupMenuDialog;
    }

    private StartupMenuDialog(@NonNull Context context) {
        super(context);
        mContentView =
                LayoutInflater.from(getContext()).inflate(R.layout.start_menu_left, null, false);
        setContentView(R.layout.start_menu_left);
    }

    public static void dismissDialog() {
        if (mStartupMenuDialog != null && mStartupMenuDialog.isShowing()) {
            mStartupMenuDialog.dismiss();
        }
    }

    @Override
    public void show(View view) {
        super.show(view);
        refreshApps(true);
    }

    public void initView() {
        mCommonList = (ListView) findViewById(R.id.common_list);
        mFileManager = (LinearLayout) findViewById(R.id.file_manager);
        mSetting = (LinearLayout) findViewById(R.id.system_setting);
        mPower = (LinearLayout) findViewById(R.id.power_off);
        mSearchView = (EditText) findViewById(R.id.search);
        mSortLayout = (LinearLayout) findViewById(R.id.sort_click_layout);
        mSortDirection = (ImageView) findViewById(R.id.sort_image_direction);
        mSortText = (TextView) findViewById(R.id.sort_text);
        mSortSelect = (ImageView) findViewById(R.id.sort_select);
        mStartMenu = (GridView) findViewById(R.id.start_menu);
    }

    public void initData() {
        mAppEntries = new ArrayList<>();
        mCommonDatas = new ArrayList<>();
        mShowAppData = new ArrayList<>();
        mCommonAdapter = new StartMenuAdapter(getContext(), ShowType.LIST, mCommonDatas);
        mStartMenuAdapter = new StartMenuAdapter(getContext(), ShowType.GRID, mShowAppData);
        mCommonList.setAdapter(mCommonAdapter);
        mStartMenu.setAdapter(mStartMenuAdapter);

        mSearchView.addTextChangedListener(this);
        mSortSelectPopupWindow = new SortSelectPopupWindow(getContext());
        mType = U.getSharedPreferences(getContext()).getInt("sortType", DEFAULT_SORT);

        mHandler = new Handler();
    }

    public void initListener() {
        mFileManager.setOnClickListener(this);
        mSetting.setOnClickListener(this);
        mPower.setOnClickListener(this);
        mSortLayout.setOnClickListener(this);
        mSortSelect.setOnClickListener(this);

        mFileManager.setOnHoverListener(this);
        mSetting.setOnHoverListener(this);
        mPower.setOnHoverListener(this);

        mSortSelectPopupWindow.setSortSelectListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_manager:
                Toast.makeText(getContext(), "打开文件管理器", Toast.LENGTH_SHORT).show();
                dismiss();
                break;
            case R.id.system_setting:
                U.launchApp(getContext(),
                        new ComponentName("com.android.settings", "com.android.settings.Settings"));
                dismiss();
                break;
            case R.id.power_off:
                Toast.makeText(getContext(), "打开电源", Toast.LENGTH_SHORT).show();
                dismiss();
                break;
            case R.id.sort_click_layout:
                mType = mType * -1;
                sortOrder();
                refreshSortView();
                mStartMenuAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_select:
                mSortSelectPopupWindow.showAsDropDown(mSortLayout);
                break;
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
                v.setBackgroundResource(R.mipmap.common_bg);
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                v.setBackgroundResource(R.color.common_hover_bg);
                break;
        }
        return false;
    }

    private void refreshApps(boolean firstDraw) {
        refreshApps(null, firstDraw);
    }

    private void refreshApps(final String query, final boolean firstDraw) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (firstDraw) {
                    mAppEntries.clear();
                    PackageManager pm = getContext().getPackageManager();
                    Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
                    AppEntry appInfo;
                    for (ResolveInfo reInfo : resolveInfos) {
                        appInfo = new AppEntry();
                        appInfo.setLabel((String) reInfo.loadLabel(pm));
                        appInfo.setPackageName(reInfo.activityInfo.packageName);
                        appInfo.setInstallTime(
                                new File(reInfo.activityInfo.applicationInfo.sourceDir).lastModified());
                        appInfo.setIcon(reInfo.loadIcon(pm));
                        appInfo.setActivityName(reInfo.activityInfo.name);
                        appInfo.setUseCounts(0);
                        mAppEntries.add(appInfo);
                    }
                    queryCommonAppInfo();
                }

                mShowAppData.clear();
                if (TextUtils.isEmpty(query)) {
                    mShowAppData.addAll(mAppEntries);
                } else {
                    for (AppEntry appEntry : mAppEntries) {
                        if (appEntry.getLabel().toLowerCase().contains(query.toLowerCase())) {
                            mShowAppData.add(appEntry);
                        }
                    }
                }

                sortOrder();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshSortView();
                        mStartMenuAdapter.notifyDataSetChanged();
                        mCommonAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    private void queryCommonAppInfo() {
        SqliteOpenHelper sqliteOpenHelper = SqliteOpenHelper.getInstance(getContext());
        SQLiteDatabase db = sqliteOpenHelper.getWritableDatabase();
        List<AppEntry> tempAppInfo = new ArrayList<>();
        mCommonDatas.clear();
        Cursor cs = db.rawQuery("select distinct * from " + StartMenuDatabaseField.TABLE_NAME
                + " order by " + StartMenuDatabaseField.COLUMN_USECOUNT + " desc", new String[]{});
        int i = 0;
        while (cs.moveToNext() && i < 8) {
            String pkgName = cs.getString(cs.getColumnIndex(StartMenuDatabaseField.COLUMN_PACKAGENAME));
            int number = cs.getInt(cs.getColumnIndex(StartMenuDatabaseField.COLUMN_USECOUNT));
            if (number > 0) {
                for (AppEntry info : mAppEntries) {
                    if (info.getPackageName().equals(pkgName)) {
                        info.setUseCounts(number);
                        tempAppInfo.add(info);
                        break;
                    }
                }
            }
            i++;
        }
        cs.close();
        db.close();

        Collections.sort(tempAppInfo, new Comparator<AppEntry>() {
            public int compare(AppEntry lhs, AppEntry rhs) {
                if (rhs.getUseCounts() == lhs.getUseCounts()) {
                    return rhs.getPackageName().compareTo(lhs.getPackageName());
                }
                return rhs.getUseCounts() > lhs.getUseCounts() ? 1 : -1;
            }
        });
        mCommonDatas.addAll(
                tempAppInfo.subList(0, Math.min(tempAppInfo.size(), 8)));
    }

    private void sortOrder() {
        switch (mType) {
            case DEFAULT_SORT:
                defaultSort();
                break;
            case NAME_SORT:
                nameSort();
                break;
            case NAME_SORT_REVERSE:
                nameSort();
                Collections.reverse(mShowAppData);
                break;
            case TIME_SORT:
                timeSort();
                break;
            case TIME_SORT_REVERSE:
                timeSort();
                Collections.reverse(mShowAppData);
                break;
            case CLICK_SORT:
                clickSort();
                break;
            case CLICK_SORT_REVERSE:
                clickSort();
                Collections.reverse(mShowAppData);
                break;
            default:
                break;
        }
        saveSortTypeToSP(mType);
    }

    private void refreshSortView() {
        mSortDirection.setVisibility(View.VISIBLE);
        switch (mType) {
            case DEFAULT_SORT:
                mSortDirection.setVisibility(View.INVISIBLE);
                mSortText.setText(R.string.default_sort);
                break;
            case NAME_SORT:
                mSortDirection.setImageResource(R.mipmap.ic_start_menu_up_arrow);
                mSortText.setText(R.string.name_sort);
                break;
            case NAME_SORT_REVERSE:
                mSortDirection.setImageResource(R.mipmap.ic_start_menu_down_arrow);
                mSortText.setText(R.string.name_sort);
                break;
            case TIME_SORT:
                mSortDirection.setImageResource(R.mipmap.ic_start_menu_up_arrow);
                mSortText.setText(R.string.time_sort);
                break;
            case TIME_SORT_REVERSE:
                mSortDirection.setImageResource(R.mipmap.ic_start_menu_down_arrow);
                mSortText.setText(R.string.time_sort);
                break;
            case CLICK_SORT:
                mSortDirection.setImageResource(R.mipmap.ic_start_menu_up_arrow);
                mSortText.setText(R.string.click_sort);
                break;
            case CLICK_SORT_REVERSE:
                mSortDirection.setImageResource(R.mipmap.ic_start_menu_down_arrow);
                mSortText.setText(R.string.click_sort);
                break;
            default:
                break;
        }
    }

    private void defaultSort() {
        nameSort();
    }

    private void nameSort() {
        List<AppEntry> listEnglish = new ArrayList<>();
        List<AppEntry> listChina = new ArrayList<>();
        List<AppEntry> listNumber = new ArrayList<>();
        for (AppEntry appInfo : mShowAppData) {
            String appLabel = appInfo.getLabel();
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
        Collections.sort(listEnglish, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry o1, AppEntry o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });

        Collections.sort(listNumber, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry o1, AppEntry o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });
        final Collator collator = Collator.getInstance();
        Collections.sort(listChina, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry o1, AppEntry o2) {
                return collator.getCollationKey(o1.getLabel()).
                        compareTo(collator.getCollationKey(o2.getLabel()));
            }
        });
        mShowAppData.clear();
        mShowAppData.addAll(listNumber);
        mShowAppData.addAll(listEnglish);
        mShowAppData.addAll(listChina);
    }

    private void timeSort() {
        Collections.sort(mShowAppData, new Comparator<AppEntry>() {
            public int compare(AppEntry lhs, AppEntry rhs) {
                if (rhs.getInstallTime() == lhs.getInstallTime()) {
                    return rhs.getPackageName().compareTo(lhs.getPackageName());
                }
                return rhs.getInstallTime() > lhs.getInstallTime() ? 1 : -1;
            }
        });
    }

    private void clickSort() {
        Collections.sort(mShowAppData, new Comparator<AppEntry>() {
            public int compare(AppEntry lhs, AppEntry rhs) {
                if (rhs.getUseCounts() == lhs.getUseCounts()) {
                    return rhs.getPackageName().compareTo(lhs.getPackageName());
                }
                return rhs.getUseCounts() > lhs.getUseCounts() ? 1 : -1;
            }
        });
    }

    private void saveSortTypeToSP(int type) {
        U.getSharedPreferences(getContext()).edit().putInt("sortType", type).commit();
    }

    @Override
    public void defaultSort(View v) {
        mType = DEFAULT_SORT;
        sortOrder();
        refreshSortView();
        mStartMenuAdapter.notifyDataSetChanged();
    }

    @Override
    public void clickSort(View v) {
        mType = CLICK_SORT;
        sortOrder();
        refreshSortView();
        mStartMenuAdapter.notifyDataSetChanged();
    }

    @Override
    public void timeSort(View v) {
        mType = TIME_SORT;
        sortOrder();
        refreshSortView();
        mStartMenuAdapter.notifyDataSetChanged();
    }

    @Override
    public void nameSort(View v) {
        mType = NAME_SORT;
        sortOrder();
        refreshSortView();
        mStartMenuAdapter.notifyDataSetChanged();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        refreshApps(s.toString(), false);
    }

    public class AppStateChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshApps(true);
        }
    }
}
