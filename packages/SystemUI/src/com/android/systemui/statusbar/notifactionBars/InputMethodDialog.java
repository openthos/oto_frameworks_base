package com.android.systemui.statusbar.notificationbars;

import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.content.pm.PackageManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import com.android.systemui.util.InputAppInfo;
import com.android.systemui.adapter.InputMethodAdapter;
import com.android.systemui.R;
import java.util.Map;
import java.util.HashMap;
import android.content.Intent;
import android.util.Log;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.widget.Toast;

public class InputMethodDialog extends BaseSettingDialog {
    private ListView mInputListView;
    private String mFirstInputName, mLastInputName;
    private InputMethodAdapter mInputMethodAdapter;
    private CharSequence mCharName;
    private Map<Integer,Boolean> mIsSelected ;
    private List mBeSelectedData = new ArrayList();
    private List<InputMethodInfo> mMethodList;
    public static ArrayList<InputAppInfo> mListViewAppInfo = null;
    private InputMethodManager mImm;
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
        if (mIsSelected != null) {
            mIsSelected = null;
        }
        mIsSelected = new HashMap<Integer, Boolean>();
        final AudioManager audioManager = (AudioManager) mContext.getSystemService(
                                                                Context.AUDIO_SERVICE);
        View mediaView = LayoutInflater.from(mContext)
                                       .inflate(R.layout.status_bar_input_method, null);
        setContentView(mediaView);
        mInputListView = (ListView) mediaView.findViewById(R.id.input_lv_view);
        mContentView = mediaView;
        mListViewAppInfo = new ArrayList<InputAppInfo>();
        inputMethod();
        mInputMethodAdapter = new InputMethodAdapter(mContext, mListViewAppInfo,
                                                     mIsSelected, mBeSelectedData);
        mInputListView.setAdapter(mInputMethodAdapter);
        mInputListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    public void inputMethod() {
        mImm = (InputMethodManager) mContext.getSystemService(
                                                    Context.INPUT_METHOD_SERVICE);
        mPm = mContext.getPackageManager();
        mMethodList = mImm.getInputMethodList();
    }

    public void updateInputMethod() {
        if (mIsSelected != null) {
            mIsSelected = null;
        }
        mIsSelected = new HashMap<Integer, Boolean>();
        String currentInputMethodId = Settings.Secure.getString(
               mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        mListViewAppInfo.clear();
        mMethodList.clear();
        mMethodList = mImm.getInputMethodList();
        for (InputMethodInfo mi : mMethodList) {
            mCharName = mi.loadLabel(mPm);
            String name = (String) mCharName;
            InputAppInfo appInfo = new InputAppInfo();
            appInfo.setName(name);
            mListViewAppInfo.add(appInfo);
        }
        for (int i = 0; i< mListViewAppInfo.size(); i++) {
            mIsSelected.put(i, false);
        }
        if (mBeSelectedData.size() > 0) {
            mBeSelectedData.clear();
        }
        if (mInputMethodAdapter != null) {
            mInputMethodAdapter = null;
        }
        mInputMethodAdapter = new InputMethodAdapter(mContext, mListViewAppInfo,
                                                     mIsSelected, mBeSelectedData);
        mInputListView.setAdapter(mInputMethodAdapter);
    }
}
