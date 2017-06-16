package com.android.startupmenu.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteOpenHelper extends SQLiteOpenHelper {
    private static final int SQL_VERSION_CODE = 3;
    private static final String SQL_NAME = "StartupMenu_database.db";

    private static SqliteOpenHelper sqliteOpenHelper;

    private SqliteOpenHelper(Context context) {
        super(context, SQL_NAME, null, SQL_VERSION_CODE);
    }

    public static SqliteOpenHelper getInstance(Context context) {
        if (sqliteOpenHelper == null) {
            sqliteOpenHelper = new SqliteOpenHelper(context);
        }
        return sqliteOpenHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TableIndexDefine.TABLE_APP_PERPO + "(" + TableIndexDefine.
                COLUMN_PERPO_ID + " integer primary key autoincrement," + TableIndexDefine.
                COLUMN_PERPO_LABEL + " char(20)," + TableIndexDefine.
                COLUMN_PERPO_PKGNAME + " char(100)," + TableIndexDefine.
                COLUMN_PERPO_INSTALL_DATE + " integer," + TableIndexDefine.
                COLUMN_PERPO_CLICK_NUM + " char(10)" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("drop table " + TableIndexDefine.TABLE_APP_PERPO);
            onCreate(db);
        }
    }
}
