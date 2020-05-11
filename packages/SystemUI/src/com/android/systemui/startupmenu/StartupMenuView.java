package com.android.systemui.startupmenu;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.dialog.DialogType;
import com.android.systemui.dialog.MenuDialog;
import com.android.systemui.power.PowerSourceActivity;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.OnClickCallback;
import com.android.systemui.startupmenu.listener.OnRecentAppClickCallback;
import com.android.systemui.startupmenu.listener.OnRecentDocClickCallback;
import com.android.systemui.startupmenu.listener.OnMenuClick;
import com.android.systemui.startupmenu.listener.RecentAppDataCallback;
import com.android.systemui.startupmenu.utils.AppOperateManager;
import com.android.systemui.startupmenu.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;

public class StartupMenuView extends FrameLayout implements View.OnClickListener,
        View.OnHoverListener, TextWatcher {

    private static int PACKAGE_REMOEVED = 0;
    private static int PACKAGE_ADD = 1;

    private Calendar mCalendar;
    private Context mContext;
    private EditText mSearch;
    private GridView mRecentList;
    private ListView mListView;
    private ListView mRecentDocsList;
    private FrameLayout mFileManager;
    private FrameLayout mSettings;
    private FrameLayout mPoweroff;
    private TextView mRecentDocsTxt;
    private TextView mRecentTxt;
    private List<AppInfo> mAppsData = new ArrayList<>();
    private List<AppInfo> mAppsUseCountData = new ArrayList<>();
    private List<AppInfo> mRecentDocsData = new ArrayList<>();
    private AppAdapter mAppAdapter;
    private AppRecentAdapter mAppRecentAdapter;
    private RecentDocsAdapter mRecentDocsAdapter;
    private AppOperateManager mOperateManager;
    private MenuDialog mMenuDialog;
    private RecursiveFileObserver mFileObserver;

    public StartupMenuView(Context context) {
        this(context, null);
    }

    public StartupMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StartupMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.systemui_startupmenu, this);
        mOperateManager = AppOperateManager.getInstance(getContext());
        mContext = context;
        mCalendar = Calendar.getInstance();
        initData();
        initView();
        initListener();
    }

    private void initData() {
        mAppsData = mOperateManager.loadAppsInfo();
        Util.sortDatasByNameLetter(mAppsData);
        List<AppInfo> count = Util.deSerialization(
                Settings.Global.getString(mContext.getContentResolver(), Util.APP_RECENT));
        if (count != null) {
            mAppsUseCountData.addAll(count);
        }
        List<AppInfo> document = Util.deSerialization(
                Settings.Global.getString(mContext.getContentResolver(), Util.DOC_RECENT));
        if (document != null) {
            mRecentDocsData.addAll(document);
        }
        mOperateManager.updateAppsInfo(mAppsData, mAppsUseCountData);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("package");
        filter.addDataScheme("file");
        getContext().registerReceiver(mPackageReceiver, filter);
        if (mFileObserver == null) {
            mFileObserver = new RecursiveFileObserver(
                    Environment.getExternalStorageDirectory().getPath());
            mFileObserver.startWatching();
        }

    }

    private void initView() {
        mListView = findViewById(R.id.startupmenu_app_list);
        mRecentList = findViewById(R.id.startupmenu_recent);
        mFileManager = findViewById(R.id.startupmenu_filemanager);
        mSettings = findViewById(R.id.startupmenu_settings);
        mPoweroff = findViewById(R.id.startupmenu_poweroff);
        mSearch = findViewById(R.id.startupmenu_search);
        mRecentTxt = findViewById(R.id.startmenu_recent_null);
        mRecentDocsList = findViewById(R.id.startupmenu_recent_docs_list);
        mRecentDocsTxt = findViewById(R.id.startupmenu_recent_docs_null);
        mMenuDialog = new MenuDialog(getContext());
        mMenuDialog.setOnMenuClick(mOnMenuClick);

        mAppAdapter = new AppAdapter(mContext, mAppsData, R.layout.startupmenu_app_list);
        mListView.setAdapter(mAppAdapter);
        mAppRecentAdapter = new AppRecentAdapter(mContext, mAppsUseCountData, R.layout.startupmenu_recent_apps);
        mRecentList.setAdapter(mAppRecentAdapter);
        mRecentList.setVisibility(mAppsUseCountData.size() == 0 ? View.GONE : View.VISIBLE);
        mRecentTxt.setVisibility(mAppsUseCountData.size() == 0 ? View.VISIBLE : View.GONE);
        mRecentDocsAdapter = new RecentDocsAdapter(mContext, mRecentDocsData, R.layout.startupmenu_recent_docs);
        mRecentDocsList.setAdapter(mRecentDocsAdapter);
        mRecentDocsList.setVisibility(mRecentDocsData.size() == 0 ? View.GONE : View.VISIBLE);
        mRecentDocsTxt.setVisibility(mRecentDocsData.size() == 0 ? View.VISIBLE : View.GONE);
    }

    private void initListener() {
        mFileManager.setOnClickListener(this);
        mSettings.setOnClickListener(this);
        mPoweroff.setOnClickListener(this);

        mFileManager.setOnHoverListener(this);
        mSettings.setOnHoverListener(this);
        mPoweroff.setOnHoverListener(this);
        mSearch.addTextChangedListener(this);

        mAppAdapter.setOnClickCallback(new OnClickCallback() {
            @Override
            public void open(AppInfo appInfo) {
                mOperateManager.openApplication(appInfo);
            }

            @Override
            public void updateSearchState() {
                mSearch.setText("");
                mAppAdapter.notifyDataSetChanged();
            }

            @Override
            public void showDialog(int x, int y, AppInfo appInfo) {
                mMenuDialog.show(DialogType.LIST, appInfo, x, y);
            }
        });

        mAppRecentAdapter.setOnRecentAppClickCallback(new OnRecentAppClickCallback() {
            @Override
            public void open(AppInfo appInfo) {
                mOperateManager.openApplication(appInfo);
            }

            @Override
            public void showDialog(int x, int y, AppInfo appInfo) {
                mMenuDialog.show(DialogType.RECENT, appInfo, x, y);
            }
        });

        mRecentDocsAdapter.setOnRecentDocClickCallback(new OnRecentDocClickCallback() {
            @Override
            public void open(AppInfo appInfo) {
                mOperateManager.openDoc(appInfo);
            }
        });

        mOperateManager.setRecentAppDataCallback(new RecentAppDataCallback() {
            @Override
            public void udpateCallback() {
                updateAppsUseCountData();
            }
        });
    }

    private void updateAppsData(String packageName, int packageStated) {
        if (packageStated == PACKAGE_ADD) {
            Util.updateInstalledDatas(mContext, packageName, mAppsData, mAppsUseCountData);
        } else if (packageStated == PACKAGE_REMOEVED) {
            Util.updateUninstalledDatas(packageName, mAppsData, mAppsUseCountData);
            mOperateManager.removeFromTaskbar(packageName);
        }
        mAppAdapter.updateAppsList(mAppsData);
        mAppRecentAdapter.updateRecentAppsList(mAppsUseCountData);
        mOperateManager.updateAppsInfo(mAppsData, mAppsUseCountData);
    }

    private void updateAppsUseCountData() {
        mAppsUseCountData.clear();
        mAppsUseCountData.addAll(mOperateManager.getUseCountInfos());
        mAppRecentAdapter.updateRecentAppsList(mAppsUseCountData);
        mRecentList.setVisibility(mAppsUseCountData.size() == 0 ? View.GONE : View.VISIBLE);
        mRecentTxt.setVisibility(mAppsUseCountData.size() == 0 ? View.VISIBLE : View.GONE);
    }

    private void updateRecentDocsData(int event, String path) {
        File file = new File(path);
        if (file.isDirectory() || !isDocumentType(file) || path.contains("/.")) {
            return;
        }
        String[] split = file.getName().split("/");
        String docName = split[split.length - 1];
        switch (event) {
            case FileObserver.CREATE:
            case FileObserver.MOVED_TO:
                addRecentDoc(docName, path);
                break;
            case FileObserver.MODIFY:
                updateRecentDoc(docName, path);
                break;
            case FileObserver.DELETE:
            case FileObserver.MOVED_FROM:
                removeRecentDoc(docName, path);
                break;
        }
        Util.sorRecentDocsByTime(mContext, mRecentDocsData);
        mRecentDocsAdapter.updateRecentDocsData(mRecentDocsData);
        mRecentDocsList.setVisibility(mRecentDocsData.size() == 0 ? View.GONE : View.VISIBLE);
        mRecentDocsTxt.setVisibility(mRecentDocsData.size() == 0 ? View.VISIBLE : View.GONE);
    }

    private boolean isDocumentType(File file) {
        return file.getName().contains("doc")
                || file.getName().contains("ppt") || file.getName().contains("xls");
    }

    private void addRecentDoc(String docName, String path) {
        AppInfo appInfo = new AppInfo();
        appInfo.setPath(path);
        appInfo.setLabel(docName);
        appInfo.setTime(System.currentTimeMillis());
        appInfo.setYear(String.valueOf(mCalendar.get(Calendar.YEAR)));
        appInfo.setMonth(String.valueOf(mCalendar.get(Calendar.MONTH) + 1));
        appInfo.setDay(String.valueOf(mCalendar.get(Calendar.DATE)));
        mRecentDocsData.add(appInfo);
    }

    private void updateRecentDoc(String docName, String path) {
        Iterator<AppInfo> iterator = mRecentDocsData.iterator();
        while (iterator.hasNext()) {
            AppInfo next = iterator.next();
            if (path.equals(next.getPath()) && docName.equals(next.getLabel())) {
                next.setTime(System.currentTimeMillis());
                next.setYear(String.valueOf(mCalendar.get(Calendar.YEAR)));
                next.setMonth(String.valueOf(mCalendar.get(Calendar.MONTH) + 1));
                next.setDay(String.valueOf(mCalendar.get(Calendar.DATE)));
            }
        }
    }

    private void removeRecentDoc(String docName, String path) {
        Iterator<AppInfo> iterator = mRecentDocsData.iterator();
        while (iterator.hasNext()) {
            AppInfo next = iterator.next();
            if (path.equals(next.getPath()) && docName.equals(next.getLabel())) {
                iterator.remove();
            }
        }
    }

    private BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getData().getSchemeSpecificPart();
            switch (intent.getAction()) {
                case Intent.ACTION_PACKAGE_ADDED:
                    updateAppsData(packageName, PACKAGE_ADD);
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
                    updateAppsData(packageName, PACKAGE_REMOEVED);
                    break;
                case Intent.ACTION_MEDIA_MOUNTED:
                    if (Environment.getExternalStorageDirectory().getPath().equals(
                            intent.getData().getPath())) {
                        mFileObserver.stopWatching();
                        mFileObserver.startWatching();
                    }
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startupmenu_filemanager:
                mOperateManager.openApplication(Util.getSpeApps(Util.FILEMANAGER));
                break;
            case R.id.startupmenu_settings:
                mOperateManager.openApplication(Util.getSpeApps(Util.SETTINGS));
                break;
            case R.id.startupmenu_poweroff:
                mContext.startActivity(new Intent(mContext, PowerSourceActivity.class));
                dismiss();
                break;
        }
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

    private OnMenuClick mOnMenuClick = new OnMenuClick() {
        @Override
        public void menuClick(View view, Dialog dialog, AppInfo appInfo, String menu, int taskId) {
            if (menu.equals(getContext().getString(R.string.open))) {
                mOperateManager.openApplication(appInfo);
            } else if (menu.equals(getContext().getString(R.string.lock_to_task_bar))) {
                mOperateManager.addToTaskbar(-1, appInfo.getComponentName());
                appInfo.setLocked(true);
            } else if (menu.equals(getContext().getString(R.string.unlock_from_task_bar))) {
                mOperateManager.removeFromTaskbar(appInfo.getComponentName());
                appInfo.setLocked(false);
            } else if (menu.equals(getContext().getString(R.string.remove_from_list))) {
                mOperateManager.removeAppFromRecent(appInfo);
            } else if (menu.equals(getContext().getString(R.string.uninstall))) {
                mOperateManager.uninstallApp(appInfo.getPackageName());
            }
            dialog.dismiss();
        }
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mAppAdapter.getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public void dismiss() {
        AppOperateManager.getInstance(getContext()).dismissStartupMenuDialog();
    }

    public MenuDialog getmMenuDialog() {
        return mMenuDialog;
    }

    public class RecursiveFileObserver extends FileObserver {
        Map<String, SingleFileObserver> mObservers;
        String mPath;
        int mMask;
        public RecursiveFileObserver(String path) {
            this(path, FileObserver.ALL_EVENTS);
        }

        public RecursiveFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
            mMask = mask;
        }

        @Override
        public void startWatching() {
            if (mObservers != null) {
                return;
            }
            mObservers = new ArrayMap<>();
            Stack stack = new Stack();
            stack.push(mPath);

            while (!stack.isEmpty()) {
                String temp = (String) stack.pop();
                mObservers.put(temp, new SingleFileObserver(temp, mMask));
                File path = new File(temp);
                File[] files = path.listFiles();
                if (files == null)
                    continue;
                for (File f: files) {
                    if (f.isDirectory() && !f.getName().equals(".") && !f.getName()
                            .equals("..")) {
                        stack.push(f.getAbsolutePath());
                    }
                }
            }
            Iterator<String> iterator = mObservers.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                mObservers.get(key).startWatching();
            }
        }

        @Override
        public void stopWatching() {
            if (mObservers == null)
                return ;

            Iterator<String> iterator = mObservers.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                mObservers.get(key).stopWatching();
            }
            mObservers.clear();
            mObservers = null;
        }

        @Override
        public void onEvent(int event, String path) {
            int el = event & FileObserver.ALL_EVENTS;
            String[] split = path.split("/");
            switch (el) {
                case FileObserver.CREATE:
                    File file = new File(path);
                    if(file.isDirectory()) {
                        Stack stack = new Stack();
                        stack.push(path);
                        while (!stack.isEmpty()) {
                            String temp = (String) stack.pop();
                            if(mObservers.containsKey(temp)) {
                                continue;
                            } else {
                                SingleFileObserver sfo = new SingleFileObserver(temp, mMask);
                                sfo.startWatching();
                                mObservers.put(temp, sfo);
                            }
                            File tempPath = new File(temp);
                            File[] files = tempPath.listFiles();
                            if (files == null)
                                continue;
                            for (File f: files) {
                                if (f.isDirectory() && !f.getName().equals(".") && !f.getName()
                                        .equals("..")) {
                                    stack.push(f.getAbsolutePath());
                                }
                            }
                        }
                    }
                    updateRecentDocsData(FileObserver.CREATE, path);
                    break;
                case FileObserver.DELETE:
                    updateRecentDocsData(FileObserver.DELETE, path);
                    break;
                case FileObserver.MODIFY:
                    updateRecentDocsData(FileObserver.MODIFY, path);
                    break;
                case FileObserver.MOVED_FROM:
                    updateRecentDocsData(FileObserver.MOVED_FROM, path);
                    break;
                case FileObserver.MOVED_TO:
                    if (!path.contains("Recycle")) {
                        updateRecentDocsData(FileObserver.MOVED_TO, path);
                    }
                break;
            }
        }

        class SingleFileObserver extends FileObserver {
            String mPath;

            public SingleFileObserver(String path) {
                this(path, ALL_EVENTS);
                mPath = path;
            }

            public SingleFileObserver(String path, int mask) {
                super(path, mask);
                mPath = path;
            }

            @Override
            public void onEvent(int event, String path) {
                if(path != null) {
                    String newPath = mPath + "/" + path;
                    RecursiveFileObserver.this.onEvent(event, newPath);
                }
            }
        }
    }
}
