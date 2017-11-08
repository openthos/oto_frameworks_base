package com.android.systemui.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.systemui.dialog.TaskbarIcon;
import com.android.systemui.startupmenu.AppEntry;

/*
* Created by Cao Yongren on 15-5-2017
*/

public class SqliteOperate {
    /*
    * When run app, need change SqlDataBase and sharedPreference.
    */
    public static void updateDataStorage(Context context, AppEntry appInfo) {
        SqliteOpenHelper sqliteOpenHelper = SqliteOpenHelper.getInstance(context);
        SQLiteDatabase db = sqliteOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + StartMenuDatabaseField.TABLE_NAME +
                " where " + StartMenuDatabaseField.COLUMN_PACKAGENAME +
                " = ? ", new String[]{appInfo.getPackageName()});
        ContentValues values = new ContentValues();
        if (cursor.moveToNext()) {
            int numbers = cursor.getInt(cursor.getColumnIndex(
                    StartMenuDatabaseField.COLUMN_USECOUNT));
            values.put(StartMenuDatabaseField.COLUMN_USECOUNT, ++numbers);
            db.update(StartMenuDatabaseField.TABLE_NAME, values, StartMenuDatabaseField.
                    COLUMN_PACKAGENAME + " = ?", new String[]{appInfo.getPackageName()});
        } else {
            values.put(StartMenuDatabaseField.COLUMN_LABEL, appInfo.getLabel());
            values.put(StartMenuDatabaseField.COLUMN_PACKAGENAME, appInfo.getPackageName());
            values.put(StartMenuDatabaseField.COLUMN_ACTIVITYNAME, appInfo.getActivityName());
            values.put(StartMenuDatabaseField.COLUMN_INSTALL_TIME, appInfo.getInstallTime());
            values.put(StartMenuDatabaseField.COLUMN_USECOUNT, 1);
            db.insert(StartMenuDatabaseField.TABLE_NAME, null, values);
        }
        cursor.close();
        db.close();
    }

    public static void deleteDataStorage(Context context, String pkgName) {
        SqliteOpenHelper sqliteOpenHelper = SqliteOpenHelper.getInstance(context);
        SQLiteDatabase db = sqliteOpenHelper.getWritableDatabase();
        db.delete(StartMenuDatabaseField.TABLE_NAME, StartMenuDatabaseField.COLUMN_PACKAGENAME
                + " = ? ", new String[]{pkgName});
        db.close();
    }

    public static void saveTaskbarIcon(Context context, TaskbarIcon taskbarIcon){
        SqliteOpenHelper sqliteOpenHelper = SqliteOpenHelper.getInstance(context);
        SQLiteDatabase db = sqliteOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TaskbarIconField.TABLE_NAME +
                " where " + TaskbarIconField.COMPONENT_NAME +
                " = ? ", new String[]{taskbarIcon.getComponentName().flattenToString()});
        ContentValues values = new ContentValues();
        if (!cursor.moveToNext()) {
            values.put(TaskbarIconField.USER_ID, taskbarIcon.getUserId(context));
            values.put(TaskbarIconField.COMPONENT_NAME,
                    taskbarIcon.getComponentName().flattenToString());
            db.insert(TaskbarIconField.TABLE_NAME, null, values);
        }
        cursor.close();
        db.close();
    }

    public static void deleteTaskbarIcon(Context context, String compontentName) {
        SqliteOpenHelper sqliteOpenHelper = SqliteOpenHelper.getInstance(context);
        SQLiteDatabase db = sqliteOpenHelper.getWritableDatabase();
        db.delete(TaskbarIconField.TABLE_NAME, TaskbarIconField.COMPONENT_NAME
                + " = ? ", new String[]{compontentName});
        db.close();
    }
}
