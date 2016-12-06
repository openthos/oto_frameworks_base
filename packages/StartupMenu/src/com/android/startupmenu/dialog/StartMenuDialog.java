package com.android.startupmenu.dialog;

import com.android.startupmenu.R;
import android.graphics.Color;
import android.R.layout;
import android.os.Bundle;
import android.content.Context;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.app.Dialog;
import android.widget.Toast;
import android.content.Intent;
import android.view.Window;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.View.OnGenericMotionListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.content.ContentValues;
import android.database.Cursor;
import com.android.startupmenu.StartupMenuActivity;
import android.database.sqlite.SQLiteDatabase;
import com.android.startupmenu.util.MySqliteOpenHelper;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.android.startupmenu.adapter.StartupMenuAdapter;
import android.os.Handler;
import android.os.Message;
import android.content.ContentResolver;

public class StartMenuDialog extends Dialog implements OnClickListener {
    public static int STARTMENU_WIDTH = 55;
    public static final int STATE_CODE_SEND_DATA = 0;
    public static final String URI_CONTENT_STATUS_BAR =
                        "content://com.android.systemui.util/status_bar_tb";
    private Context mContext;
    private boolean mFlag;
    private int mPosition;
    private TextView mRightOpen;
    private StartupMenuActivity mStartupMenuActivity;
    private SQLiteDatabase mdb;
    private MySqliteOpenHelper mMsoh;
    private int mListType;
    private String mPkgName;
    private boolean mBooleanFlag;
    private String mStrTextView;
    private TextView mRightFixedTaskbar;
    private boolean mflagChange;
    private String mLockedAppText;
    private String mUnlockedAppText;

    public StartMenuDialog(Context context) {
        super(context);
        mContext = context;
    }

    public StartMenuDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    public void setPosition(int pos) {
        mPosition = pos;
    }

    protected StartMenuDialog(Context context, boolean cancelable,
                              OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.right_click_menu1);

        mMsoh = new MySqliteOpenHelper(mContext, "Application_database.db", null, 1);
        mdb = mMsoh.getWritableDatabase();
        mLockedAppText = mContext.getResources().getString(R.string.lockedapptext);
        mUnlockedAppText = mContext.getResources().getString(R.string.unlockedapptext);
        mRightOpen = (TextView) findViewById(R.id.tv_right_open);
        TextView rightPhoneRun = (TextView) findViewById(R.id.tv_right_phone_run);
        TextView rightDesktopRun = (TextView) findViewById(R.id.tv_right_desktop_run);
        mRightFixedTaskbar = (TextView) findViewById(R.id.tv_right_fixed_taskbar);
        TextView rightUninstall = (TextView) findViewById(R.id.tv_right_uninstall);
        mStrTextView = StartupMenuAdapter.strPkgName;
        new Thread(new QueryCursorData()).start();
        mFlag = true;
        mRightOpen.setOnClickListener(this);
        rightPhoneRun.setOnClickListener(this);
        rightDesktopRun.setOnClickListener(this);
        mRightFixedTaskbar.setOnClickListener(this);
        rightUninstall.setOnClickListener(this);

