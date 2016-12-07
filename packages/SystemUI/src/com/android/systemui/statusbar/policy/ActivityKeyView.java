
package com.android.systemui.statusbar.policy;

import android.app.Dialog;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.Window;
import com.android.internal.statusbar.StatusbarActivity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.widget.GridView;
import android.view.KeyEvent;
import com.android.systemui.R;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.MotionEvent;
import android.content.pm.PackageManager;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

public class ActivityKeyView extends ImageView {

    private static final int DIALOG_OFFSET_PART = 3; // divide 3
    private static final int DIALOG_PADDING_TIPS = 30;
    private static final int TIMER_NUMBERS = 1000;
    private static final int DIALOG_OFFSET_DIMENSIONS = 20;
    private int mChangeDimension = 0;

    //OnClickListener mOpen;     /* Use to open activity by mPkgName fo related StatusbarActivity. */
    //OnClickListener mClose;     /* Use to close window like mCloseBtn of window header. */
    //OnClickListener mDock;      /* Use to dock related StatusbarActivity in status bar. */
    //OnClickListener mUnDock;    /* Use to undock related StatusbarAcitivity from status bar. */
    OnTouchListener mOpen;
    OnTouchListener mClose;
    OnTouchListener mDock;
    OnTouchListener mUnDock;
    OnTouchListener mPhoneMode;
    OnTouchListener mPcMode;

    StatusbarActivity mActivity;    /* Related StatusbarActivity. */
    View mFocusedView;
    View mRunningView;

    private static final String TAG = "ActivityKeyView";
    private static Dialog mDialog = null;   /* Define a Singleton dialog as tools. */
    private static boolean mShowRBM = false;

    public ActivityKeyView(Context context) {
        super(context);
        initListener();
    }

