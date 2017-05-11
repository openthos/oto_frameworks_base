package com.android.startupmenu.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class StartupMenuSqliteOpenHelper extends SQLiteOpenHelper {

    public StartupMenuSqliteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
        db.execSQL("create table perpo(_id integer primary key autoincrement, "
                                       + "label char(20), pkname char(100), date char(50), "
                                       + "int char(10), click char(10))");
        */
        db.execSQL("create table " + TableIndexDefine.TABLE_APP_PERPO + "(" + TableIndexDefine.
                    COLUMN_PERPO_ID + " integer primary key autoincrement," + TableIndexDefine.
                    COLUMN_PERPO_LABEL + " char(20)," + TableIndexDefine.
                    COLUMN_PERPO_PKGNAME + " char(100)," + TableIndexDefine.
                    COLUMN_PERPO_INSTALL_DATE + " char(50)," + TableIndexDefine.
                    COLUMN_PERPO_CLICK_NUM + " char(10)" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
