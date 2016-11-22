package com.otosoft.setupwizard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.SystemProperties;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.content.Context;

import java.io.IOException;
import java.io.DataOutputStream;

import com.otosoft.tools.ChangeBuildPropTools;
import com.android.internal.widget.LockPatternUtils;

public class UserSetupActivity extends BaseActivity {
    private TextView mFinish;
    private TextView mPrev;
    private EditText mEditTextUsername;
    private EditText mComputername;
    private EditText mOldPassword;
    private EditText mNewPassword;
    private String screenPassword;
    private DevicePolicyManager devicePolicyManager;
    private LockPatternUtils mLockPatternUtils;
    private TextView mSkip;
    private String defaultComputerName;
    private String computerName;
    private String userName;
    private String oldPassword;
    private String newPassword;
    private static final String RO_PROPERTY_HOST = "ro.build.host";
    private static final String RO_PROPERTY_USER = "ro.build.user";

    private final Runnable mRequestFocus = new Runnable() {
        public void run() {
            if ((UserSetupActivity.this.mFinish != null)
                && UserSetupActivity.this.mFinish.isEnabled()) {
                UserSetupActivity.this.mFinish.requestFocusFromTouch();
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setup);
        mPrev = (TextView) findViewById(R.id.tv_prev);
        mFinish = (TextView) findViewById(R.id.tv_finish);
        this.mEditTextUsername = (EditText) findViewById(R.id.edittext_username);
        this.mComputername = (EditText) findViewById(R.id.edittext_computer_name);
        this.mOldPassword = (EditText) findViewById(R.id.edittext_screen_password);
        this.mNewPassword = (EditText) findViewById(R.id.edittext_screen_password_confirm);
        mSkip = (TextView) findViewById(R.id.tv_skip);
        mLockPatternUtils = new LockPatternUtils(this);
        defaultComputerName = SystemProperties.get("ro.build.host");
        this.mComputername.setText(defaultComputerName);
        userName = SystemProperties.get(RO_PROPERTY_USER);
        if (TextUtils.isEmpty(userName)) {
            this.mEditTextUsername.setText("Owner");
        }
//        String userName = UserManager.get(this).getUserName();
//        if (!TextUtils.equals(userName, "\u673a\u4e3b")) {
//            this.mEditTextUsername.setText(userName);
//        }
        devicePolicyManager = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);

        mFinish.setEnabled(!TextUtils.isEmpty(this.mEditTextUsername.getText().toString().trim()));
        this.mEditTextUsername.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != 0 && actionId != 6) {
                    return false;
                }
                ((InputMethodManager) UserSetupActivity.this.getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
                String oldUserName = UserManager.get(UserSetupActivity.this).getUserName();
                String newUserName = UserSetupActivity.this.mEditTextUsername.getText().toString();
                if (!TextUtils.isEmpty(newUserName)) {
                    if (!newUserName.equals(oldUserName)) {
                        UserManager.get(UserSetupActivity.this).setUserName(UserHandle.myUserId(), newUserName);
                    }
                }
                return true;
            }
        });
        this.mEditTextUsername.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                UserSetupActivity.this.mFinish.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
            }
        });
        mFinish.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                computerName = mComputername.getText().toString().trim();
                userName = mEditTextUsername.getText().toString().trim();
                oldPassword = mOldPassword.getText().toString().trim();
                newPassword = mNewPassword.getText().toString().trim();
                //grant permission
                ChangeBuildPropTools.exec("chmod -R 777  /system/build.prop");

                //save computer name
                ChangeBuildPropTools.setPropertyName(
                              ChangeBuildPropTools.getPropertyName(RO_PROPERTY_HOST,computerName));
                //save user name
                ChangeBuildPropTools.setPropertyName(
                                  ChangeBuildPropTools.getPropertyName(RO_PROPERTY_USER,userName));

                String oldUserName = UserManager.get(UserSetupActivity.this).getUserName();
                String newUserName = UserSetupActivity.this.mEditTextUsername.getText().toString();
                if (!(TextUtils.isEmpty(newUserName) || newUserName.equals(oldUserName))) {
                    UserManager.get(UserSetupActivity.this).setUserName(UserHandle.myUserId(), newUserName);
                }
                if (!TextUtils.isEmpty(oldPassword) && !TextUtils.isEmpty(newPassword) && oldPassword.equals(newPassword)) {
                    mLockPatternUtils.saveLockPassword(newPassword,
                                         DevicePolicyManager.PASSWORD_QUALITY_NUMERIC, false);
                    ChangeBuildPropTools.exec("chmod -R 644  /system/build.prop");
                    Intent intent = new Intent();
                    intent.setAction("com.android.wizard.STARTUSE");
                    startActivity(intent);
                }
                else{
                    Toast.makeText(UserSetupActivity.this,
                            getText(R.string.toast_openthos_register_password_wrong),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        mPrev.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                UserSetupActivity.this.onBackPressed();
            }
        });
        this.mSkip.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.android.wizard.STARTUSE");
                startActivity(intent);
            }
        });
    }

    public void onResume() {
        super.onResume();
        this.mRequestFocus.run();
        new Handler().postDelayed(this.mRequestFocus, 500);
    }

}
