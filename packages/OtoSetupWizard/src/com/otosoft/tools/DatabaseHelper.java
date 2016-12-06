package com.otosoft.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.RadioButton;

/**
 * Created by wanglifeng on 16-8-9.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "runmode.db";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "create table selectState(_id integer primary key autoincrement,"
                                              + "appPackage varchar, state int)";
        String sqlOpenthosID = "create table openthosID(_id integer primary key autoincrement,"
                                + "openthosID varchar, password varchar)";

        String sqlUpgradeUrl = "create table upgradeUrl(_id integer primary key autoincrement,"
                                  + "upgradeUrl varchar)";

        sqLiteDatabase.execSQL(sql);
        sqLiteDatabase.execSQL(sqlOpenthosID);
        sqLiteDatabase.execSQL(sqlUpgradeUrl);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
