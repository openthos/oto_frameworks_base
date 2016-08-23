package com.android.documentsui.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.documentsui.util.MySqliteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class MyInstalledReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String pkName = intent.getData().getSchemeSpecificPart();
            Log.i("openthos", "Install app package name :" + pkName);
        }
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String pkName = intent.getData().getSchemeSpecificPart();
            MySqliteOpenHelper mMsoh = new MySqliteOpenHelper(context, "Application_database.db",
                                                              null, 1);
            SQLiteDatabase db = mMsoh.getWritableDatabase();
            db.delete("perpo", "pkname = ? ", new String[] { pkName });
        }
    }
}
