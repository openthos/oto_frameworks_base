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
import com.android.systemui.startupmenu.DialogType;

/**
 * Created by ljh on 17-9-26.
 */

public class BaseDialog extends Dialog {
    protected View mContentView;
    protected static Point mPoint;

    public BaseDialog(@NonNull Context context) {
        this(context, R.style.StartMenuDialogStyle);
    }

    public BaseDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        //init screen's size
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

    /**
     * show dialog at the grivate of view's center
     * @param view
     */
    public void show(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        if (mContentView != null) {
            mContentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        }
        Window dialogWindow = getWindow();
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialogWindow.setWindowAnimations(R.style.ShowDialog);
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
        dialogWindow.setAttributes(lp);
        show();
    }
}
