package com.android.systemui.dialog;

import android.app.ActivityManagerNative;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Slog;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.startupmenu.DialogType;
import com.android.systemui.sql.SqliteOperate;
import com.android.systemui.startupmenu.U;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

/**
 * Created by ljh on 17-9-14.
 */

public class BarDialog extends BaseDialog implements AdapterView.OnItemClickListener {
    private static BarDialog listDialog;
    private static ComponentName mComponentName;
    private static int mTaskId;
    private static HashMap<Integer, FrameLayout> mIconMap;
    private static HashMap<ComponentName, FrameLayout> mDockedMap;
    private static LinearLayout mActivityLayout;
    private static FrameLayout mIconLayout;
    public static int mShowX;
    public static int mShowY;
    public List<String> mDatas;
    public DialogAdapter mAdapter;
    private ListView mListView;
    private Point mPoint;
    private int mWidth;
    private int mHeight;
    private int mStatusBarHeight;

    public static BarDialog getInstance(Context context, ComponentName appEntry, int taskId,
                                        LinearLayout activityLayout,
                                        FrameLayout iconLayout,
                                        HashMap<Integer, FrameLayout> iconMap,
                                        HashMap<ComponentName, FrameLayout> dockedMap) {
        if (listDialog == null) {
            listDialog = new BarDialog(context);
        }
        mComponentName = appEntry;
        mTaskId = taskId;
        mIconMap = iconMap;
        mDockedMap = dockedMap;
        mActivityLayout = activityLayout;
        mIconLayout = iconLayout;
        return listDialog;
    }

    public static void dismissBarDialog(){
        if(listDialog != null && listDialog.isShowing()){
            listDialog.dismiss();
        }
    }

    private BarDialog (@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.menu_dialog, null, false);
        setContentView(mContentView);
        create();
    }

    @Override
    public void initListener() {
        mListView.setOnItemClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.menu_dialog);
