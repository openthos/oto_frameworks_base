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
        /*if(keyCode != KeyEvent.KEYCODE_TAB || !event.isAltPressed()) {
            return super.onKeyDown(keyCode, event);
        }

        Log.w(TAG, "onKeyDown");
        if(this.isShowing() && this.mTaskAdapter != null) {
            this.mTaskAdapter.stepTask();
        }*/

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
}
