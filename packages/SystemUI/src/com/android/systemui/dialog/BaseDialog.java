package com.android.systemui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.systemui.R;

/**
 * Created by ljh on 17-9-26.
 */

public class BaseDialog extends Dialog {
    protected View mContentView;
    protected static Point mPoint;
    private int mViewId;

    public BaseDialog(@NonNull Context context) {
        this(context, R.style.StartMenuDialogStyle);
    }

    public BaseDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        if (mPoint == null) {
            Display defaultDisplay = ((WindowManager)
                    getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            mPoint = new Point();
            defaultDisplay.getRealSize(mPoint);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initListener();
    }

    public void initView() {

    }

    public void initData() {

    }

    public void initListener() {

    }

    public void show(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        if (mContentView != null) {
            mContentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        }
        Window dialogWindow = getWindow();
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.format = PixelFormat.TRANSPARENT;
        lp.dimAmount = 0;

        if (mContentView.getMeasuredWidth() / 2 > location[0]) {
            dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
            lp.x = 0;
        } else if (location[0] + view.getMeasuredWidth() / 2 +
                mContentView.getMeasuredWidth() / 2 > mPoint.x) {
            dialogWindow.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
            lp.x = 0;
        } else {
            dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            lp.x = location[0] + view.getMeasuredWidth() / 2 - mPoint.x / 2;
        }
        lp.y = 0;

//        android.util.Log.i("ljh", "mContentView.getMeasuredWidth() " + mContentView.getMeasuredWidth());
//        android.util.Log.i("ljh", "mContentView.getMeasuredHeight() " + mContentView.getMeasuredHeight());
//        android.util.Log.i("ljh", "location[0] " + location[0]);
//        android.util.Log.i("ljh", "location[1] " + location[1]);
//        android.util.Log.i("ljh", "view.getMeasuredWidth() " + view.getMeasuredWidth());
//        android.util.Log.i("ljh", "mPoint.x " + mPoint.x);
//        android.util.Log.i("ljh", "--------------------------------------");

        dialogWindow.setAttributes(lp);
        show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
//            dismiss();
        }
    }
}
