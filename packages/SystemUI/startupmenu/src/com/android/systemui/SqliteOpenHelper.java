package com.android.systemui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.systemui.bean.AppInfo;
import com.android.systemui.listener.Callback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SqliteOpenHelper extends SQLiteOpenHelper {
    private static final int SQL_VERSION_CODE = 1;
    private static final String SQL_NAME = "startupMenu_database.db";

    public static final String TABLE_NAME = "table_name";
    public static final String APP_ID = "_id";
    public static final String APP_LABEL = "label";
    public static final String APP_PACKAGENAME = "pkgname";
    public static final String APP_INSTALL_TIME = "install_date";
    public static final String APP_CLICK_NUMBERS = "click_numbers";

    private static SqliteOpenHelper sqliteOpenHelper;
    private static SQLiteDatabase mDb;
    private final ExecutorService mSingleThreadExecutor;
    private Context mContext;
    private List<String> mFilterAppPkgNames;

    private SqliteOpenHelper(Context context) {
        super(context, SQL_NAME, null, SQL_VERSION_CODE);
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
        mFilterAppPkgNames = new ArrayList<>();
        String[] array = context.getResources().getStringArray(
                com.android.internal.R.array.poor_quality_apps);
        mFilterAppPkgNames.addAll(Arrays.asList(array));
        mContext = context;
    }

    public static SqliteOpenHelper getInstance(Context context) {
        if (sqliteOpenHelper == null) {
            sqliteOpenHelper = new SqliteOpenHelper(context);
        }
        return sqliteOpenHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(" +
                APP_ID + " integer primary key autoincrement," +
                APP_LABEL + " char(20)," +
                APP_PACKAGENAME + " char(100)," +
                APP_INSTALL_TIME + " integer," +
                APP_CLICK_NUMBERS + " integer" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            mDb.execSQL("drop table " + TABLE_NAME);
            onCreate(db);
        }
    }

    public void updateDataStorage(final AppInfo appInfo) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (appInfo == null) {
                    return;
                }
                mDb = sqliteOpenHelper.getWritableDatabase();
                Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME +
                        " where " + APP_PACKAGENAME
                        + " = ? ", new String[]{appInfo.getPkgName()});
                ContentValues values = new ContentValues();
                values.put(APP_CLICK_NUMBERS, appInfo.getClickCounts());
                if (cursor.moveToNext()) {
                    mDb.update(TABLE_NAME, values, APP_PACKAGENAME + " = ?",
                            new String[]{appInfo.getPkgName()});
                } else {
                    values.put(APP_LABEL, appInfo.getAppLabel());
                    values.put(APP_PACKAGENAME, appInfo.getPkgName());
                    values.put(APP_INSTALL_TIME, appInfo.getInstallTime());
                    mDb.insert(TABLE_NAME, null, values);
                }
                cursor.close();
                mDb.close();
            }
        });
    }

    public void deleteDataStorage(final String pkgName) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDb = sqliteOpenHelper.getWritableDatabase();
                mDb.delete(TABLE_NAME, APP_PACKAGENAME
                        + " = ? ", new String[]{pkgName});
                mDb.close();
            }
        });
    }

    public void queryAllDataStorage(final Callback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, AppInfo> appInfos = new HashMap<>();
                Map<String, AppInfo> map = new HashMap();
                mDb = sqliteOpenHelper.getReadableDatabase();
                AppInfo appInfo = null;
                Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME, null);
                while (cursor.moveToNext()) {
                    appInfo = new AppInfo();
                    appInfo.setAppLabel(cursor.getString(cursor.getColumnIndex(APP_LABEL)));
                    appInfo.setPkgName(
                            cursor.getString(cursor.getColumnIndex(APP_PACKAGENAME)));
                    appInfo.setClickCounts(
                            cursor.getInt(cursor.getColumnIndex(APP_CLICK_NUMBERS)));
                    appInfo.setInstallTime(
                            cursor.getLong(cursor.getColumnIndex(APP_INSTALL_TIME)));
                    map.put(appInfo.getPkgName(), appInfo);
                }
                mDb.close();

                PackageManager pm = mContext.getPackageManager();
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
                for (ResolveInfo reInfo : resolveInfos) {
                    String packageName = reInfo.activityInfo.packageName;
                    if (isFilterApp(packageName)) {
                        continue;
                    }
                    appInfo = new AppInfo();
                    appInfo.setAppLabel((String) reInfo.loadLabel(pm));
                    appInfo.setPkgName(packageName);
                    appInfo.setInstallTime(new File(
                            reInfo.activityInfo.applicationInfo.sourceDir).lastModified());
                    appInfo.setAppIcon(reInfo.loadIcon(pm));
                    appInfo.setActivityName(reInfo.activityInfo.name);
                    appInfo.setSystemApp(isSystemApp(reInfo));
                    appInfo.setClickCounts(getClickCounts(map.get(packageName)));
                    appInfo.setFullScreen(isFullScreen(packageName));
                    appInfos.put(packageName, appInfo);
                }
                callback.callback(appInfos);
            }
        });
    }

    private boolean isFilterApp(String pkgName) {
        return mFilterAppPkgNames.contains(pkgName);
    }

    private int getClickCounts(AppInfo appInfo) {
        return appInfo == null ? 0 : appInfo.getClickCounts();
    }

    private boolean isSystemApp(ResolveInfo resolveInfo) {
        return (resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
    }

    private boolean isFullScreen(String packageName) {
        return ApplicationInfo.isMaximizedStyleWindow(packageName)
                || ApplicationInfo.isRealFullScreenStyleWindow(packageName);
    }
}
