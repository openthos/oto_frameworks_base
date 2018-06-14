package com.android.startupmenu;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.TextView;

import com.android.startupmenu.bean.AppInfo;
import com.android.startupmenu.bean.Type;
import com.android.startupmenu.listener.OnMenuClick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ljh on 17-9-14.
 */

public class MenuDialog extends Dialog implements AdapterView.OnItemClickListener {

    public static final String URI_CONTENT_STATUS_BAR =
            "content://com.android.systemui.util/status_bar_tb";
    private static MenuDialog listDialog;
    protected static Point mPoint;
    private List<String> mDatas;
    private DialogAdapter mAdapter;
    private OnMenuClick mOnMenuClick;
    private ListView mListView;
    private int mWidth;
    private int mHeight;
    private int mStatusBarHeight;
    private AppInfo mAppInfo;
    private String mPkgName;

    public static MenuDialog getInstance(Context context) {
        if (listDialog == null) {
            listDialog = new MenuDialog(context);
        }
        return listDialog;
    }

    private MenuDialog(Context context) {
        super(context, R.style.dialog);
        create();
    }

    public void initListener() {
        mListView.setOnItemClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startmenu_dialog_menu);
        if (mPoint == null) {
            Display defaultDisplay = ((WindowManager)
                    getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            mPoint = new Point();
            defaultDisplay.getRealSize(mPoint);
        }
        initView();
        initData();
        initListener();
    }

    public void initView() {
        mListView = (ListView) findViewById(R.id.list_view);
    }

    public void initData() {
        mDatas = new ArrayList<>();
        mAdapter = new DialogAdapter();
        mListView.setAdapter(mAdapter);
    }

    public void setOnMenuClick(OnMenuClick menuClick) {
        mOnMenuClick = menuClick;
    }

    public void showSort(View view) {
        mAppInfo = null;
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        Window dialogWindow = getWindow();
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.format = PixelFormat.TRANSPARENT;
        lp.dimAmount = 0;

        prepareData(Type.SORT);
        lp.x = location[0] + view.getWidth() - mWidth;
        lp.y = location[1] + view.getHeight();
        dialogWindow.setAttributes(lp);
        show();
    }

    public void show(Type type, AppInfo appInfo, int x, int y) {
        mAppInfo = appInfo;
        mPkgName = appInfo.getPkgName();
        show(type, x, y);
    }

    private void show(Type type, int x, int y) {
        Window dialogWindow = getWindow();
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.format = PixelFormat.TRANSPARENT;
        lp.dimAmount = 0;
        prepareData(type);
        lp.x = x;
        if (y + mHeight + mStatusBarHeight < mPoint.y) {
            lp.y = y;
        } else {
            lp.y = y - mHeight;
        }
        dialogWindow.setAttributes(lp);
        show();
    }

    private void prepareData(Type type) {
        mDatas.clear();
        String[] sArr = null;
        switch (type) {
            case GRID:
                if (queryData(mPkgName)) {
                    sArr = getContext().getResources().getStringArray(R.array.grid_menu_lock);
                } else {
                    sArr = getContext().getResources().getStringArray(R.array.grid_menu_unlock);
                }
                break;
            case LIST:
                sArr = getContext().getResources().getStringArray(R.array.list_menu);
                break;
            case SORT:
                sArr = getContext().getResources().getStringArray(R.array.sort_show);
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

    private boolean queryData(String str) {
        Uri uri = Uri.parse(URI_CONTENT_STATUS_BAR);
        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor cursor = contentResolver.query(
                uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String strname = cursor.getString(1);
                if (strname.equals(str)) {
                    return true;
                }
            }
            cursor.close();
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.startmenu_item_dialog, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String name = mDatas.get(position);
            if (mAppInfo != null) {
                if (name.equals(getContext().getResources().getString(R.string.phone_mode))) {
                    holder.name.setEnabled(mAppInfo.isSystemApp()
                            ? false : (mAppInfo.isFullScreen() ? false : true));
                } else if (name.equals(getContext().getResources().getString(R.string.desktop_mode))) {
                    holder.name.setEnabled(!mAppInfo.isSystemApp());
                }
                holder.name.setTextColor(getContext().getResources().getColor(holder.name.isEnabled()
                        ? android.R.color.black : android.R.color.darker_gray));
            } else {
                holder.name.setEnabled(true);
                holder.name.setTextColor(
                        getContext().getResources().getColor(android.R.color.black));
            }
            holder.name.setText(name);
            return convertView;
        }

        private class ViewHolder implements View.OnHoverListener, View.OnClickListener {
            private TextView name;

            public ViewHolder(View view) {
                name = (TextView) view.findViewById(R.id.name);
                name.setOnHoverListener(this);
                name.setOnClickListener(this);
            }

            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        v.setBackgroundResource(R.color.blue);
                        ((TextView) v).setTextColor(getContext()
                                .getResources().getColor(android.R.color.white));
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        v.setBackgroundResource(android.R.color.white);
                        ((TextView) v).setTextColor(getContext()
                                .getResources().getColor(android.R.color.black));
                        break;
                }
                return false;
            }

            @Override
            public void onClick(View v) {
                if (mOnMenuClick != null && mAppInfo != null) {
                    mOnMenuClick.menuClick(v, listDialog,
                            mAppInfo, ((TextView) v).getText().toString());

                } else if (mOnMenuClick != null) {
                    mOnMenuClick.sortShow(v, listDialog, ((TextView) v).getText().toString());
                }
            }
        }
    }
}
