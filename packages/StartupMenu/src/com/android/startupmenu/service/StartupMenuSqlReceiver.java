package com.android.startupmenu.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.startupmenu.StartupMenuActivity;
import com.android.startupmenu.util.StartupMenuSqliteOpenHelper;
import com.android.startupmenu.util.TableIndexDefine;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.android.startupmenu.StartupMenuActivity.isEnglish;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class StartupMenuSqlReceiver extends BroadcastReceiver {
    private StartupMenuSqliteOpenHelper mMsoh;
    private SQLiteDatabase mdb;
    private boolean mIsHasReayDb;
    private int mNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        mMsoh = new StartupMenuSqliteOpenHelper(
                context, "StartupMenu_database.db", null, StartupMenuActivity.SQL_VERSION_CODE);
        mdb = mMsoh.getWritableDatabase();
        //Accept Message
        if (intent.getAction().equals(Intent.ACTION_SEND_CLICK_INFO)) {
            String pkgName = intent.getStringExtra("keyAddInfo");
            Cursor c = mdb.rawQuery("select * from " + TableIndexDefine.TABLE_APP_PERPO +
                           " where " + TableIndexDefine.COLUMN_PERPO_PKGNAME +
                           " = ?", new String[]{pkgName});
            ContentValues values = new ContentValues();
            int number = 0;
            if (c.moveToNext()) {
                number = c.getInt(c.getColumnIndex(TableIndexDefine.COLUMN_PERPO_CLICK_NUM));
                number++;
                values.put(TableIndexDefine.COLUMN_PERPO_CLICK_NUM, number);
                mdb.update(TableIndexDefine.TABLE_APP_PERPO, values, TableIndexDefine
                                  .COLUMN_PERPO_PKGNAME + " = ? ", new String[]{pkgName});
            } else {
                number++;
                values.put(TableIndexDefine.COLUMN_PERPO_PKGNAME, pkgName);
                values.put(TableIndexDefine.COLUMN_PERPO_CLICK_NUM, number);
                mdb.insert(TableIndexDefine.TABLE_APP_PERPO,
                           TableIndexDefine.COLUMN_PERPO_PKGNAME, values);
                BackstageRenewalData(context);
            }
            //Same to open run
            SharedPreferences sharedPreference = context.getSharedPreferences("click",
                                                          Context.MODE_PRIVATE);
            Editor editor = sharedPreference.edit();
            String type = sharedPreference.getString("type", "sortName");
            int order = sharedPreference.getInt("order", 0);
            editor.clear();
            editor.putBoolean("isClickApp", true);
            editor.putString("type", type);
            editor.putInt("order", order);
            editor.commit();
        }
    }

    public void BackstageRenewalData(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
        for (ResolveInfo reInfo : resolveInfos) {
            File file = new File(reInfo.activityInfo.applicationInfo.sourceDir);
            long installTime = file.lastModified();
            String pkgName = reInfo.activityInfo.packageName;
            String appLabel = (String) reInfo.loadLabel(pm);
            mIsHasReayDb = false;
            Cursor c = mdb.rawQuery("select * from " + TableIndexDefine.TABLE_APP_PERPO +
                                    " where " + TableIndexDefine.COLUMN_PERPO_PKGNAME + " = ?",
                                                new String[]{pkgName});
            while (c.moveToNext()) {
                String pkname = c.getString(c.getColumnIndex(TableIndexDefine.COLUMN_PERPO_PKGNAME));
                if (pkgName.equals(pkname)) {
                    mIsHasReayDb = true;
                    break;
                }
            }

            if (!mIsHasReayDb) {
                mdb.execSQL("insert into " +
                             TableIndexDefine.TABLE_APP_PERPO + "(" +
                             TableIndexDefine.COLUMN_PERPO_LABEL + "," +
                             TableIndexDefine.COLUMN_PERPO_PKGNAME + "," +
                             TableIndexDefine.COLUMN_PERPO_INSTALL_DATE + "," +
                             TableIndexDefine.COLUMN_PERPO_CLICK_NUM + ")" +
                             "values (?, ?, ?, ?)",
                             new Object[]{appLabel, pkgName, installTime, mNumber});
            }

            if (isEnglish(appLabel)) {
                ContentValues contentvalues = new ContentValues();
                contentvalues.put(TableIndexDefine.COLUMN_PERPO_LABEL, appLabel);
                mdb.update(TableIndexDefine.TABLE_APP_PERPO, contentvalues, TableIndexDefine.
                                      COLUMN_PERPO_PKGNAME + " = ?", new String[]{pkgName});
            } else {
                ContentValues contentvalues = new ContentValues();
                contentvalues.put(TableIndexDefine.COLUMN_PERPO_LABEL, appLabel);
                mdb.update(TableIndexDefine.TABLE_APP_PERPO, contentvalues, TableIndexDefine.
                                      COLUMN_PERPO_PKGNAME + " = ?", new String[]{pkgName});
            }
        }
    }
}
