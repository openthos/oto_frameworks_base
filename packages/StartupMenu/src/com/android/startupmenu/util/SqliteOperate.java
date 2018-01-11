package com.android.startupmenu.util;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;

import com.android.startupmenu.bean.AppInfo;

/*
* Created by Cao Yongren on 15-5-2017
*/

public class SqliteOperate {
    /*
    * When run app, need change SqlDataBase and sharedPreference.
    */
    public static void updateDataStorage(Context context, AppInfo appInfo) {
        SqliteOpenHelper sqliteOpenHelper = SqliteOpenHelper.getInstance(context);
        SQLiteDatabase db = sqliteOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TableIndexDefine.TABLE_APP_PERPO +
                " where " + TableIndexDefine.COLUMN_PERPO_PKGNAME +
                " = ? ", new String[]{appInfo.getPkgName()});
        ContentValues values = new ContentValues();
        if (cursor.moveToNext()) {
            int numbers = cursor.getInt(cursor.getColumnIndex(
                    TableIndexDefine.COLUMN_PERPO_CLICK_NUM));
            values.put(TableIndexDefine.COLUMN_PERPO_CLICK_NUM, ++numbers);
            db.update(TableIndexDefine.TABLE_APP_PERPO, values, TableIndexDefine.
                    COLUMN_PERPO_PKGNAME + " = ?", new String[]{appInfo.getPkgName()});
        } else {
            values.put(TableIndexDefine.COLUMN_PERPO_LABEL, appInfo.getAppLabel());
            values.put(TableIndexDefine.COLUMN_PERPO_PKGNAME, appInfo.getPkgName());
            values.put(TableIndexDefine.COLUMN_PERPO_INSTALL_DATE, appInfo.getInstallTime());
            values.put(TableIndexDefine.COLUMN_PERPO_CLICK_NUM, 1);
            db.insert(TableIndexDefine.TABLE_APP_PERPO, null, values);
        }
        cursor.close();
        db.close();
    }

    public static void deleteDataStorage(Context context, String pkgName) {
        SqliteOpenHelper sqliteOpenHelper = SqliteOpenHelper.getInstance(context);
        SQLiteDatabase db = sqliteOpenHelper.getWritableDatabase();
        db.delete(TableIndexDefine.TABLE_APP_PERPO, TableIndexDefine.COLUMN_PERPO_PKGNAME
                + " = ? ", new String[]{pkgName});
        db.close();
    }
}
