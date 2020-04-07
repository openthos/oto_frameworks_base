package com.android.systemui.dialog;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.startupmenu.bean.AppInfo;
import com.android.systemui.startupmenu.listener.OnMenuClick;
import com.android.systemui.statusbar.phone.StatusBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;

public class MenuDialog extends BaseDialog {
    private List<String> mDatas;
    private HashSet<Integer> mTasks;
    private DialogAdapter mAdapter;
    private ListView mListView;
    private AppInfo mAppInfo;
    private OnMenuClick mOnMenuClick;
    private int mTaskId;
    private int mWidth;
    private int mHeight;
    private StatusBar mStatusBar;
    private int mStatusBarHeight;
    private DialogType mDialogType;
    private int mMinDialogWidth = 160;

    public MenuDialog(Context context) {
        super(context);
        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.menu_dialog, null, false);
        setContentView(mContentView);
        initView();
        initData();
        create();
    }

    public void initView() {
        mListView = (ListView) mContentView.findViewById(R.id.list);
    }

    public void initData() {
        mDatas = new ArrayList<>();
        mAdapter = new DialogAdapter();
        mListView.setAdapter(mAdapter);
        mStatusBar = SysUiServiceProvider.getComponent(getContext(), StatusBar.class);
        mStatusBarHeight = getContext().getResources().
                getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
    }

    public void show(DialogType type, AppInfo appInfo, int x, int y) {
        mDialogType = type;
        mAppInfo = appInfo;
        show(type, x, y);
    }

    public void show(DialogType type, AppInfo appInfo, View view, int taskId) {
        mDialogType = type;
        mAppInfo = appInfo;
        prepareData();
        mTaskId = taskId;
        show(view);
    }

    public void show(DialogType type, AppInfo appInfo, View view, HashSet<Integer> tasks) {
        mDialogType = type;
        mAppInfo = appInfo;
        mTasks = tasks;
        prepareData();
        show(view);
    }

    public void show(DialogType type, AppInfo appInfo, View view) {
        mDialogType = type;
        mAppInfo = appInfo;
        prepareData();
        show(view);
    }

    public void show(DialogType type, int x, int y) {
        mDialogType = type;
        prepareData();
        Window dialogWindow = getWindow();
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.format = PixelFormat.TRANSPARENT;
        lp.dimAmount = 0;
        switch (type) {
            case SHOW_TASKBAR:
                dialogWindow.setWindowAnimations(R.style.ShowDialog);
                dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
                lp.x = x - mWidth / 2;
                lp.y = 0;
                break;
            case RECENT:
            case LIST:
                dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
                lp.x = x;
                if (y > mPoint.y - mHeight - mStatusBarHeight) {
                    lp.y = y - mHeight;
                } else {
                    lp.y = y;
                }
                break;
            default:
                break;
        }
        dialogWindow.setAttributes(lp);
        show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && isStartupMenu()) {
            StartupMenuDialog startupMenuDialog = mStatusBar.getStartupMenuDialog();
            if (startupMenuDialog != null) {
                startupMenuDialog.setCanDismiss(false);
            }
        }
        super.onWindowFocusChanged(hasFocus);
    }

    /**
     * init dialog data by dialog type
     */
    private void prepareData() {
        mDatas.clear();
        String[] sArr = null;
        switch (mDialogType) {
            case LIST:
                if (mAppInfo.isLocked()) {
                    sArr = getContext().getResources().getStringArray(R.array.list_menu_lock);
                } else {
                    sArr = getContext().getResources().getStringArray(R.array.list_menu_unlock);
                }
                break;
            case RECENT:
                sArr = getContext().getResources().getStringArray(R.array.recent_menu);
                break;
            case SHOW_TASKBAR:
                sArr = getContext().getResources().getStringArray(R.array.bar_show_hide);
                break;
            case BAR_LOCK_CLOSE:
                sArr = getContext().getResources().getStringArray(R.array.bar_menu_lock_close);
                break;
            case BAR_LOCK_OPEN:
                sArr = getContext().getResources().getStringArray(R.array.bar_menu_lock_open);
                break;
            case BAR_UNLOCK_OPEN:
                sArr = getContext().getResources().getStringArray(R.array.bar_menu_unlock_open);
                break;
            case NOTIFY_NAME:
                sArr = new String[]{mAppInfo.getLabel()};
                break;
            case SELECT_TASK:
                sArr = new String[mTasks.size()];
                for (int i = 0; i < mTasks.size(); i++) {
                    sArr[i] = String.valueOf((int) mTasks.toArray()[i]);
                }
                break;
        }
        mDatas.addAll(Arrays.asList(sArr));
        mAdapter.notifyDataSetChanged();
        //set listView's width and height
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        mHeight = params.height;
        mWidth = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View view = mAdapter.getView(i, null, null);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            android.util.Log.e("lxx-dialog","before---mWidth="+mWidth+" mHeight="+mHeight+" view="+view
                            +" getWidth="+view.getWidth()+" getHeight="+view.getHeight());
            mWidth = Math.max(view.getMeasuredWidth(), mWidth);
            android.util.Log.e("lxx-dialog","after---mWidth="+mWidth+" mHeight="+mHeight);
        }

        if (mWidth < mMinDialogWidth) {
            params.width = mWidth = mMinDialogWidth;
        }
        android.util.Log.e("lxx-dialog","final---mWidth="+mWidth+" mHeight="+mHeight);
        mListView.setLayoutParams(params);
    }

    public boolean isStartupMenu() {
        return mDialogType != null && (mDialogType == DialogType.RECENT
                || mDialogType == DialogType.LIST);
    }

    public void setOnMenuClick(OnMenuClick menuClick) {
        mOnMenuClick = menuClick;
    }

    private class DialogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public String getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView =
                        LayoutInflater.from(getContext()).inflate(R.layout.menu_dialog_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
                holder.text.setOnHoverListener(mHoverListener);
                holder.text.setOnClickListener(mClickListener);
                holder.text.setOnTouchListener(mTouchListener);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.text.setText(getItem(position));
            return convertView;
        }

        private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (mDialogType == DialogType.SELECT_TASK
                        && e.getAction() == MotionEvent.ACTION_DOWN
                        && e.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                    mOnMenuClick.menuClick(v, MenuDialog.this,
                            mAppInfo, ((TextView) v).getText().toString(), -3);
                    return true;
                }
                return false;
            }
        };

        private View.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMenuClick != null) {
                    if (mDialogType == DialogType.SELECT_TASK) {
                        mOnMenuClick.menuClick(v, MenuDialog.this,
                                mAppInfo, ((TextView) v).getText().toString(), -2);
                    } else {
                        mOnMenuClick.menuClick(v, MenuDialog.this,
                                mAppInfo, ((TextView) v).getText().toString(), mTaskId);
                    }
                }
            }
        };

        private View.OnHoverListener mHoverListener = new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        v.setSelected(true);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        v.setSelected(false);
                        break;
                }
                return false;
            }
        };

        private class ViewHolder {
            private TextView text;

            public ViewHolder(View view) {
                text = (TextView) view.findViewById(R.id.text);
            }
        }
    }
}
