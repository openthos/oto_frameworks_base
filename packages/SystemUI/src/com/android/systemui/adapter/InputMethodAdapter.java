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
import android.widget.CheckBox;
import android.widget.TextView;
import android.provider.Settings;
import android.os.UserHandle;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodInfo;

public class InputMethodAdapter extends BaseAdapter {
    private List<InputAppInfo> mListAppInfo = null;
    LayoutInflater mInfater = null;
    private Context mContext;
    private List mBeSelectedData;
    private Map<Integer, Boolean> mBsSelected;
    private InputMethodManager input_method;
    private final String currentInputMethodId;
    private final List<InputMethodInfo> inputMethodList;

    public InputMethodAdapter(Context context, List<InputAppInfo> apps,
                      Map<Integer, Boolean> isSelected, List mBeSelectedData) {
        mInfater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListAppInfo = apps;
        mContext = context;
        input_method = (InputMethodManager) context.getSystemService("input_method");
        inputMethodList = input_method.getInputMethodList();
        currentInputMethodId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        mBeSelectedData = mBeSelectedData;
        mIsSelected = isSelected;
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
        return position;
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
        String imiId = inputMethodList.get(position).getId();
        mIsSelected.put(position,imiId.equals(currentInputMethodId));
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mIsSelected.get(position)) {
                    return;
                }
                boolean cu = !mIsSelected.get(position);
                for (Integer p : mIsSelected.keySet()) {
                    mIsSelected.put(p, false);
                }
                mIsSelected.put(position, cu);
                InputMethodAdapter.this.notifyDataSetChanged();
                mBeSelectedData.clear();
                if (cu) {
                    mBeSelectedData.add(mListAppInfo.get(position));
                }
                input_method.showInputMethodPicker();
            }
        });
        holder.checkBox.setChecked(mIsSelected.get(position));
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
        CheckBox checkBox;
        TextView tvAppLabel;

        public ViewHolder(View view) {
            this.checkBox = (CheckBox) view.findViewById(R.id.input_method_checkbox);
            this.tvAppLabel = (TextView) view.findViewById(R.id.input_method_name);
        }
    }
}
