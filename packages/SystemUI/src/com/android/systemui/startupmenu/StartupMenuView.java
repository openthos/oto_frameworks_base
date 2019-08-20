package com.android.systemui.startupmenu;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
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

import com.android.systemui.R;
import com.android.systemui.dialog.DialogType;
import com.android.systemui.dialog.MenuDialog;
import com.android.systemui.power.PowerSourceActivity;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.bean.Type;
import com.android.systemui.startupmenu.listener.DataCallback;
import com.android.systemui.startupmenu.listener.OnClickCallback;
import com.android.systemui.startupmenu.listener.OnMenuClick;
import com.android.systemui.startupmenu.utils.AppOperateManager;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
    private AppOperateManager mOperateManager;
    private ArrayList<AppInfo> mGridDatas;
    private ArrayList<AppInfo> mListDatas;
    private AppAdapter mGridAdapter;
    private AppAdapter mListAdapter;
    private MenuDialog mMenuDialog;
    private int mType;
    private Handler mHandler;

    public StartupMenuView(Context context) {
        this(context, null);
    }

    public StartupMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StartupMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.startup_menu, this);
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
        mHandler = new Handler();
        mSharedPreference =
                getContext().getSharedPreferences("sort_type", Context.MODE_PRIVATE);
        mOperateManager = AppOperateManager.getInstance(getContext());
        mOperateManager.reloadData();
        mGridDatas = new ArrayList<>();
        mListDatas = new ArrayList<>();

        mGridAdapter = new AppAdapter(getContext(), Type.GRID);
        mListAdapter = new AppAdapter(getContext(), Type.LIST);
        mGridView.setAdapter(mGridAdapter);
        mListView.setAdapter(mListAdapter);
        mMenuDialog = new MenuDialog(getContext());

        mType = mSharedPreference.getInt("sortType", DEFAULT_SORT);

        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        installFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        installFilter.addDataScheme("package");
        getContext().registerReceiver(mInstallReceiver, installFilter);
    }

    public void refresh() {
        //TODO
//        mSearch.setText("");
//        reloadGridAppInfos();
//        reloadListAppInfos();
        mOperateManager.reloadData(new DataCallback() {
            @Override
            public void callback(Map<String, AppInfo> appInfoMaps) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSearch.setText("");
                        reloadGridAppInfos();
                        reloadListAppInfos();
                    }
                });
            }
        });
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

        mListAdapter.setOnClickCallback(new OnClickCallback() {
            @Override
            public void open(AppInfo appInfo) {
                mOperateManager.openApplication(appInfo.getComponentName());
                dismiss();
            }

            @Override
            public void showDialog(int x, int y, AppInfo appInfo) {
                mMenuDialog.show(DialogType.LIST, appInfo, x, y);
            }
        });

        mGridAdapter.setOnClickCallback(new OnClickCallback() {
            @Override
            public void open(AppInfo appInfo) {
                mOperateManager.openApplication(appInfo.getComponentName());
                dismiss();
            }

            @Override
            public void showDialog(int x, int y, AppInfo appInfo) {
                mMenuDialog.show(DialogType.GRID, appInfo, x, y);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_manager:
                mOperateManager.openApplication(new ComponentName(
                        "org.openthos.filemanager",
                        "org.openthos.filemanager.MainActivity"));
                break;
            case R.id.system_setting:
                mOperateManager.openApplication(new ComponentName(
                        "com.android.settings", "com.android.settings.Settings"));
                break;
            case R.id.power_off:
                getContext().startActivity(new Intent(getContext(), PowerSourceActivity.class));
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

    public MenuDialog getmMenuDialog() {
        return mMenuDialog;
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

    public void dismiss() {
        AppOperateManager.getInstance(getContext()).dismissStartupMenuDialog();
    }

    private void reloadListAppInfos() {
        mListDatas.clear();
        Collections.sort(mOperateManager.getDataList(), new Comparator<AppInfo>() {
            public int compare(AppInfo lhs, AppInfo rhs) {
                int rhsNumber = rhs.getUseCounts();
                int lhsNumber = lhs.getUseCounts();
                if (rhsNumber > lhsNumber) {
                    return 1;
                } else if (lhsNumber == rhsNumber) {
                    return rhs.getInstallTime() > lhs.getInstallTime() ? 1 : -1;
                } else {
                    return -1;
                }
            }
        });
        int min = Math.min(mOperateManager.getDataList().size(), LIST_APP_NUM);
        for (int i = 0; i < min; i++) {
            AppInfo appInfo = mOperateManager.getDataList().get(i);
            if (appInfo.getUseCounts() != 0) {
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
            mGridDatas.addAll(mOperateManager.getDataList());
        } else {
            for (AppInfo appInfo : mOperateManager.getDataList()) {
                if (appInfo.getLabel().toLowerCase().contains(searchText.toLowerCase())) {
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
                    return lhs.getLabel().compareTo(rhs.getLabel());
                }
            }
        });
    }

    private void nameSort() {
        List<AppInfo> listEnglish = new ArrayList<>();
        List<AppInfo> listChina = new ArrayList<>();
        List<AppInfo> listNumber = new ArrayList<>();
        for (AppInfo appInfo : mGridDatas) {
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
        Collections.sort(listEnglish, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });

        Collections.sort(listNumber, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });
        final Collator collator = Collator.getInstance();
        Collections.sort(listChina, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return collator.getCollationKey(o1.getLabel()).
                        compareTo(collator.getCollationKey(o2.getLabel()));
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
                    return rhs.getPackageName().compareTo(lhs.getPackageName());
                }
                return rhs.getInstallTime() > lhs.getInstallTime() ? 1 : -1;
            }
        });
    }

    private void clickSort() {
        Collections.sort(mGridDatas, new Comparator<AppInfo>() {
            public int compare(AppInfo lhs, AppInfo rhs) {
                if (rhs.getUseCounts() == lhs.getUseCounts()) {
                    return rhs.getInstallTime() > lhs.getInstallTime() ? 1 : -1;
                }
                return rhs.getUseCounts() > lhs.getUseCounts() ? 1 : -1;
            }
        });
    }

    private void removeApplicaton(AppInfo appInfo) {
        Toast.makeText(getContext(),
                getContext().getString(R.string.remove_application) + appInfo.getLabel(),
                Toast.LENGTH_SHORT).show();
        for (AppInfo info : mOperateManager.getDataList()) {
            if (info.getPackageName().equals(appInfo.getPackageName())) {
                info.setUseCounts(0);
                break;
            }
        }
        reloadListAppInfos();
        reloadGridAppInfos();
        SqliteOpenHelper.getInstance(getContext()).deleteDataStorage(appInfo.getPackageName());
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
        public void menuClick(View view, Dialog dialog, AppInfo appInfo, String menu, int taskId) {
            if (menu.equals(getContext().getString(R.string.open))) {
                mOperateManager.openApplication(appInfo.getComponentName());
            } else if (menu.equals(getContext().getString(R.string.lock_to_task_bar))) {
                mOperateManager.addToTaskbar(-1, appInfo.getComponentName());
            } else if (menu.equals(getContext().getString(R.string.unlock_from_task_bar))) {
                mOperateManager.removeFromTaskbar(appInfo.getComponentName());
                appInfo.setLocked(false);
            } else if (menu.equals(getContext().getString(R.string.remove_from_list))) {
                removeApplicaton(appInfo);
            } else if (menu.equals(getContext().getString(R.string.uninstall))) {
                mOperateManager.uninstallApp(appInfo.getPackageName());
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
                mOperateManager.removeFromTaskbar(pkName);
                SqliteOpenHelper.getInstance(context).deleteDataStorage(pkName);
            }
            mOperateManager.reloadData();
        }
    };
}