//        initView();
//        initData();
    }

    @Override
    public void initView() {
        mListView = (ListView) findViewById(R.id.list);
        Slog.e("LADEHUNTER+BARDIALOG", "mListView: " + mListView + " after initView");
    }

    @Override
    public void initData() {
        mDatas = new ArrayList<>();
        mAdapter = new DialogAdapter();
        Slog.e("LADEHUNTER+BARDIALOG", "mAdapater: " + mAdapter + " in initData", new Exception());
        mListView.setAdapter(mAdapter);
        Display defaultDisplay = ((WindowManager)
                getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mPoint = new Point();
        defaultDisplay.getRealSize(mPoint);

    }

    public void showDialog() {

        int x = mShowX;
        int y = mShowY;
        prepareData();

        Window dialogWindow = getWindow();
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.format = PixelFormat.TRANSPARENT;
        lp.dimAmount = 0;
        if (x > mPoint.x - mWidth) {
            lp.x = x - mWidth / 2 - mPoint.x / 2;
        } else {
            lp.x = x + mWidth / 2 - mPoint.x / 2;
        }
        if (y < mPoint.y - mStatusBarHeight - mHeight) {
            lp.y = y + mHeight / 2 - mPoint.y / 2;
        } else {
            lp.y = y - mHeight / 2 - mPoint.y / 2;
        }
        dialogWindow.setAttributes(lp);
        Slog.e("LADEHUNTER+BARDIALOG", "Dilog will show");
        show();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus){
            dismiss();
        }
    }

    private void prepareData() {
        mDatas.clear();
        boolean docked = mDockedMap.containsKey(mComponentName);
        boolean run = mIconMap.containsKey(mTaskId);
        Slog.e("LADEHUNTER+BARDIALOG", "mComponentName: " + mComponentName);
        Slog.e("LADEHUNTER+BARDIALOG", "mTaskId: " + mTaskId);
        Slog.e("LADEHUNTER+BARDIALOG", "mIconMap: " + mIconMap);
        Slog.e("LADEHUNTER+BARDIALOG", "mDockedMap: " + mDockedMap);
        Slog.e("LADEHUNTER+BARDIALOG", "mIconLayout: " + mIconLayout);
        Slog.e("LADEHUNTER+BARDIALOG", "docked: " + docked + " run: " + run);
        String[] sArr;
        if (docked && run) {
            sArr = getContext().getResources().getStringArray(
                                R.array.bar_menu_docked_open);
            Slog.e("LADEHUNTER+BARDIALOG", "bar_menu_docked_open");
        } else if (docked) {
            sArr = getContext().getResources().getStringArray(
                                R.array.bar_menu_docked_closed);
            Slog.e("LADEHUNTER+BARDIALOG", "bar_menu_docked_closed");
        } else {
            sArr = getContext().getResources().getStringArray(
                                R.array.bar_menu_normal);
            Slog.e("LADEHUNTER+BARDIALOG", "bar_menu_normal");
        }
        mDatas.addAll(Arrays.asList(sArr));
        Slog.e("LADEHUNTER+BARDIALOG", "mDatas: " + mDatas);
        mAdapter.notifyDataSetChanged();
        Slog.e("LADEHUNTER+BARDIALOG", "mAdapter: " + mAdapter);
        mWidth = 0;
        mHeight = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View view = mAdapter.getView(i, null, null);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mWidth = Math.max(view.getMeasuredWidth(), mWidth);
            Slog.e("LADEHUNTER+BARDIALOG", "mWidth: " + mWidth);
            mHeight = mHeight + view.getMeasuredHeight();
            Slog.e("LADEHUNTER+BARDIALOG", "mHeight: " + mHeight);
        }

        mListView.setLayoutParams(new LinearLayout.LayoutParams(mWidth, mHeight));
        Slog.e("LADEHUNTER+BARDIALOG", "mListView: " + mListView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean run = mIconMap.containsKey(mTaskId);
        boolean docked = mDockedMap.containsKey(mComponentName);
        boolean iconDocked = mDockedMap.containsValue(mIconLayout);
        dismiss();
        String content = mDatas.get(position);
        if (content.equals(getContext().getString(R.string.open))) {
            U.launchApp(getContext(), mComponentName);
        } else if (content.equals(getContext().getString(R.string.close))) {
            try {
                ActivityManager.getService().removeTask(mTaskId);
            } catch (Exception e){
            }
        } else if (content.equals(getContext().getString(R.string.dock))) {
            if (!docked) {
                mDockedMap.put(mComponentName, mIconLayout);
            }
        } else if (content.equals(getContext().getString(R.string.undock))) {
            if (iconDocked) {
                mDockedMap.remove(mComponentName);
                if (!run) {
                    mActivityLayout.removeView(mIconLayout);
                }
            }
        }
    }

    private void dismissAndHideStartMenu(boolean hideStartMenu) {
        dismiss();
    }

    private void uninstallApp() {
        Uri uri = Uri.parse("package:" + mComponentName.getPackageName());
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        getContext().startActivity(intent);
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
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
//            if (mType == DialogType.GRID
//                    && getItem(position).equals(getContext().getString(R.string.lock_app))
//                    && mInstance.isPinned(mComponentName.getComponentName())) {
//                mDatas.set(position, getContext().getString(R.string.unlock_app));
//                holder.text.setText(getContext().getString(R.string.unlock_app));
//            } else {
//                holder.text.setText(getItem(position));
//            }
            holder.text.setText(getItem(position));
            return convertView;
        }

        private class ViewHolder implements View.OnHoverListener {
            private TextView text;

            public ViewHolder(View view) {
                text = (TextView) view.findViewById(R.id.text);
                view.setOnHoverListener(this);
            }

            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        v.setBackgroundResource(R.color.common_hover_bg);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        v.setBackgroundResource(android.R.color.white);
                        break;
                }
                return false;
            }
        }
    }
}
