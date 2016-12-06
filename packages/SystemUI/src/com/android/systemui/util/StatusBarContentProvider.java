package com.android.systemui.util;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/*
 * Created by CaoYongren ,by 2016-12-5
*/

public class StatusBarContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.android.systemui.util";
    public static final int MATCH_CODE = 1;
    UriMatcher mUriMatcher;

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public boolean onCreate() {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, "status_bar_tb", MATCH_CODE);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        SQLiteDatabase db = getContext().openOrCreateDatabase("Status_bar_database.db",
                                                             Context.MODE_PRIVATE, null);
        int code = mUriMatcher.match(uri);
        Cursor cursor = null;
        switch (code) {
           case MATCH_CODE:
               cursor = db.rawQuery("select * from status_bar_tb", null);
           break;
        }
        return cursor;
    }
}
