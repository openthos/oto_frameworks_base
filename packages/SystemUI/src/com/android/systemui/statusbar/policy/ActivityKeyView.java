
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

public class ActivityKeyView extends ImageView {
    OnClickListener mOpen;     /* Use to open activity by mPkgName fo related StatusbarActivity. */
    OnClickListener mClose;     /* Use to close window like mCloseBtn of window header. */
    OnClickListener mDock;      /* Use to dock related StatusbarActivity in status bar. */
    OnClickListener mUnDock;    /* Use to undock related StatusbarAcitivity from status bar. */
    StatusbarActivity mActivity;    /* Related StatusbarActivity. */
    View mFocusedView;

    String TAG = "ActivityKeyView"; /* Log Tag. */
    public static Dialog mRBM = null;   /* Define right button menu as a Singleton dialog. */

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
        /* Init all OnClickListener. */
        mOpen = new OnClickListener() {
            @Override
            public void onClick(View v) {
                runApkByPkg();
                mRBM.dismiss();
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
                mRBM.dismiss();
            }
        };

        mDock = new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.mIsDocked = true;
                mRBM.dismiss();
            }
        };

        mUnDock = new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.mIsDocked = false;
                mRBM.dismiss();
                removeFromRoot();
            }
        };
    }

    public void removeFromRoot() {
        if(!mActivity.mApkRun) {
            this.setVisibility(View.GONE);
        }
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

    /* Build right button menu consider about
       the state of realted StatusbarActivity. */
    public View getRbmView() {
        LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(mActivity.mIsDocked) {
            if(mActivity.mApkRun) {
                return buildRbmDockedRun(li);
            } else {
                return buildRbmDocked(li);
            }
        } else {
            return buildRbmRun(li);
        }
    }

    public View buildRbmDocked(LayoutInflater li) {
        View rbmDocked = li.inflate(R.layout.right_button_menu_docked, null, false);
        TextView open = (TextView) rbmDocked.findViewById(R.id.rbm_open);
        open.setOnClickListener(mOpen);
        TextView undock = (TextView) rbmDocked.findViewById(R.id.rbm_undock);
        undock.setOnClickListener(mUnDock);
        return rbmDocked;
    }

    public View buildRbmDockedRun(LayoutInflater li) {
        View rbmDockedRun = li.inflate(R.layout.right_button_menu_docked_run, null, false);
        TextView close = (TextView) rbmDockedRun.findViewById(R.id.rbm_close);
        close.setOnClickListener(mClose);
        TextView undock = (TextView) rbmDockedRun.findViewById(R.id.rbm_undock);
        undock.setOnClickListener(mUnDock);
        return rbmDockedRun;
    }

    public View buildRbmRun(LayoutInflater li) {
        View rbmRun = li.inflate(R.layout.right_button_menu_run, null, false);
        TextView close = (TextView) rbmRun.findViewById(R.id.rbm_close);
        close.setOnClickListener(mClose);
        TextView dock = (TextView) rbmRun.findViewById(R.id.rbm_dock);
        dock.setOnClickListener(mDock);
        return rbmRun;
    }

    /* Only new RBM in first get. */
    public static Dialog getRBM(Context context) {
        if(mRBM == null) {
            mRBM = new Dialog(context);
            mRBM.requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window rbmWindow = mRBM.getWindow();
            rbmWindow.setType(2008);
        }
        return mRBM;
    }

    /* Make sure RMB can be dismiss by other (especially by statusbar). */
    public static void dismissRBM() {
        if(mRBM != null) {
            if(mRBM.isShowing()) {
                mRBM.dismiss();
            }
        }
    }

    /* Use to get related StatubarActivity from PhoneStatusBar. */
    public void setStatusbarActivity(StatusbarActivity sa) {
        mActivity = sa;
    }

    public void setFocusedView(View view) {
        mFocusedView = view;
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
            //Resize
            try {
                ActivityManagerNative.getDefault().relayoutWindow(mActivity.mStackId,
                                                                  mActivity.mRestoreRect);
            } catch(Exception exc) {
            }
            mActivity.mHiden = false;
        }
    }

    public void runApkByPkg() {
        //Run APK by PkgName
        try {
            PackageManager manager = mContext.getPackageManager();
            Intent lanuch = new Intent();
            lanuch = manager.getLaunchIntentForPackage(mActivity.mPkgName);
            lanuch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(lanuch);
        } catch(Exception exc) {
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int button = e.getButtonState();
        int action = e.getAction();

        /* Show RBM when right button is clicked. */
        if(button == MotionEvent.BUTTON_SECONDARY && action == MotionEvent.ACTION_DOWN) {

            /* Set Rbm View to RBM dialog. */
            Dialog dialog = getRBM(mContext);
            View rbm = getRbmView();
            dialog.setContentView(rbm);

            Window dw = dialog.getWindow();
            WindowManager.LayoutParams lp = dw.getAttributes();

            /* Measure rbm by force to get real width and height. */
            int dpx = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE))
                                                                      .getDefaultDisplay().getWidth();
            int dpy = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE))
                                                                     .getDefaultDisplay().getHeight();
            int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
            int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
            rbm.measure(w, h);

            /* Compute location by ActivityKeyView's location and rbm's size. */
            int[] location = new int[2];
            this.getLocationOnScreen(location);
            lp.x = location[0] - dpx/2;
            lp.y = location[1] - dpy/2 - rbm.getMeasuredHeight() - rbm.getMeasuredHeight()/2;
            lp.width = LayoutParams.WRAP_CONTENT;
            lp.height = LayoutParams.WRAP_CONTENT;

            dw.setAttributes(lp);
            dialog.show();

            /* Return true to finish this MotionEvent. */
            return true;
        }
        if(action == MotionEvent.ACTION_DOWN) {
            if(mActivity.mIsDocked) {
                if(!mActivity.mApkRun) {
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
    }
}
