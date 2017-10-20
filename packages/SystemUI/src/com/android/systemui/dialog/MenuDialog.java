package com.android.systemui.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ListView;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.sql.SqliteOperate;
import com.android.systemui.startupmenu.AppEntry;
import com.android.systemui.startupmenu.DialogType;
import com.android.systemui.startupmenu.U;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ljh on 17-9-14.
 */

public class MenuDialog extends BaseDialog implements AdapterView.OnItemClickListener {
    private static AppEntry mAppEntry;
    private static MenuDialog listDialog;
    private List<String> mDatas;
    private DialogAdapter mAdapter;
    private ListView mListView;
    private int mWidth;
    private int mHeight;
    private int mStatusBarHeight;

    public static MenuDialog getInstance(Context context) {
        if (listDialog == null) {
            listDialog = new MenuDialog(context);
        }
        return listDialog;
    }

    public static void dismissMenuDialog() {
        if (listDialog != null && listDialog.isShowing()) {
            listDialog.dismiss();
        }
    }

    public MenuDialog(@NonNull Context context) {
        super(context);
        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.menu_dialog, null, false);
        setContentView(mContentView);
        initView();
        initData();
        create();
    }

    public void initListener() {
        mListView.setOnItemClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.i("ljh", "oncreate");
//        setContentView(R.layout.menu_dialog);
//        initView();
//        initData();
    }

    public void initView() {
        mListView = (ListView) mContentView.findViewById(R.id.list);
    }

    public void initData() {
        mDatas = new ArrayList<>();
        mAdapter = new DialogAdapter();
        mListView.setAdapter(mAdapter);
    }

    public void show(DialogType type, AppEntry appEntry, int x, int y) {
        android.util.Log.i("ljh", "show");
        mAppEntry = appEntry;
        show(type, x, y);
    }

    private void show(DialogType type, int x, int y) {

        prepareData(type);

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
        show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            dismiss();
        }
    }

    private void prepareData(DialogType type) {
        mDatas.clear();
        String[] sArr = null;
        switch (type) {
            case GRID:
                sArr = getContext().getResources().getStringArray(R.array.grid_menu);
                break;
            case LIST:
                sArr = getContext().getResources().getStringArray(R.array.list_menu);
                break;
        }
        mDatas.addAll(Arrays.asList(sArr));
        mAdapter.notifyDataSetChanged();
        mWidth = 0;
        mHeight = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View view = mAdapter.getView(i, null, null);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mWidth = Math.max(view.getMeasuredWidth(), mWidth);
            mHeight = mHeight + view.getMeasuredHeight();
        }

        mListView.setLayoutParams(new LinearLayout.LayoutParams(mWidth, mHeight));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        String content = mDatas.get(position);
        if (content.equals(getContext().getString(R.string.open))) {
            U.launchApp(getContext(), mAppEntry.getComponentName());
            SqliteOperate.updateDataStorage(getContext(), mAppEntry);
        } else if (content.equals(getContext().getString(R.string.phone_mode))) {
            U.launchApp(getContext(), mAppEntry.getComponentName(), U.PHONE_MODE);
            SqliteOperate.updateDataStorage(getContext(), mAppEntry);
        } else if (content.equals(getContext().getString(R.string.desktop_mode))) {
            U.launchApp(getContext(), mAppEntry.getComponentName(), U.DESKTOP_MODE);
            SqliteOperate.updateDataStorage(getContext(), mAppEntry);
        } else if (content.equals(getContext().getString(R.string.lock_app))) {

        } else if (content.equals(getContext().getString(R.string.unlock_app))) {

        } else if (content.equals(getContext().getString(R.string.remove_out))) {
            StartupMenuDialog.getInstance(getContext()).mCommonDatas.remove(mAppEntry);
            StartupMenuDialog.getInstance(getContext()).mCommonAdapter.notifyDataSetChanged();
            SqliteOperate.deleteDataStorage(getContext(), mAppEntry.getPackageName());
        } else if (content.equals(getContext().getString(R.string.uninstall))) {
            uninstallApp();
        }
    }

    private void dismissAndHideStartMenu(boolean hideStartMenu) {
        dismiss();
    }

    private void uninstallApp() {
        Uri uri = Uri.parse("package:" + mAppEntry.getPackageName());
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
//                    && mInstance.isPinned(mAppEntry.getComponentName())) {
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
