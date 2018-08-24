package com.android.systemui.startupmenu;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.DataCallback;

import java.io.File;
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
    public static final String LABEL = "label";
    public static final String PKGNAME = "pkgname";
    public static final String INSTALL_TIME = "install_date";
    public static final String CLICK_NUMBERS = "click_numbers";
    public static final String IS_LOCKED = "locked";
    public static final String USER_ID = "user_id";
    public static final String ACTIVITY_NAME = "activity_name";

    private static SqliteOpenHelper sqliteOpenHelper;
    private static SQLiteDatabase mDb;
    private final ExecutorService mSingleThreadExecutor;
    private Context mContext;

    private SqliteOpenHelper(Context context) {
        super(context, SQL_NAME, null, SQL_VERSION_CODE);
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
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
                LABEL + " char(20)," +
                PKGNAME + " char(100)," +
                INSTALL_TIME + " integer," +
                CLICK_NUMBERS + " integer," +
                IS_LOCKED + " integer," +
                USER_ID + " integer," +
                ACTIVITY_NAME + " char(100)" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            mDb.execSQL("drop table " + TABLE_NAME);
            onCreate(db);
        }
    }

    public void updateDataClick(final AppInfo appInfo) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (appInfo == null) {
                    return;
                }
                mDb = sqliteOpenHelper.getWritableDatabase();
                Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME + " where " + PKGNAME
                        + " = ? ", new String[]{appInfo.getPackageName()});
                ContentValues values = new ContentValues();
                values.put(CLICK_NUMBERS, appInfo.getUseCounts());
                if (cursor.moveToNext()) {
                    mDb.update(TABLE_NAME, values, PKGNAME + " = ?",
                            new String[]{appInfo.getPackageName()});
                } else {
                    values.put(LABEL, appInfo.getLabel());
                    values.put(PKGNAME, appInfo.getPackageName());
                    values.put(INSTALL_TIME, appInfo.getInstallTime());
                    values.put(INSTALL_TIME, appInfo.getInstallTime());
                    values.put(ACTIVITY_NAME, appInfo.getActivityName());
                    values.put(IS_LOCKED, appInfo.isLocked() ? 1 : 0);
                    mDb.insert(TABLE_NAME, null, values);
                }
                cursor.close();
                mDb.close();
            }
        });
    }

    public void updateDataLocked(final AppInfo appInfo) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (appInfo == null) {
                    return;
                }
                mDb = sqliteOpenHelper.getWritableDatabase();
                Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME + " where " + PKGNAME
                        + " = ? ", new String[]{appInfo.getPackageName()});
                ContentValues values = new ContentValues();
                values.put(IS_LOCKED, appInfo.isLocked() ? 1 : 0);
                if (cursor.moveToNext()) {
                    mDb.update(TABLE_NAME, values, PKGNAME + " = ?",
                            new String[]{appInfo.getPackageName()});
                } else {
                    values.put(LABEL, appInfo.getLabel());
                    values.put(PKGNAME, appInfo.getPackageName());
                    values.put(INSTALL_TIME, appInfo.getInstallTime());
                    values.put(CLICK_NUMBERS, appInfo.getUseCounts());
                    values.put(ACTIVITY_NAME, appInfo.getActivityName());
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
                mDb.delete(TABLE_NAME, PKGNAME
                        + " = ? ", new String[]{pkgName});
                mDb.close();
            }
        });
    }

    public void queryLockedApp(final DataCallback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, AppInfo> map = new HashMap();
                mDb = sqliteOpenHelper.getReadableDatabase();
                AppInfo appInfo = null;
                Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME, null);
                while (cursor.moveToNext()) {
                    appInfo = new AppInfo();
                    appInfo.setLabel(cursor.getString(cursor.getColumnIndex(LABEL)));
                    appInfo.setPackageName(
                            cursor.getString(cursor.getColumnIndex(PKGNAME)));
                    if (appInfo.isLocked()) {
                        map.put(appInfo.getPackageName(), appInfo);
                    }
                }
                mDb.close();
                callback.callback(map);
            }
        });
    }

    public void queryAllDataStorage(final DataCallback callback) {
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
                    appInfo.setLabel(cursor.getString(cursor.getColumnIndex(LABEL)));
                    appInfo.setPackageName(
                            cursor.getString(cursor.getColumnIndex(PKGNAME)));
                    appInfo.setUseCounts(
                            cursor.getInt(cursor.getColumnIndex(CLICK_NUMBERS)));
                    appInfo.setInstallTime(
                            cursor.getLong(cursor.getColumnIndex(INSTALL_TIME)));
                    appInfo.setLocked(
                            cursor.getInt(cursor.getColumnIndex(IS_LOCKED)) == 0 ? false : true);
                    map.put(appInfo.getPackageName(), appInfo);
                }
                mDb.close();

                PackageManager pm = mContext.getPackageManager();
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
                for (ResolveInfo reInfo : resolveInfos) {
                    String packageName = reInfo.activityInfo.packageName;
                    appInfo = new AppInfo();
                    appInfo.setLabel((String) reInfo.loadLabel(pm));
                    appInfo.setPackageName(packageName);
                    appInfo.setInstallTime(new File(
                            reInfo.activityInfo.applicationInfo.sourceDir).lastModified());
                    appInfo.setIcon(reInfo.loadIcon(pm));
                    appInfo.setActivityName(reInfo.activityInfo.name);
                    appInfo.setSystemApp(isSystemApp(reInfo));
                    appInfo.setUseCounts(getClickCounts(map.get(packageName)));
                    appInfo.setLocked(getLocked(map.get(packageName)));
                    appInfos.put(packageName, appInfo);
                }
                callback.callback(appInfos);
            }
        });
    }

    private int getClickCounts(AppInfo appInfo) {
        return appInfo == null ? 0 : appInfo.getUseCounts();
    }

    private boolean getLocked(AppInfo appInfo) {
        return appInfo == null ? false : appInfo.isLocked();
    }

    private boolean isSystemApp(ResolveInfo resolveInfo) {
        return (resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
