package com.android.systemui.statusbar.notificationbars;

import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.content.pm.PackageManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.android.systemui.util.InputAppInfo;
import com.android.systemui.adapter.InputMethodAdapter;
import com.android.systemui.R;

import android.provider.Settings;

public class InputMethodDialog extends BaseSettingDialog {
    private ListView mInputListView;
    private InputMethodAdapter mInputMethodAdapter;
    private List<InputMethodInfo> mMethodList;
    private ArrayList<InputAppInfo> mListViewAppInfo;
    private InputMethodManager mInputMethodManager;
    private PackageManager mPm;

    public InputMethodDialog(Context context) {
        super(context);
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
        updateInputMethod();
    }

    @Override
    protected void initViews() {
        mContentView = LayoutInflater.from(mContext)
                .inflate(R.layout.status_bar_input_method, null);
        setContentView(mContentView);
        mInputListView = (ListView) mContentView.findViewById(R.id.input_lv_view);
        initData();
    }

    private void initData() {
        mListViewAppInfo = new ArrayList<InputAppInfo>();
        mInputMethodManager = (InputMethodManager) mContext.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        mPm = mContext.getPackageManager();
        mInputMethodAdapter =
                new InputMethodAdapter(mContext, mListViewAppInfo, mInputMethodManager);
        mInputListView.setAdapter(mInputMethodAdapter);
        mInputListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    public void updateInputMethod() {
        String currentInputMethodId = Settings.Secure.getString(
                mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        mListViewAppInfo.clear();
        mMethodList = mInputMethodManager.getEnabledInputMethodList();
        InputAppInfo appInfo;
        for (InputMethodInfo mi : mMethodList) {
            appInfo = new InputAppInfo();
            String name = (String) mi.loadLabel(mPm);
            appInfo.setName(name);
            appInfo.setSelected(currentInputMethodId.equals(mi.getId()));
            mListViewAppInfo.add(appInfo);
        }
        mInputMethodAdapter.notifyDataSetChanged();
        setListviewParams();
    }

    public void setListviewParams() {
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
}
