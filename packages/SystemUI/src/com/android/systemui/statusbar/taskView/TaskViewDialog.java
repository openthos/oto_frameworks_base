package com.android.systemui.statusbar.taskview;

import com.android.systemui.R;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

/**
 * Created by fengjie on 16-5-26.
 */
public class TaskViewDialog extends Dialog {
    public static final String TAG = "TaskViewDialog";
    private static TaskViewDialog taskViewDialog = null;
    private Context mcontext;
    private TaskSwitchAdapter mTaskAdapter;
    private GridView mTaskContainer;
    private DisplayMetrics mDm;

    IntentFilter mBroadcastIntentFilter
                     = new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS");
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                TaskViewDialog.this.dismiss();
            }
        }
    };

    public static TaskViewDialog getInstance(Context context) {
        if (taskViewDialog == null) {
            taskViewDialog = new TaskViewDialog(context, R.style.TaskViewDialog);
        }
        return taskViewDialog;
    }

    public TaskViewDialog(Context context) {
        super(context);
        this.mcontext = context;
        create();
    }

    public TaskViewDialog(Context context, int theme) {
        super(context, theme);
        this.mcontext = context;
        create();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void create() {
        mDm = mcontext.getResources().getDisplayMetrics();
        Window window = getWindow();
        window.setType(LayoutParams.TYPE_SYSTEM_ALERT);
        window.setFlags(LayoutParams.FLAG_ALT_FOCUSABLE_IM,LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        this.setContentView(R.layout.task_dialog);
        this.mTaskContainer = (GridView) findViewById(R.id.gridView);
        gridViewReloadTask();
        this.setCanceledOnTouchOutside(true);

        LayoutParams params = window.getAttributes();
        params.setTitle("TaskView");
        window.setAttributes(params);

        /*setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(TaskViewDialog.this.mTaskAdapter != null) {
                    TaskViewDialog.this.mTaskAdapter.resetTask();
                }
            }
        });*/

       this.mTaskContainer.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    protected void onStart() {
        super.onStart();
        getContext().registerReceiver(this.mBroadcastReceiver, this.mBroadcastIntentFilter);
    }

    protected void onStop() {
        super.onStop();
        getContext().unregisterReceiver(this.mBroadcastReceiver);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(!isShowing() || mTaskAdapter == null) {
            return true;
        }

        if(keyCode == KeyEvent.KEYCODE_TAB && event.isAltPressed() && event.isShiftPressed()) {
            mTaskAdapter.stepTaskForward();
        }

        return true;
    }

    private void gridViewReloadTask() {
        if(this.mTaskContainer != null) {
            this.mTaskAdapter = new TaskSwitchAdapter(getContext(), this.mTaskContainer);

            this.mTaskContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TaskViewDialog.this.dissmissAndSwitchForATask(position);
                }
            });
            mTaskContainer.setAdapter(this.mTaskAdapter);
        }
    }

    public void triggerTaskView() {
        if(this.isShowing()) {
            hideTaskView();
        } else {
            showTaskView();
        }
    }

    public void showTaskView() {
        gridViewReloadTask();

        Window window = getWindow();
        LayoutParams params = window.getAttributes();
        params.width = getDialogWidth();
        window.setAttributes(params);

        this.show();
        if(this.mTaskAdapter != null) {
            this.mTaskAdapter.stepTask();
        }
    }

    public void tabTaskView() {
        if(isShowing() && mTaskAdapter != null) {
            mTaskAdapter.stepTask();
        }
    }

    public void showOrTabTaskView() {
        if(!isShowing()) {
            showTaskView();
        } else {
            tabTaskView();
        }
    }

    public void hideTaskView() {
        TaskViewDialog.this.dissmissAndSwitchForATask();
        this.mTaskAdapter.resetTask();
    }

    private void dissmissAndSwitchForATask(int position) {
        this.dismiss();
        if(this.mTaskAdapter != null) {
            this.mTaskAdapter.switchTask(position);
        }
    }

    private void dissmissAndSwitchForATask() {
        dissmissAndSwitchForATask(-1);
    }

    private int getDialogWidth() {
        int taskSize = mTaskAdapter.getTaskSize();
        int itemInterval_dp = 16;
        int itemWidth_dp = 80;

        int itemWidth = dip2px(mcontext, itemWidth_dp);
        int itemInterval = dip2px(mcontext, itemInterval_dp);

        int dialogWidth = 0;
        int rowItemSize = getTaskItemSize(mDm.widthPixels , itemInterval, itemWidth);

        if(taskSize <= 0) {
            return 0;
        }

        mTaskContainer.setColumnWidth(itemWidth);
        if (taskSize <= rowItemSize) {
            dialogWidth = taskSize * itemWidth + (taskSize + 1) * itemInterval;
        } else {
            dialogWidth = rowItemSize * itemWidth + (rowItemSize + 1) * itemInterval;
        }

        return dialogWidth;
    }

    private int getTaskItemSize(int windowWidth, int itemInterval, int itemWidth) {
        int itemSize = 0;
        itemSize = windowWidth / (itemWidth + itemInterval);
        if (itemSize > 4) {
            itemSize -= 4;
        }

        return itemSize;
    }

    private int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
