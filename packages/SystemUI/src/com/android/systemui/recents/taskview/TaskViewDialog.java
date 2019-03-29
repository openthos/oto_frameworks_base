package com.android.systemui.recents.taskview;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnHoverListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import com.android.systemui.R;

public class TaskViewDialog extends Dialog {
    private static final String TAG = "TaskViewDialog";
    private static final int WINDOW_MARGIN = 4;
    private static TaskViewDialog mTaskViewDialog = null;
    private Context mContext;
    private TaskSwitchAdapter mTaskAdapter;
    private GridView mTaskContainer;
    private TextView mClearAll;
    private ClickListener mClickListener;
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
        mClearAll = (TextView) findViewById(R.id.recent_clear_all);
        gridViewReloadTask();
        setCanceledOnTouchOutside(false);
        mClickListener = new ClickListener();
        mClearAll.setOnClickListener(mClickListener);

        LayoutParams params = window.getAttributes();
        params.setTitle("TaskView");
        window.setAttributes(params);

        mTaskContainer.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    private class ClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.recent_clear_all:
                    mTaskAdapter.clearAllRecents();
                    refreshWindow();
                    break;
                case R.id.remove:
                    mTaskAdapter.removeSingleRecent();
                    refreshWindow();
                    break;
            }
        }
    }

    private void gridViewReloadTask() {
        if (mTaskContainer != null) {
            mTaskAdapter = new TaskSwitchAdapter();

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
        show();
        Window window = getWindow();
        LayoutParams params = window.getAttributes();
        params.width = getDialogWidth();
        window.setAttributes(params);
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
        int spacing = mTaskContainer.getHorizontalSpacing() == 0
                ? (int) mContext.getResources().getDimension(R.dimen.recents_layout_min_margin)
                : mTaskContainer.getHorizontalSpacing();

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

    public void refreshWindow() {
        Window window = getWindow();
        LayoutParams params = window.getAttributes();
        params.width = getDialogWidth();
        window.setAttributes(params);
    }

    private class TaskSwitchAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private ArrayList<TaskInfo> mTasks = new ArrayList();
        private ActivityManager mActivityManager;
        private PackageManager mPackageManager;
        private int mCurrentPosition = 1;
        private Drawable mDrawable;
        private ActivityManager.TaskSnapshot mSnapshot = null;
        private float mImageSize;

        class TaskInfo {
            public RecentTaskInfo info;
            public Intent intent;
            public boolean isHome;

            public TaskInfo(RecentTaskInfo info, Intent intent, boolean isHome) {
                this.info = info;
                this.intent = intent;
                this.isHome = isHome;
            }
        }

        public TaskSwitchAdapter() {
            super();
            mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            mInflater = LayoutInflater.from(mContext);
            mPackageManager = mContext.getPackageManager();
            mImageSize = mContext.getResources().getDimension(R.dimen.size_fiftyfive);
            reloadTask();
        }

        public void reloadTask() {
            ArrayList<RecentTaskInfo> recentTasks = new ArrayList();
            try {
                recentTasks = (ArrayList) mActivityManager.getRecentTasks(
                        ActivityManager.getMaxRecentTasksStatic(),
                        ActivityManager.RECENT_WITH_EXCLUDED);
            } catch(Exception e) {
                e.printStackTrace();
            }

            mTasks.clear();
            mCurrentPosition = 1;
            for (RecentTaskInfo ri : recentTasks) {
                if (ri.id < 0) {
                    continue;
                }
                Intent intent = new Intent(ri.baseIntent);
                intent.setComponent(ri.realActivity);
                boolean isHome = false;
                try {
                    isHome = ActivityManager.getService().getIsHome(ri.id);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                int stackId = ri.stackId;
                if (stackId != 0 || mTasks.isEmpty()) {
                    mTasks.add(new TaskInfo(ri, intent, isHome));
                } else {
                    mTasks.add(0, new TaskInfo(ri, intent, isHome));
                }
            }
            notifyDataSetChanged();
        }

        public void resetTask() {
            mTasks.clear();
        }

        public void stepTask() {
            if (!mTasks.isEmpty()) {
                mCurrentPosition = (mCurrentPosition + 1) % (mTasks.size());
                notifyDataSetChanged();
            }
        }

        public void switchTask(int position) {
            if (position == -1) {
                position = mCurrentPosition;
            }
            if (mTasks != null && position >= 0 && position < mTasks.size()) {
                mCurrentPosition = position;
                TaskInfo taskInfo = mTasks.get(mCurrentPosition);
                try {
                    if (taskInfo.info.id >= 0) {
                        if (taskInfo.isHome) {
                            ActivityManager.getService().returnToDesktop();
                        } else {
                            ActivityManager.getService()
                                    .setFocusedTask(mTasks.get(mCurrentPosition).info.id);
                        }
                    } else if (taskInfo.intent != null) {
                        taskInfo.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(taskInfo.intent);
                    }
                } catch(Exception e) {
                    Toast.makeText(mContext,
                            mContext.getString(R.string.switch_fail), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public int getCount() {
            return mTasks != null ? mTasks.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mTasks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder viewHolder;
            if (view == null) {
                view = (View) mInflater.inflate(R.layout.task_item, null);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
                view.setOnHoverListener(new ItemHoverListener());
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            TaskInfo taskInfo = mTasks.get(position);
            ResolveInfo resolveInfo = mPackageManager.resolveActivity(taskInfo.intent, 0);
            viewHolder.title.setText(resolveInfo.activityInfo.loadLabel(mPackageManager));
            mSnapshot = null;
            try {
                mSnapshot = mActivityManager.getService().getTaskSnapshot(taskInfo.info.id, false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (mSnapshot != null) {
                mDrawable = zoomBitmap(Bitmap.createHardwareBitmap(mSnapshot.getSnapshot()));
            } else {
                mDrawable = resolveInfo.activityInfo.loadIcon(mPackageManager);
            }
            viewHolder.image.setImageDrawable(mDrawable);
            if (taskInfo.isHome) {
                viewHolder.remove.setVisibility(View.GONE);
            } else {
                viewHolder.remove.setVisibility(View.VISIBLE);
            }

            setItemNormal(view);
            if (mCurrentPosition == position) {
                setItemFocus(view);
            }
            viewHolder.remove.setOnClickListener(mClickListener);
            return view;
        }

        private class ViewHolder {
            public TextView title;
            public ImageView image;
            public ImageView remove;

            public ViewHolder(View view) {
                title = (TextView) view.findViewById(R.id.title);
                image = (ImageView) view.findViewById(R.id.imageView);
                remove = (ImageView) view.findViewById(R.id.remove);
            }
        }

        private class ItemHoverListener implements OnHoverListener {

            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        setItemNormal(mTaskContainer.getChildAt(mCurrentPosition));
                        setItemFocus(v);
                        mCurrentPosition = mTaskContainer.getPositionForView(v);
                        break;
                }
                return false;
            }
        }

        public void setItemFocus(View v) {
            if (v != null) {
                v.setBackground(mContext.getResources().getDrawable(R.drawable.taskview_item_bg_focus));
                v.findViewById(R.id.title).setVisibility(View.VISIBLE);
            }
        }

        public void setItemNormal(View v) {
            if (v != null) {
                v.setBackground(mContext.getResources().getDrawable(R.drawable.taskview_item_bg_normal));
                v.findViewById(R.id.title).setVisibility(View.INVISIBLE);
            }
        }

        public int getTaskSize() {
            return getCount();
        }

        private BitmapDrawable zoomBitmap(Bitmap bitmap) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = (mImageSize / width);
            float scaleHeight = (mImageSize / height);
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            return new BitmapDrawable(newbmp);
        }

        public void removeSingleRecent() {
            mActivityManager.removeTaskRecents(mTasks.get(mCurrentPosition).info.id, false);
            reloadTask();
        }

        public void clearAllRecents() {
            for (TaskInfo task : mTasks) {
                if (!task.isHome) {
                    mActivityManager.removeTaskRecents(task.info.id, false);
                }
            }
            reloadTask();
        }
    }
}
