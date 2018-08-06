package com.android.systemui.adapter;

import java.util.List;

import com.android.systemui.R;
import com.android.systemui.util.InputAppInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodInfo;

public class InputMethodAdapter extends BaseAdapter {
    private List<InputAppInfo> mInputAppInfos;
    private Context mContext;
    private InputMethodManager mInputMethodManager;

    public InputMethodAdapter(Context context,
                              List<InputAppInfo> apps,InputMethodManager inputMethodManager) {
        mContext = context;
        mInputAppInfos = apps;
        mInputMethodManager = inputMethodManager;
    }

    @Override
    public int getCount() {
        return mInputAppInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mInputAppInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertview, ViewGroup arg2) {
        View view = null;
        ViewHolder holder = null;
        if (convertview == null || convertview.getTag() == null) {
            view = LayoutInflater.from(mContext).
                         inflate(R.layout.status_bar_inputmethod_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertview;
            holder = (ViewHolder) convertview.getTag();
        }
        final InputAppInfo appInfo = mInputAppInfos.get(position);
        holder.tvAppLabel.setText(appInfo.getName());
        view.setOnHoverListener(hoverListener);
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mInputMethodManager.showInputMethodPicker();
            }
        });
        holder.checkBox.setChecked(appInfo.isSelected());
        return view;
    }

    private void setSelected(InputAppInfo appInfo) {
        if (appInfo.isSelected()) {
            return;
        }
        appInfo.setSelected(true);
        for (int i = 0; i < mInputAppInfos.size(); i++) {
            InputAppInfo inputAppInfo = mInputAppInfos.get(i);
            if (inputAppInfo.isSelected()) {
                inputAppInfo.setSelected(false);
            }
        }
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
        CheckBox checkBox;
        TextView tvAppLabel;

        public ViewHolder(View view) {
            this.checkBox = (CheckBox) view.findViewById(R.id.input_method_checkbox);
            this.tvAppLabel = (TextView) view.findViewById(R.id.input_method_name);
        }
    }
}
