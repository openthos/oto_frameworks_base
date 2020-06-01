package com.android.systemui.calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLHelper {
    private static String SQL_NAME = "calendar_db";
    private static String TABLE_NAME = "notice_db";

    public static void createSql(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context, SQL_NAME, null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
    }

    public static void insertNotice(Context context, String message, String time, String level) {
        ContentValues values = new ContentValues();
        values.put("message", message);
        values.put("time", time);
        values.put("level", level);
        DatabaseHelper dbHelper = new DatabaseHelper(context, SQL_NAME, null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public static void updateNotice(Context context, String id, String message, String time,
                                       String level) {
        DatabaseHelper dbHelper = new DatabaseHelper(context, SQL_NAME, null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("message", message);
        values.put("time", time);
        values.put("level", level);
        db.update(TABLE_NAME, values, "_id=?", new String[]{id});
        db.close();
    }

    public static List<Map<String, String>> queryAllNotices(Context context, int tableCount) {
        List<Map<String, String>> tempList = new ArrayList<Map<String, String>>();
        DatabaseHelper dbHelper = new DatabaseHelper(context, SQL_NAME, null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String _id = cursor.getString(cursor.getColumnIndex("_id"));
                String message = cursor.getString(cursor.getColumnIndex("title"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String level = cursor.getString(cursor.getColumnIndex("location"));
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ID", _id);
                map.put("MESSAGE", message);
                map.put("TIME", time);
                map.put("LEVEL", level);
                tempList.add(map);
            }
            db.close();
        }
        return tempList;
    }

    public static List<Map<String, String>> queryAllData(Context context) {
        List<Map<String, String>> tempList = new ArrayList<Map<String, String>>();
        DatabaseHelper dbHelper = new DatabaseHelper(context, SQL_NAME, null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String _id = cursor.getString(cursor.getColumnIndex("_id"));
            String message = cursor.getString(cursor.getColumnIndex("title"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            String level = cursor.getString(cursor.getColumnIndex("location"));
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("ID", _id);
            map.put("MESSAGE", message);
            map.put("TIME", time);
            map.put("LEVEL", level);
            tempList.add(map);
        }
        return tempList;
    }

    public static void deleteNotice(Context context, String id, int tableCount) {
        DatabaseHelper dbHelper = new DatabaseHelper(context, SQL_NAME, null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, "_id=?", new String[]{id});
        db.close();
    }

    public static void deleteAll(Context context, int tableCount) {
        DatabaseHelper dbHelper = new DatabaseHelper(context, SQL_NAME, null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}
