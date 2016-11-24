package com.android.startupmenu.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.startupmenu.util.MySqliteOpenHelper;
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

import android.content.ContentValues;
import android.database.Cursor;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;
import android.content.Intent;

public class MySQLReceiver extends BroadcastReceiver {
    private MySqliteOpenHelper mMsoh;
    private SQLiteDatabase mdb;
    private boolean mIsHasReayDb;
    private int mNumber;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_STARTMENU_SEND_SQLITE_INFO)) {
            BackstageRenewalData(context);
        }

        //Accept Message
        if (intent.getAction().equals(Intent.ACTION_SEND_CLICK_INFO)) {
            mMsoh = new MySqliteOpenHelper(context, "Application_database.db", null, 1);
            mdb = mMsoh.getWritableDatabase();
            String pkgName = intent.getStringExtra("keyAddInfo");
            Cursor c = mdb.rawQuery("select * from perpo where pkname = ?",
                      new String[] { pkgName });
            ContentValues values = new ContentValues();
            int numbers = 0;
            int number = 0;
            if (c.moveToNext()) {
                numbers = c.getInt(c.getColumnIndex("int"));
                number = c.getInt(c.getColumnIndex("click"));
                numbers ++;
                number ++;
                values.put("int", numbers);
                values.put("click", number);
                mdb.update("perpo", values, "pkname = ?", new String[] { pkgName });
            } else {
                numbers ++;
                number ++;
                values.put("pkname", pkgName);
                values.put("int", numbers);
                values.put("click", number);
                mdb.insert("perpo", "pkname", values);
                BackstageRenewalData(context);
            }
            //Same to open run
            SharedPreferences sharedPreference = context.getSharedPreferences("click",
                                                          Context.MODE_PRIVATE);
            Editor editor = sharedPreference.edit();
            String type = sharedPreference.getString("type", "sortName");
            int order = sharedPreference.getInt("order", 0);
            editor.clear();
            editor.putInt("isClick", 1);
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
            Cursor c = mdb.rawQuery("select * from perpo where pkname = ?",
                    new String[] { pkgName });
            while (c.moveToNext()) {
                String pkname = c.getString(c.getColumnIndex("pkname"));
                if (pkgName.equals(pkname)) {
                    mIsHasReayDb = true;
                    break;
                }
            }

            if (!mIsHasReayDb) {
                mdb.execSQL("insert into perpo(label,pkname,date,int,click) "
                                + "values (?,?,?,?,?)",
                        new Object[] { appLabel, pkgName, systemDate,
                                mNumber,mNumber});
            }
            if(isEnglish(appLabel)) {
                ContentValues contentvalues = new ContentValues();
                contentvalues.put("label", appLabel);
                mdb.update("perpo", contentvalues, "pkname = ?", new String[]{ pkgName });
            } else {
                ContentValues contentvalues = new ContentValues();
                contentvalues.put("label", appLabel);
                mdb.update("perpo", contentvalues, "pkname = ?", new String[]{ pkgName });
            }
        }
    }
}
