package com.android.systemui.dialog;

import android.content.Context;
import android.view.View;

import com.android.systemui.startupmenu.StartupMenuView;

public class StartupMenuDialog extends BaseDialog {
    private boolean mCanDismiss;

    public StartupMenuDialog(Context context) {
        super(context);
        mCanDismiss = true;
        mContentView = new StartupMenuView(context);
        setContentView(mContentView);
    }

    @Override
    public void show(View view) {
        super.show(view);
        ((StartupMenuView) mContentView).refresh();
    }

    @Override
    public void dismiss() {
        MenuDialog menuDialog = ((StartupMenuView) mContentView).getmMenuDialog();
        if (menuDialog != null && menuDialog.isShowing()) {
            menuDialog.dismiss();
        }
        super.dismiss();
    }

    public void setCanDismiss(boolean canDismiss) {
        mCanDismiss = canDismiss;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!mCanDismiss) {
            mCanDismiss = true;
        } else if (!hasFocus) {
            dismiss();
        }
        super.onWindowFocusChanged(hasFocus);
    }
}