    public ActivityKeyView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initListener();
    }

    public ActivityKeyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
        initListener();
    }

    public void initListener() {
        mOpen = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                waitTimer();
                runApkByPkg();
                dismissDialog();
                return true;
            }
        };

        mClose = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                try {
                    ActivityManagerNative.getDefault().closeActivity(mActivity.mStackId);
                } catch (RemoteException e) {
                    Log.e(TAG, "Close button failes", e);
                }
                dismissDialog();
                return true;
            }
        };

        mDock = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!mActivity.mIsDocked) {
                    mActivity.mIsDocked = true;
                    sendLockedInfo();
                }
                dismissDialog();
                return true;
            }
        };

        mUnDock = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mActivity.mIsDocked = false;
                dismissDialog();
                removeFromRoot();
                return true;
            }
        };

        mPhoneMode = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                waitTimer();
                runPhoneMode();
                dismissDialog();
                return true;
            }
        };

        mPcMode = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                waitTimer();
                runPcMode();
                dismissDialog();
                return true;
            }
        };
        /* mOpen = new OnClickListener() {
            @Override
            public void onClick(View v) {
                waitTimer();
                runApkByPkg();
                dismissDialog();
            }
        };

        mClose = new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ActivityManagerNative.getDefault().closeActivity(mActivity.mStackId);
                } catch (RemoteException e) {
                    Log.e(TAG, "Close button failes", e);
                }
                dismissDialog();
            }
        };

        mDock = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mActivity.mIsDocked) {
                    mActivity.mIsDocked = true;
                    sendLockedInfo();
                }
                dismissDialog();
            }
        };

        mUnDock = new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.mIsDocked = false;
                dismissDialog();
                removeFromRoot();
            }
        };*/

        setOnHoverListener(new HoverListener());
    }

    public void removeFromRoot() {
        if (!mActivity.mApkRun) { //no run
            this.setVisibility(View.GONE);
            // use to tell phoneStartmenu reduce one
            sendRemoveInfo();
        } else {  // run
            sendRemoveInfo();
        }
    }

    public void sendRemoveInfo() {
        Intent intentIcon = new Intent();
        intentIcon.putExtra("rmIcon",mActivity.mPkgName);
        intentIcon.setAction(Intent.ACTION_SYSTEMUI_SEND_INFO_UNLOCK);
        mContext.sendBroadcast(intentIcon);
    }

    public void sendLockedInfo() {
        Intent intentlock = new Intent();
        intentlock.putExtra("lockIcon", mActivity.mPkgName);
        intentlock.setAction(Intent.ACTION_SYSTEMUI_SEND_INFO_LOCK);
        mContext.sendBroadcast(intentlock);
    }

    public void activityStart(int stackId) {
        mActivity.mStackId = stackId;
        mActivity.mApkRun = true;
        mActivity.mHiden = false;
    }

    public StatusbarActivity getStatusbarActivity() {
        return mActivity;
    }

    public void saveStackInfo(Rect rect) {
        mActivity.mRestoreRect = rect;
        mActivity.mHiden = true;
    }

    public void activityClosed() {
        mActivity.mApkRun = false;
    }

    public View getRbmView() {
        LayoutInflater li =
                        (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(mActivity.mIsDocked) {
            if(mActivity.mApkRun) {
                mChangeDimension = DIALOG_OFFSET_DIMENSIONS;
                return buildRbmDockedRun(li);
            } else {
                return buildRbmDocked(li);
            }
        } else {
            mChangeDimension = DIALOG_OFFSET_DIMENSIONS;
            return buildRbmRun(li);
        }
    }

    public View buildRbmDocked(LayoutInflater li) {
        View rbmDocked = li.inflate(R.layout.right_button_menu_docked, null, false);
        rbmDocked.setFocusable(true);
        TextView open = (TextView) rbmDocked.findViewById(R.id.rbm_open);
        //open.setOnClickListener(mOpen);
        open.setOnTouchListener(mOpen);
        open.setOnHoverListener(hoverListener);
        TextView undock = (TextView) rbmDocked.findViewById(R.id.rbm_undock);
        undock.setOnTouchListener(mUnDock);
        undock.setOnHoverListener(hoverListener);
        TextView phoneMode = (TextView) rbmDocked.findViewById(R.id.rbm_phone_mode);
        phoneMode.setOnTouchListener(mPhoneMode);
        phoneMode.setOnHoverListener(hoverListener);
        TextView pcMode = (TextView) rbmDocked.findViewById(R.id.rbm_pc_mode);
        pcMode.setOnTouchListener(mPcMode);
        pcMode.setOnHoverListener(hoverListener);
        //undock.setOnClickListener(mUnDock);
        return rbmDocked;
    }

    public View buildRbmDockedRun(LayoutInflater li) {
        View rbmDockedRun = li.inflate(R.layout.right_button_menu_docked_run, null, false);
        rbmDockedRun.setFocusable(true);
        TextView close = (TextView) rbmDockedRun.findViewById(R.id.rbm_close);
        //close.setOnClickListener(mClose);
        close.setOnTouchListener(mClose);
        close.setOnHoverListener(hoverListener);
        TextView undock = (TextView) rbmDockedRun.findViewById(R.id.rbm_undock);
        //undock.setOnClickListener(mUnDock);
        undock.setOnTouchListener(mUnDock);
        return rbmDockedRun;
    }

    public View buildRbmRun(LayoutInflater li) {
        View rbmRun = li.inflate(R.layout.right_button_menu_run, null, false);
        rbmRun.setFocusable(true);
        TextView close = (TextView) rbmRun.findViewById(R.id.rbm_close);
        //close.setOnClickListener(mClose);
        close.setOnTouchListener(mClose);
        close.setOnHoverListener(hoverListener);
        TextView dock = (TextView) rbmRun.findViewById(R.id.rbm_dock);
        //dock.setOnClickListener(mDock);
        dock.setOnTouchListener(mDock);
        dock.setOnHoverListener(hoverListener);
        return rbmRun;
    }

    public static void dismissDialog() {
        dismissDialog(false);
    }

    public static void dismissDialog(boolean fromHover) {
        if (fromHover && mShowRBM) {
            return;
        }
        mShowRBM = false;
        if ((mDialog == null) || !mDialog.isShowing()) {
            return;
        }
        mDialog.dismiss();
    }

    public static boolean preventResponseHover() {
        return mShowRBM && mDialog.isShowing();
    }

    private void showDialog(View view, int padding) {
        if(mDialog == null) {
            mDialog = new Dialog(mContext);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
            mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        mDialog.setContentView(view);

        Window dw = mDialog.getWindow();
        WindowManager.LayoutParams lp = dw.getAttributes();
        int dpx = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE))
                                                                  .getDefaultDisplay().getWidth();
        int dpy = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE))
                                                                 .getDefaultDisplay().getHeight();
        int iconSize = getResources().getDimensionPixelSize(R.dimen.status_bar_icon_size_big);
        int[] location = new int[2];

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                     View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        getLocationOnScreen(location);
        lp.x = location[0] - dpx / 2 + iconSize - iconSize / DIALOG_OFFSET_PART;
        lp.y = location[1] - dpy / 2 - view.getMeasuredHeight() + DIALOG_OFFSET_DIMENSIONS
                                                                - padding;
        lp.width = LayoutParams.WRAP_CONTENT;
        lp.height = LayoutParams.WRAP_CONTENT;

        dw.setAttributes(lp);
        mDialog.show();
    }

    public void setStatusbarActivity(StatusbarActivity sa) {
        mActivity = sa;
    }

    public void setFocusedView(View view) {
        mFocusedView = view;
    }

    public void setRunningView(View view) {
        mRunningView = view;
    }

    private void setFocusedStack() {
        try {
            if (ActivityManagerNative.getDefault().getFocusedStackId() != mActivity.mStackId) {
                ActivityManagerNative.getDefault().setFocusedStack(mActivity.mStackId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void resizeStack(){
        if(mActivity.mHiden){
            openAppBroadcast(mContext);
            try {
                ActivityManagerNative.getDefault().relayoutWindow(mActivity.mStackId,
                                                                  mActivity.mRestoreRect);
            } catch(Exception exc) {
            }
            mActivity.mHiden = false;
        }
    }

    public void runApkByPkg() {
        try {
            PackageManager manager = mContext.getPackageManager();
            Intent lanuch = new Intent();
            lanuch = manager.getLaunchIntentForPackage(mActivity.mPkgName);
            lanuch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(lanuch);
            openAppBroadcast(mContext);
        } catch(Exception exc) {
        }
    }

    private void runPhoneMode() {
        PackageManager manager = mContext.getPackageManager();
        Intent intent = new Intent();
        intent = manager.getLaunchIntentForPackage(mActivity.mPkgName);
        intent.addFlags(Intent.FLAG_ACTIVITY_RUN_PHONE_MODE
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
    }

    private void runPcMode() {
        PackageManager manager = mContext.getPackageManager();
        Intent intent = new Intent();
        intent = manager.getLaunchIntentForPackage(mActivity.mPkgName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }


    public void openAppBroadcast(Context context) {
        Intent openAppIntent = new Intent();
        openAppIntent.setAction(Intent.ACTION_OPEN_APPLICATION);
        context.sendBroadcast(openAppIntent);
    }

    //send broadCast; Matthew
    public void sendBroadcastMethod() {
        Intent intent = new Intent();
        intent.putExtra("keyAddInfo", mActivity.mPkgName);
        intent.setAction(Intent.ACTION_SEND_CLICK_INFO);
        mContext.sendBroadcast(intent);
    }

    //Wait one second; Matthew
    private void waitTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                sendBroadcastMethod();
            }
        };
        timer.schedule(task, TIMER_NUMBERS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int button = e.getButtonState();
        int action = e.getAction();

        if(button == MotionEvent.BUTTON_SECONDARY && action == MotionEvent.ACTION_DOWN) {
            dismissDialog();
            mShowRBM = true;
            showDialog(getRbmView(), mChangeDimension);
            return true;
        }
        // Locked status to click
        if(action == MotionEvent.ACTION_DOWN) {
            if(mActivity.mIsDocked) {
                if(!mActivity.mApkRun) {
                    waitTimer();
                    runApkByPkg();
                } else if(mActivity.mHiden) {
                    resizeStack();
                }
            } else if(mActivity.mHiden) {
                resizeStack();
            }
            setFocusedStack();
        }
        return super.onTouchEvent(e);
    }

    public void setFocused(boolean focused) {
        mFocusedView.setVisibility(focused ? View.VISIBLE : View.INVISIBLE);
        mRunningView.setVisibility(focused ? View.INVISIBLE : View.VISIBLE);
    }

    public void setRunning(boolean focused) {
        mRunningView.setVisibility(focused ? View.VISIBLE : View.INVISIBLE);
        mFocusedView.setVisibility(focused ? View.VISIBLE : View.INVISIBLE);

    }

    private class HoverListener implements OnHoverListener {
        @Override
        public boolean onHover(View useless, MotionEvent event){
            if (preventResponseHover()) {
                return false;
            }
            switch(event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    useless.setBackgroundResource(R.drawable.ic_background_mouse_hover);
                    View view = ((LayoutInflater) mContext.getSystemService(
                                                           Context.LAYOUT_INFLATER_SERVICE))
                                     .inflate(R.layout.status_bar_activity_hover_tips, null, false);
                    TextView v = (TextView) view.findViewById(R.id.akv_tips);
                    if (v != null) {
                        v.setText(PackageManager.getTitleByPkg(getContext(), mActivity.mPkgName));
                    }
                    dismissDialog();
                    showDialog(view, DIALOG_PADDING_TIPS);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    useless.setBackgroundResource(R.drawable.system_bar_background);
                    break;
            }
            return false;
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
}
