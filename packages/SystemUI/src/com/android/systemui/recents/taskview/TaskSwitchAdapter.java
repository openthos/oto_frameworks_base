package com.android.systemui.recents.taskview;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnHoverListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.R;
import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Matrix;
import android.os.RemoteException;

public class TaskSwitchAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<TaskInfo> mTasks = new ArrayList();
    private ActivityManager mActivityManager;
    private Context mContext;
    private PackageManager mPackageManager;
    private int mCurrentPosition = 1;
    private GridView mContainer;
    private Drawable mDrawable;
    private ActivityManager.TaskSnapshot mSnapshot = null;
    private float mImageSize;
    private static final int DISPLAY_TASKS = 100;
    private static final int MAX_TASKS = DISPLAY_TASKS + 1;

    class TaskInfo {
        public RecentTaskInfo info;
        public Intent intent;

        public TaskInfo(RecentTaskInfo info, Intent intent) {
            this.info = info;
            this.intent = intent;
        }
    }

    public TaskSwitchAdapter(Context c, GridView parent) {
        super();
        mContext = c;
        mActivityManager = (ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);
        mInflater = LayoutInflater.from(c);
        mPackageManager = mContext.getPackageManager();
        mContainer = parent;
        mImageSize = c.getResources().getDimension(R.dimen.size_fiftyfive);
        reloadTask();
    }

    public void reloadTask() {
        ArrayList<RecentTaskInfo> recentTasks = new ArrayList();
        try {
            recentTasks = (ArrayList) mActivityManager.getRecentTasks(MAX_TASKS,
                    ActivityManager.RECENT_WITH_EXCLUDED);
        } catch(Exception e) {
            e.printStackTrace();
        }

        mTasks.clear();
        mCurrentPosition = 1;
        for (int i = 0; i < recentTasks.size(); i++) {
            RecentTaskInfo ri = (RecentTaskInfo) recentTasks.get(i);
            if (ri.id < 0) {
                continue;
            }
            Intent intent = new Intent(ri.baseIntent);
            intent.setComponent(ri.realActivity);

            int stackId = ri.stackId;
            if (stackId != 0 || mTasks.isEmpty()) {
                mTasks.add(new TaskInfo(ri, intent));
            } else {
                mTasks.add(0, new TaskInfo(ri, intent));
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
            if (taskInfo.info.id >= 0) {
                mActivityManager.moveTaskToFront(mTasks.get(mCurrentPosition).info.id, 0, null);
            } else if (taskInfo.intent != null) {
                taskInfo.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    mContext.startActivity(taskInfo.intent);
                } catch(Exception e) {
                    e.printStackTrace();
                    Log.w("TaskSwitchAdapter", "switchTask e:   " + e );
                }
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
        TaskInfo taskInfo = (TaskInfo) mTasks.get(position);
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

        setItemNormal(view);
        if (mCurrentPosition == position) {
            setItemFocus(view);
        }
        return view;
    }

    private class ViewHolder {
        public TextView title;
        public ImageView image;

        public ViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.title);
            image = (ImageView) view.findViewById(R.id.imageView);
        }
    }

    private class ItemHoverListener implements OnHoverListener {

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    setItemNormal(mContainer.getChildAt(mCurrentPosition));
                    setItemFocus(v);
                    mCurrentPosition = mContainer.getPositionForView(v);
                    break;
            }
            return false;
        }
    }

    public void setItemFocus(View v) {
        v.setBackground(mContext.getResources().getDrawable(R.drawable.taskview_item_bg_focus));
        v.findViewById(R.id.title).setVisibility(View.VISIBLE);
    }

    public void setItemNormal(View v) {
        v.setBackground(mContext.getResources().getDrawable(R.drawable.taskview_item_bg_normal));
        v.findViewById(R.id.title).setVisibility(View.INVISIBLE);
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
}