        mRightOpen.setOnHoverListener(hoverListener);
        rightPhoneRun.setOnHoverListener(hoverListener);
        rightDesktopRun.setOnHoverListener(hoverListener);
        mRightFixedTaskbar.setOnHoverListener(hoverListener);
        rightUninstall.setOnHoverListener(hoverListener);
    }

    public void setEnableOpenwith(boolean can) {
        mFlag = can;
        if (can) {
            mRightOpen.setTextColor(Color.parseColor("#000000"));
        } else {
            mRightOpen.setTextColor(Color.parseColor("#b19898"));
        }
    }

    public void showDialog(int x, int y, int height, int width, int type) {
        mStrTextView = StartupMenuAdapter.strPkgName;
        new Thread(new QueryCursorData()).start();;
        show();
        Window dialogWindow = getWindow();
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
        if (x > (d.getWidth() - dialogWindow.getAttributes().width))
        {
            lp.x = d.getWidth() - dialogWindow.getAttributes().width;
        } else {
            lp.x = x;
        }
        int statusBarHeight = mContext.getResources()
                .getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height_real);
        if (dialogWindow.getAttributes().height < 0
                && y > (d.getHeight() - height)) {
            lp.y = d.getHeight() - height - statusBarHeight;
        } else {
            if (y > (d.getHeight() - dialogWindow.getAttributes().height)) {
                lp.y = d.getHeight() - dialogWindow.getAttributes().height - statusBarHeight;
            } else {
                lp.y = y - STARTMENU_WIDTH;
            }
        }
        lp.width = width;
        lp.height = height;
        //lp.alpha = 0.9f;
        dialogWindow.setAttributes(lp);
        mListType = type;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.tv_right_open:
            Intent intent;
            if (mListType == 0) {
                mPkgName = StartupMenuActivity.mlistAppInfo.get(mPosition).getPkgName();
                intent = StartupMenuActivity.mlistAppInfo.get(mPosition).getIntent();
            } else {
                mPkgName = StartupMenuActivity.mlistViewAppInfo.get(mPosition).getPkgName();
                intent = StartupMenuActivity.mlistViewAppInfo.get(mPosition).getIntent();
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            StartupMenuAdapter.openAppBroadcast(mContext);
            Cursor c = mdb.rawQuery("select * from perpo where pkname = ?",
                                    new String[] { mPkgName });
            c.moveToNext();
            int numbers = c.getInt(c.getColumnIndex("int"));
            numbers++;
            int number = c.getInt(c.getColumnIndex("click"));
            number++;
            ContentValues values = new ContentValues();
            values.put("int", numbers);
            values.put("click", number);
            mdb.update("perpo", values, "pkname = ?", new String[] { mPkgName });
            SharedPreferences sharedPreference = mContext.getSharedPreferences("click",
                                                          Context.MODE_PRIVATE);
            Editor editor = sharedPreference.edit();
            String type = sharedPreference.getString("type", "sortName");
            int order = sharedPreference.getInt("order", 0);
            editor.clear();
            editor.putInt("isClick", 1);
            editor.putString("type", type);
            editor.putInt("order", order);
            editor.commit();
            dialogDismiss();
            break;
        case R.id.tv_right_phone_run:
            runPhoneMode();
            addUsedNum();
            dialogDismiss();
            break;
        case R.id.tv_right_desktop_run:
            runPcMode();
            addUsedNum();
            dialogDismiss();
            break;
        case R.id.tv_right_fixed_taskbar:
            String pkgInfo = StartupMenuActivity.mlistAppInfo.get(mPosition).getPkgName();
            if (mflagChange) {
                Intent intentSend = new Intent();
                intentSend.putExtra("keyInfo", pkgInfo);
                intentSend.setAction(Intent.ACTION_STARTUPMENU_SEND_INFO_LOCK);
                mContext.sendBroadcast(intentSend);
                mRightFixedTaskbar.setText(mUnlockedAppText);
                new Thread(new QueryCursorData()).start();
            } else {
                Intent intentUnlock = new Intent();
                intentUnlock.putExtra("unlockapk", pkgInfo);
                intentUnlock.setAction(Intent.STARTMENU_UNLOCKED);
                mContext.sendBroadcast(intentUnlock);
                mRightFixedTaskbar.setText(mLockedAppText);
                new Thread(new QueryCursorData()).start();
            }
            dialogDismiss();
            break;
        case R.id.tv_right_uninstall:
            if (mListType == 0) {
                mPkgName = StartupMenuActivity.mlistAppInfo.get(mPosition).getPkgName();
            } else {
                mPkgName = StartupMenuActivity.mlistViewAppInfo.get(mPosition).getPkgName();
            }
            Uri uri = Uri.parse("package:" + mPkgName);
            Intent intents = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
            intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intents);
            dialogDismiss();
            break;
        }
    }

    View.OnHoverListener hoverListener = new View.OnHoverListener() {
        public boolean onHover(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundResource(R.color.rightMenuFocus);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundResource(android.R.color.transparent);
                    break;
            }
            return false;
        }
    };

    //Method of run phone mode
    private void runPhoneMode() {
        Intent intent = StartupMenuActivity.mlistAppInfo.get(mPosition).getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_RUN_PHONE_MODE
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
    }

    //Method of run pc mode
    private void runPcMode() {
        Intent intent = StartupMenuActivity.mlistAppInfo.get(mPosition).getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    //Method of save used numbers
    private void addUsedNum() {
        String pkgName = StartupMenuActivity.mlistAppInfo.get(mPosition).getPkgName();
        Cursor cursor = mdb.rawQuery("select * from perpo where pkname = ?",
                                     new String[] { pkgName });
        cursor.moveToNext();
        int numbers = cursor.getInt(cursor.getColumnIndex("int"));
        numbers++;
        int number = cursor.getInt(cursor.getColumnIndex("click"));
        number++;
        ContentValues values = new ContentValues();
        values.put("int", numbers);
        values.put("click",number);
        mdb.update("perpo", values, "pkname = ?", new String[] { pkgName });
        SharedPreferences sharedPreference = mContext.getSharedPreferences("click",
                                                             Context.MODE_PRIVATE);
        Editor editor = sharedPreference.edit();
        editor.clear();
        editor.putInt("isClick",1);
        editor.commit();
    }

    private boolean queryData(String str) {
        Uri uri = Uri.parse(URI_CONTENT_STATUS_BAR);
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String strname = cursor.getString(1);
                if (strname.equals(str)) {
                    return true;
                }
            }
            cursor.close();
        }
        return false;
    }

    private void changeTextViewText(boolean flag) {
        if (flag) {
            mRightFixedTaskbar.setText(mUnlockedAppText);
            mflagChange = false;
        } else {
            mRightFixedTaskbar.setText(mLockedAppText);
            mflagChange = true;
        }
    }

    private void dialogDismiss() {
        dismiss();
        StartupMenuActivity.setFocus(false);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case STATE_CODE_SEND_DATA:
                    changeTextViewText(mBooleanFlag);
                    break;
            }
            return false;
        }
    });

    class QueryCursorData implements Runnable {
        @Override
        public void run() {
            mBooleanFlag = queryData(mStrTextView);
            mHandler.sendEmptyMessage(STATE_CODE_SEND_DATA);
        }
    }
}
