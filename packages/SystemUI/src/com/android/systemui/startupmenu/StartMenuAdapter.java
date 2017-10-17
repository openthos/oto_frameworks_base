/* Copyright 2016 Braden Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.startupmenu;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.dialog.MenuDialog;
import com.android.systemui.sql.SqliteOperate;

import java.util.List;

public class StartMenuAdapter extends BaseAdapter
        implements View.OnTouchListener, View.OnLongClickListener, View.OnHoverListener {

    private List<AppEntry> mDatas;
    private MenuDialog mMenuDialog;
    private Context mContext;
    private ShowType mType;
    private View mTempView;
    private boolean mIsLongPress;
    private boolean mIsLeftKey;
    private boolean mIsRightKey;
    private boolean isGrid;
    private int mDownX;
    private int mDownY;

    public StartMenuAdapter(Context context, ShowType type, List<AppEntry> datas) {
        mContext = context;
        mType = type;
        mDatas = datas;
        isGrid = mType == ShowType.GRID;
        mMenuDialog = MenuDialog.getInstance(context);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public AppEntry getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).
                    inflate(isGrid ? R.layout.row_alt : R.layout.row, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mDatas != null && mDatas.size() > 0) {
            AppEntry entry = getItem(position);

            holder.name.setText(entry.getLabel());
            holder.icon.setImageDrawable(entry.getIcon());
            holder.layout.setTag(entry);
        }
        return convertView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        AppEntry appInfo = (AppEntry) v.getTag();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsLongPress = false;
                mIsLeftKey = false;
                mIsRightKey = false;
                mDownX = (int) event.getRawX();
                mDownY = (int) event.getRawY();
                switch (event.getButtonState()) {
                    case MotionEvent.BUTTON_PRIMARY:
                        mIsLeftKey = true;
                        openApplication(appInfo);
                        break;
                    case MotionEvent.BUTTON_SECONDARY:
                        mIsRightKey = true;
                        showDialog(mDownX, mDownY, appInfo);
                        break;
                    default:
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!(mIsLeftKey || mIsRightKey || mIsLongPress)) {
//                    openApplication(appInfo);
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_HOVER_ENTER:
                if (mTempView != null && mTempView != v) {
                    mTempView.setBackgroundResource(getExitResource());
                }
                v.setBackgroundResource(getEnterResource());
                mTempView = v;
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                if (mTempView != v) {
                    v.setBackgroundResource(getExitResource());
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        mIsLongPress = true;
//        showDialog(mDownX, mDownY, (AppEntry) v.getTag());
        return false;
    }

    private void openApplication(AppEntry appInfo) {
        U.launchApp(mContext, appInfo.getComponentName());
        SqliteOperate.updateDataStorage(mContext, appInfo);
    }

    private class ViewHolder {
        LinearLayout layout;
        ImageView icon;
        TextView name;

        public ViewHolder(View view) {
            layout = (LinearLayout) view.findViewById(R.id.entry);
            icon = (ImageView) view.findViewById(R.id.icon);
            name = (TextView) view.findViewById(R.id.name);
            layout.setOnTouchListener(StartMenuAdapter.this);
            layout.setOnLongClickListener(StartMenuAdapter.this);
            layout.setOnHoverListener(StartMenuAdapter.this);
        }
    }

    private int getExitResource() {
        return isGrid ? R.color.grid_unhover_bg : R.color.common_hover_bg;
    }

    private int getEnterResource() {
        return isGrid ? R.color.grid_hover_bg : R.mipmap.common_bg;
    }

    private void showDialog(int x, int y, AppEntry appInfo) {
        Log.i("ljh", "弹菜单");
        mMenuDialog.show(mType == ShowType.GRID ? DialogType.GRID : DialogType.LIST, appInfo, x, y);

    }
}
