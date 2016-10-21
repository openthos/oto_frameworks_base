package com.android.systemui.adapter;

import java.util.List;
import java.util.Map;

import com.android.systemui.R;
import android.content.ContentValues;
import com.android.systemui.util.InputAppInfo;
import android.database.Cursor;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.UserHandle;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;

public class InputMethodAdapter extends BaseAdapter {
    private List<InputAppInfo> mListAppInfo = null;
    LayoutInflater mInfater = null;
    private Context mContext;

    public InputMethodAdapter(Context context, List<InputAppInfo> apps) {
        mInfater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListAppInfo = apps;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mListAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mListAppInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertview, ViewGroup arg2) {
        View view = null;
        ViewHolder holder = null;
        if (convertview == null || convertview.getTag() == null) {
            view = mInfater.inflate(R.layout.status_bar_inputmethod_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertview;
            holder = (ViewHolder) convertview.getTag();
        }
        InputAppInfo appInfo = (InputAppInfo) getItem(position);
        String appName = appInfo.getName();
        holder.tvAppLabel.setText(appName);
        view.setOnHoverListener(hoverListener);
        return view;
    }

    View.OnHoverListener hoverListener = new View.OnHoverListener() {
        public boolean onHover(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundResource(R.color.inputmethodfocus);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundResource(android.R.color.transparent);
                    break;
            }
            return false;
        }
    };

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.input_method_checkbox);
            this.tvAppLabel = (TextView) view.findViewById(R.id.input_method_name);
        }
    }
}
