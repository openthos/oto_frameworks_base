package com.android.systemui.recents.taskview;

import com.android.systemui.R;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;

public class TaskViewDialog extends Dialog {
    private static final String TAG = "TaskViewDialog";
    private static final int WINDOW_MARGIN = 4;
    private static TaskViewDialog mTaskViewDialog = null;
    private Context mContext;
    private TaskSwitchAdapter mTaskAdapter;
    private GridView mTaskContainer;
    private DisplayMetrics mDisplayMetrics;

    public static TaskViewDialog getInstance(Context context) {
        if (mTaskViewDialog == null) {
            mTaskViewDialog = new TaskViewDialog(context, R.style.TaskViewDialog);
        }
        return mTaskViewDialog;
    }

    private TaskViewDialog(Context context) {
        super(context);
        mContext = context;
        create();
    }

    private TaskViewDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        create();
    }

    public void create() {
        mDisplayMetrics = mContext.getResources().getDisplayMetrics();
        Window window = getWindow();
        window.setType(LayoutParams.TYPE_SYSTEM_ALERT);
        window.setFlags(LayoutParams.FLAG_ALT_FOCUSABLE_IM, LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        setContentView(R.layout.task_dialog);
        mTaskContainer = (GridView) findViewById(R.id.gridView);
        gridViewReloadTask();
        setCanceledOnTouchOutside(true);

        LayoutParams params = window.getAttributes();
        params.setTitle("TaskView");
        window.setAttributes(params);

        mTaskContainer.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_TAB:
                if (event.getRepeatCount() <= 0) {
                    showOrTabTaskView();
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void gridViewReloadTask() {
        if (mTaskContainer != null) {
            mTaskAdapter = new TaskSwitchAdapter(getContext(), mTaskContainer);

            mTaskContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                   dissmissAndSwitchForATask(position);
                }
            });
            mTaskContainer.setAdapter(mTaskAdapter);
        }
    }

    public void showTaskView() {
        gridViewReloadTask();

        Window window = getWindow();
        LayoutParams params = window.getAttributes();
        params.width = getDialogWidth();
        window.setAttributes(params);

        show();
        if (mTaskAdapter != null) {
            mTaskAdapter.stepTask();
        }
    }

    public void tabTaskView() {
        if (mTaskAdapter != null) {
            mTaskAdapter.stepTask();
        }
    }

    public void showOrTabTaskView() {
        if (!isShowing()) {
            showTaskView();
        } else {
            tabTaskView();
        }
    }

    public void hideTaskView() {
        dissmissAndSwitchForATask();
        mTaskAdapter.resetTask();
    }

    private void dissmissAndSwitchForATask(int position) {
        dismiss();
        if (mTaskAdapter != null) {
            mTaskAdapter.switchTask(position);
        }
    }

    private void dissmissAndSwitchForATask() {
        dissmissAndSwitchForATask(-1);
    }

    private int getDialogWidth() {
        View listItem = mTaskAdapter.getView(0, null, mTaskContainer);
        listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int itemWidth = listItem.getMeasuredWidth();
        int spacing = mTaskContainer.getHorizontalSpacing();

        int taskSize = mTaskAdapter.getTaskSize();
        int dialogWidth = 0;
        int rowItemSize = getTaskItemSize(mDisplayMetrics.widthPixels, itemWidth, spacing);

        if (taskSize <= 0) {
            return 0;
        }
        mTaskContainer.setColumnWidth(itemWidth);
        if (taskSize <= rowItemSize) {
            dialogWidth = taskSize * itemWidth + (taskSize + 1) * spacing;
        } else {
            dialogWidth = rowItemSize * itemWidth + (rowItemSize + 1) * spacing;
        }
        return dialogWidth;
    }

    private int getTaskItemSize(int windowWidth, int itemWidth, int spacing) {
        int itemSize = windowWidth / (itemWidth + spacing);
        if (itemSize > WINDOW_MARGIN) {
            itemSize -= WINDOW_MARGIN;
        }
        return itemSize;
    }
}
