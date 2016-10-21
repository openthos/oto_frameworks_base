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
import android.widget.Toast;
import android.widget.GridView;
import android.widget.AdapterView;
import android.content.pm.PackageManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import com.android.systemui.util.InputAppInfo;
import com.android.systemui.adapter.InputMethodAdapter;
import com.android.systemui.R;

public class InputMethodDialog extends BaseSettingDialog {
    private GridView mInputGridView;
    private String mFirstInputName, mLastInputName;
    private InputMethodAdapter mInputMethodAdapter;
    private CharSequence mCharName;
    public static ArrayList<InputAppInfo> mGridViewAppInfo = null;

    public InputMethodDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void show(View v) {
        super.show(v);
    }

    @Override
    protected void initViews() {
        final AudioManager audioManager = (AudioManager) mContext.getSystemService(
                                                                      Context.AUDIO_SERVICE);
        View mediaView = LayoutInflater.from(mContext)
                                       .inflate(R.layout.status_bar_input_method, null);
        setContentView(mediaView);
        mInputGridView = (GridView) mediaView.findViewById(R.id.input_gv_view);
        mContentView = mediaView;
        mGridViewAppInfo = new ArrayList<InputAppInfo>();
        inputMethond();
        mInputMethodAdapter = new InputMethodAdapter(mContext, mGridViewAppInfo);
        mInputGridView.setAdapter(mInputMethodAdapter);
        mInputGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ((InputMethodManager)mContext.getSystemService("input_method"))
                                                       .showInputMethodPicker();
            }
        });
    }

    public void inputMethond() {
        InputMethodManager imm = (InputMethodManager)
                                 mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> methodList = imm.getInputMethodList();
        PackageManager pm = mContext.getPackageManager();
        for (InputMethodInfo mi : methodList ) {
            mCharName = mi.loadLabel(pm);
            String name = (String) mCharName;
            InputAppInfo appInfo = new InputAppInfo();
            appInfo.setName(name);
            mGridViewAppInfo.add(appInfo);
        }
    }
}
