package com.android.startupmenu.dialog;

import com.android.startupmenu.StartupMenuActivity;
import com.android.startupmenu.R;

import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Context;
import android.app.Dialog;
import android.os.UserHandle;
import android.widget.Toast;
import android.content.Intent;
import android.view.Window;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.startupmenu.bean.AppInfo;
import com.android.startupmenu.util.Constants;
import com.android.startupmenu.util.SqliteOperate;

public class CommonAppDialog extends Dialog implements OnClickListener {
    private TextView mOpen;
    private StartupMenuActivity mActivity;
    private AppInfo mAppInfo;
    private boolean mIsFullScreen;
    private String mPkgName;

    private TextView mRunAsPhone;
    private TextView mRunAsDesktop;
    private TextView mRemove;

    public CommonAppDialog(Context context, int themeResId, AppInfo appInfo) {
        super(context, themeResId);
        mAppInfo = appInfo;
        mActivity = (StartupMenuActivity) context;
        mPkgName = mAppInfo.getPkgName();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.right_click_usually_menu);

        mOpen = (TextView) findViewById(R.id.tv_right_usually_open);
        mRunAsPhone = (TextView) findViewById(R.id.tv_right_phone_usually_run);
        mRunAsDesktop = (TextView) findViewById(
                R.id.tv_right_desktop_usually_run);
        mRemove = (TextView) findViewById(R.id.tv_removed_list);

        mIsFullScreen = ApplicationInfo.isMaximizedStyleWindow(mPkgName) ||
                ApplicationInfo.isRealFullScreenStyleWindow(mPkgName);
        if (mIsFullScreen) {
            mRunAsPhone.setEnabled(false);
            mRunAsPhone.setTextColor(Color.parseColor(Constants.TEXT_COLOR_GRAY));
        }

        mOpen.setOnClickListener(this);
        mRunAsPhone.setOnClickListener(this);
        mRunAsDesktop.setOnClickListener(this);
        mRemove.setOnClickListener(this);

        mOpen.setOnHoverListener(hoverListener);
        mRunAsPhone.setOnHoverListener(hoverListener);
        mRunAsDesktop.setOnHoverListener(hoverListener);
        mRemove.setOnHoverListener(hoverListener);
    }

    public void setEnableOpenwith(boolean can) {
        if (can) {
            mOpen.setTextColor(Color.parseColor("#000000"));
        } else {
            mOpen.setTextColor(Color.parseColor("#b19898"));
        }
    }

    public void showDialog(int x, int y) {
        show();
        Window dialogWindow = getWindow();
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        lp.x = x;
        lp.y = y;
        dialogWindow.setAttributes(lp);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_right_usually_open:
                Intent intent = mAppInfo.getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(intent);
                openAppBroadcast();
                SqliteOperate.updateDataStorage(mActivity, mAppInfo);
                dialogDismiss();
                break;
            case R.id.tv_right_phone_usually_run:
                runPhoneMode();
                SqliteOperate.updateDataStorage(mActivity, mAppInfo);
                dialogDismiss();
                break;
            case R.id.tv_right_desktop_usually_run:
                runPcMode();
                SqliteOperate.updateDataStorage(mActivity, mAppInfo);
                dialogDismiss();
                break;
            case R.id.tv_removed_list:
                String label = mAppInfo.getAppLabel();
                Toast.makeText(mActivity, mActivity.getString(R.string.remove_application) + label,
                        Toast.LENGTH_SHORT).show();
                mActivity.mCommonAppInfos.remove(mAppInfo);
                mActivity.mListAdapter.notifyDataSetChanged();
                cancel();
                SqliteOperate.deleteDataStorage(mActivity, mAppInfo.getPkgName());
                dialogDismiss();
                break;
        }
    }

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

    private void dialogDismiss() {
        dismiss();
        mActivity.setFocus(false);
    }
}
