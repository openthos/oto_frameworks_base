package com.android.startupmenu.dialog;

import com.android.startupmenu.R;

import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Context;
import android.app.Dialog;
import android.content.Intent;
import android.os.UserHandle;
import android.view.Window;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.database.Cursor;

import com.android.startupmenu.StartupMenuActivity;

import com.android.startupmenu.bean.AppInfo;
import com.android.startupmenu.util.Constants;
import com.android.startupmenu.util.SqliteOperate;

import android.net.Uri;
import android.provider.Settings;

import android.os.Handler;
import android.os.Message;
import android.content.ContentResolver;

public class AppDialog extends Dialog implements OnTouchListener {
    public static final int STATE_CODE_SEND_DATA = 0;
    public static final String URI_CONTENT_STATUS_BAR =
            "content://com.android.systemui.util/status_bar_tb";
    private StartupMenuActivity mActivity;
    private String mPkgName;
    private String mLockedAppText;
    private String mUnlockedAppText;
    private AppInfo mAppInfo;
    private boolean mBooleanFlag;
    private boolean mIsFullScreen;
    private int mDialogHeight;

    private TextView mOpen;
    private TextView mRunAsPhone;
    private TextView mRunAsDesktop;
    private TextView mRightFixedTaskbar;
    private TextView mUninstall;
    private LinearLayout mLayout;


    public AppDialog(Context context, int themeResId, AppInfo appInfo) {
        super(context, themeResId);
        mActivity = (StartupMenuActivity) context;
        mAppInfo = appInfo;
        mPkgName = appInfo.getPkgName();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.right_click_menu);

        mLockedAppText = mActivity.getResources().getString(R.string.lockedapptext);
        mUnlockedAppText = mActivity.getResources().getString(R.string.unlockedapptext);

        mLayout = (LinearLayout) findViewById(R.id.right_click_menu);
        mOpen = (TextView) findViewById(R.id.tv_right_open);
        mRunAsPhone = (TextView) findViewById(R.id.tv_right_phone_run);
        mRunAsDesktop = (TextView) findViewById(R.id.tv_right_desktop_run);
        mRightFixedTaskbar = (TextView) findViewById(R.id.tv_right_fixed_taskbar);
        mUninstall = (TextView) findViewById(R.id.tv_right_uninstall);

        new Thread(new QueryCursorData()).start();

        mIsFullScreen = ApplicationInfo.isMaximizedStyleWindow(mPkgName) ||
                ApplicationInfo.isRealFullScreenStyleWindow(mPkgName);
        if (mIsFullScreen) {
            mRunAsPhone.setEnabled(false);
            mRunAsPhone.setTextColor(Color.parseColor(Constants.TEXT_COLOR_GRAY));
        }
        mOpen.setOnTouchListener(this);
        mRunAsPhone.setOnTouchListener(this);

        mRunAsDesktop.setOnTouchListener(this);
        mRightFixedTaskbar.setOnTouchListener(this);
        mUninstall.setOnTouchListener(this);

        mOpen.setOnHoverListener(hoverListener);
        mRunAsPhone.setOnHoverListener(hoverListener);
        mRunAsDesktop.setOnHoverListener(hoverListener);
        mRightFixedTaskbar.setOnHoverListener(hoverListener);
        mUninstall.setOnHoverListener(hoverListener);

        mLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mDialogHeight = mLayout.getMeasuredHeight();
    }

    public void setEnableOpenwith(boolean can) {
        if (can) {
            mOpen.setTextColor(Color.parseColor("#000000"));
        } else {
            mOpen.setTextColor(Color.parseColor("#b19898"));
        }
    }

    public void showDialog(int x, int y) {
        Window dialogWindow = getWindow();
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        show();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
        lp.x = x;
        if (y > (d.getHeight() - mDialogHeight)) {
            lp.y = y - mDialogHeight;
        } else {
            lp.y = y;
        }
        dialogWindow.setAttributes(lp);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()) {
            case R.id.tv_right_open:
                Intent intent = mAppInfo.getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(intent);
                openAppBroadcast();
                dialogDismiss();
                SqliteOperate.updateDataStorage(mActivity, mAppInfo);
                break;
            case R.id.tv_right_phone_run:
                runPhoneMode();
                dialogDismiss();
                SqliteOperate.updateDataStorage(mActivity, mAppInfo);
                break;
            case R.id.tv_right_desktop_run:
                runPcMode();
                dialogDismiss();
                SqliteOperate.updateDataStorage(mActivity, mAppInfo);
                break;
            case R.id.tv_right_fixed_taskbar:
                if (mRightFixedTaskbar.getText().toString().equals(mLockedAppText)) {
                    Intent intentSend = new Intent();
                    intentSend.putExtra("keyInfo", mPkgName);
                    intentSend.setAction(Constants.ACTION_STARTUPMENU_SEND_INFO_LOCK);
                    mActivity.sendBroadcast(intentSend);
                    mRightFixedTaskbar.setText(mUnlockedAppText);
                } else {
                    Intent intentUnlock = new Intent();
                    intentUnlock.putExtra("unlockapk", mPkgName);
                    intentUnlock.setAction(Constants.STARTMENU_UNLOCKED);
                    mActivity.sendBroadcast(intentUnlock);
                    mRightFixedTaskbar.setText(mLockedAppText);
                }
                dialogDismiss();
                break;
            case R.id.tv_right_uninstall:
                Uri uri = Uri.parse("package:" + mPkgName);
                Intent intents = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(intents);
                dialogDismiss();
                break;
        }
        return true;
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

    public void openAppBroadcast() {
        Intent openAppIntent = new Intent();
        openAppIntent.setAction(Constants.ACTION_OPEN_APPLICATION);
        mActivity.sendBroadcastAsUser(openAppIntent, UserHandle.ALL);
    }

    //Method of run phone mode
    private void runPhoneMode() {
        Intent intent = mAppInfo.getIntent();
        intent.addFlags(Constants.FLAG_ACTIVITY_RUN_PHONE_MODE
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mActivity.startActivity(intent);
    }

    //Method of run pc mode
    private void runPcMode() {
        Intent intent = mAppInfo.getIntent();
        intent.addFlags(Constants.FLAG_ACTIVITY_RUN_PC_MODE
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mActivity.startActivity(intent);
    }

    private boolean queryData(String str) {
        Uri uri = Uri.parse(URI_CONTENT_STATUS_BAR);
        ContentResolver contentResolver = mActivity.getContentResolver();
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

    private void dialogDismiss() {
        dismiss();
        mActivity.setFocus(false);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case STATE_CODE_SEND_DATA:
                    if (mBooleanFlag) {
                        mRightFixedTaskbar.setText(mUnlockedAppText);
                    } else {
                        mRightFixedTaskbar.setText(mLockedAppText);
                    }
                    break;
            }
            return false;
        }
    });

    class QueryCursorData implements Runnable {
        @Override
        public void run() {
            mBooleanFlag = queryData(mPkgName);
            mHandler.sendEmptyMessage(STATE_CODE_SEND_DATA);
        }
    }
}
