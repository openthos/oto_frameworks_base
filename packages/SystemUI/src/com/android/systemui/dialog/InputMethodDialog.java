package com.android.systemui.dialog;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.systemui.R;

import java.util.List;

public class InputMethodDialog extends BaseDialog {
    private ListView mInputListView;
    private InputMethodAdapter mInputMethodAdapter;

    public InputMethodDialog(Context context) {
        super(context);
        mContentView = LayoutInflater.from(getContext())
                .inflate(R.layout.status_bar_input_method, null);
        setContentView(mContentView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void show(View v) {
        super.show(v);
        setListviewParams();
    }

    @Override
    public void initView() {
        mInputListView = (ListView) mContentView.findViewById(R.id.input_lv_view);
    }

    public void initData() {
        mInputMethodAdapter = new InputMethodAdapter(getContext());
        mInputListView.setAdapter(mInputMethodAdapter);
        mInputListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    public void setListviewParams() {
        mInputMethodAdapter.refresh();
        int maxWidth = 0;
        int height = 0;
        for (int i = 0; i < mInputMethodAdapter.getCount(); i++) {
            View listItem = mInputMethodAdapter.getView(i, null, mInputListView);
            listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int width = listItem.getMeasuredWidth();
            if (width > maxWidth) {
                maxWidth = width;
            }
            height = height + listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = mInputListView.getLayoutParams();
        params.width = maxWidth;
        params.height = height;
        mInputListView.setLayoutParams(params);
    }

    private class InputMethodAdapter extends BaseAdapter {
        private final PackageManager mPackageManager;
        private Context mContext;
        private InputMethodManager mInputMethodManager;
        private List<InputMethodInfo> mMethodList;
        private String mCurrentInputMethodId;

        public InputMethodAdapter(Context context) {
            mContext = context;
            mInputMethodManager =
                    (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            mPackageManager = mContext.getPackageManager();
            initAppInfos();
        }

        @Override
        public int getCount() {
            return mMethodList.size();
        }

        @Override
        public Object getItem(int position) {
            return mMethodList.get(position);
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
            final InputMethodInfo inputMethodInfo = mMethodList.get(position);
            holder.label.setText(inputMethodInfo.loadLabel(mPackageManager));
            view.setOnHoverListener(hoverListener);
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Settings.Secure.putString(mContext.getContentResolver(),
                            Settings.Secure.DEFAULT_INPUT_METHOD, inputMethodInfo.getId());
                    dismiss();
                }
            });
            if (mCurrentInputMethodId.equals(inputMethodInfo.getId())) {
                holder.img.setImageDrawable(
                        mContext.getDrawable(android.R.drawable.checkbox_on_background));
            } else {
                holder.img.setImageDrawable(null);
            }
            return view;
        }

        public void refresh() {
            initAppInfos();
            notifyDataSetChanged();
        }

        private void initAppInfos() {
            mMethodList = mInputMethodManager.getInputMethodList();
            mCurrentInputMethodId = Settings.Secure.getString(
                    mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
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
            ImageView img;
            TextView label;

            public ViewHolder(View view) {
                img = (ImageView) view.findViewById(R.id.input_method_img);
                label = (TextView) view.findViewById(R.id.input_method_name);
            }
        }
    }
}
