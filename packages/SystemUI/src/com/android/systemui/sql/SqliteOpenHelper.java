package com.android.systemui.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteOpenHelper extends SQLiteOpenHelper {
    private static final int SQL_VERSION_CODE = 3;
    private static final String SQL_NAME = "SYSTEMUI_DATABASE";

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
        db.execSQL("create table " + StartMenuDatabaseField.TABLE_NAME + "("
                + StartMenuDatabaseField.COLUMN_ID + " integer primary key autoincrement,"
                + StartMenuDatabaseField.COLUMN_LABEL + " char(20),"
                + StartMenuDatabaseField.COLUMN_PACKAGENAME + " char(100),"
                + StartMenuDatabaseField.COLUMN_ACTIVITYNAME + " char(100),"
                + StartMenuDatabaseField.COLUMN_INSTALL_TIME + " integer,"
                + StartMenuDatabaseField.COLUMN_USECOUNT + " char(10)"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("drop table " + StartMenuDatabaseField.TABLE_NAME);
            onCreate(db);
        }
    }
}
