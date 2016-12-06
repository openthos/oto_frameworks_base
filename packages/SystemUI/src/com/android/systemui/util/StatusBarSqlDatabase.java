package com.android.systemui.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by CaoYongren on 2016-12-5.
*/

public class StatusBarSqlDatabase extends SQLiteOpenHelper {

    public StatusBarSqlDatabase(Context context, String name,
                         SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table status_bar_tb(_id integer primary key autoincrement,"
                                                          + "pkgname varchar(100))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
