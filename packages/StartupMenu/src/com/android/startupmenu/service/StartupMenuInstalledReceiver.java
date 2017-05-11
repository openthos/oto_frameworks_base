package com.android.startupmenu.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.startupmenu.util.StartupMenuSqliteOpenHelper;
import com.android.startupmenu.util.TableIndexDefine;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import static com.android.startupmenu.StartupMenuActivity.isEnglish;

public class StartupMenuInstalledReceiver extends BroadcastReceiver {
    private StartupMenuSqliteOpenHelper mMsoh;
    private SQLiteDatabase mdb;
    private boolean mIsHasReayDb;
    private int mNumber;
    @Override
    public void onReceive(Context context, Intent intent) {
        mMsoh = new StartupMenuSqliteOpenHelper(context, "StartupMenu_database.db", null, 1);
        mdb = mMsoh.getWritableDatabase();
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String pkName = intent.getData().getSchemeSpecificPart();
            Log.i("openthos", "Install app package name :" + pkName);
            BackstageRenewalData(context);
        }
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String pkName = intent.getData().getSchemeSpecificPart();
            mdb.delete(TableIndexDefine.TABLE_APP_PERPO, TableIndexDefine.COLUMN_PERPO_PKGNAME
                                                         + " = ? ", new String[] { pkName });
        }
    }

    public void BackstageRenewalData(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
        for (ResolveInfo reInfo : resolveInfos) {
            File file = new File(reInfo.activityInfo.applicationInfo.sourceDir);
            Date systemDate = new Date(file.lastModified());
            ApplicationInfo applicationInfo = reInfo.activityInfo.applicationInfo;
            String activityName = reInfo.activityInfo.name;
            String pkgName = reInfo.activityInfo.packageName;
            String appLabel = (String) reInfo.loadLabel(pm);
            Drawable icon = reInfo.loadIcon(pm);
            mIsHasReayDb = false;
            Cursor c = mdb.rawQuery("select * from " + TableIndexDefine.TABLE_APP_PERPO +
                                    " where " + TableIndexDefine.COLUMN_PERPO_PKGNAME + " = ?",
                                                new String[] { pkgName });
            while (c.moveToNext()) {
                String pkname = c.getString(c.getColumnIndex(
                                              TableIndexDefine.COLUMN_PERPO_PKGNAME));
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
                    "values (?, ?, ?, ?)" , new Object[] {
                    appLabel, pkgName, systemDate, mNumber} );
            }

            if(isEnglish(appLabel)) {
                ContentValues values = new ContentValues();
                values.put(TableIndexDefine.COLUMN_PERPO_LABEL, appLabel);
                mdb.update(TableIndexDefine.TABLE_APP_PERPO, values,
                        TableIndexDefine.COLUMN_PERPO_PKGNAME + " = ?", new String[]{ pkgName });
            } else {
                ContentValues values = new ContentValues();
                values.put(TableIndexDefine.COLUMN_PERPO_LABEL, appLabel);
                mdb.update(TableIndexDefine.TABLE_APP_PERPO, values,
                           TableIndexDefine.COLUMN_PERPO_PKGNAME + " = ?", new String[]{ pkgName });
            }
        }
    }
}
