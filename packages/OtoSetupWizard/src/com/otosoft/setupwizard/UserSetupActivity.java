package com.otosoft.setupwizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemProperties;
import android.app.admin.DevicePolicyManager;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;

import java.io.File;

import com.android.internal.widget.LockPatternUtils;

public class UserSetupActivity extends BaseActivity {
    private TextView mNext;
    private TextView mPrev;
    private EditText mUsername;
    private EditText mComputername;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private LockPatternUtils mLockPatternUtils;
    private TextView mSkip;
    private static final String RO_PROPERTY_HOST = "ro.build.host";
    private static final String RO_PROPERTY_USER = "ro.build.user";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setup);

        mPrev = (TextView) findViewById(R.id.tv_prev);
        mNext = (TextView) findViewById(R.id.tv_finish);
        mUsername = (EditText) findViewById(R.id.edittext_username);
        mComputername = (EditText) findViewById(R.id.edittext_computer_name);
        mPassword = (EditText) findViewById(R.id.edittext_screen_password);
        mConfirmPassword = (EditText) findViewById(R.id.edittext_screen_password_confirm);
        mSkip = (TextView) findViewById(R.id.tv_skip);
        mLockPatternUtils = new LockPatternUtils(this);

        String defaultComputerName = SystemProperties.get(RO_PROPERTY_HOST);
        mComputername.setText(defaultComputerName);
        String userName = SystemProperties.get(RO_PROPERTY_USER);
        if (TextUtils.isEmpty(userName)) {
            mUsername.setText("Owner");
        }

        mNext.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String computerName = mComputername.getText().toString().trim();
                if (!TextUtils.isEmpty(computerName)) {
                    Settings.System.putString(getContentResolver(),
                                          Settings.System.SYS_PROPERTY_HOST, computerName);
                } else {
                    Toast.makeText(UserSetupActivity.this,
                            getText(R.string.toast_openthos_register_computer_name),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String userName = mUsername.getText().toString().trim();
                if (!TextUtils.isEmpty(userName)) {
                    Settings.System.putString(getContentResolver(),
                                          Settings.System.SYS_PROPERTY_USER, userName);
                } else {
                    Toast.makeText(UserSetupActivity.this,
                            getText(R.string.toast_openthos_register_user_name),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String password = mPassword.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(UserSetupActivity.this,
                            getText(R.string.toast_openthos_register_password_empty),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String confirmPassword = mConfirmPassword.getText().toString().trim();
                if (TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(UserSetupActivity.this,
                            getText(R.string.toast_openthos_register_password_empty),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.equals(confirmPassword)) {
                    mLockPatternUtils.saveLockPassword(confirmPassword,
                                         DevicePolicyManager.PASSWORD_QUALITY_NUMERIC, false);
                    startActivity();
                } else{
                    Toast.makeText(UserSetupActivity.this,
                            getText(R.string.toast_openthos_register_password_not_equals),
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
                showWarningDialog();
            }
        });
    }

    private void showWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getText(R.string.warning_dialog_message));
        builder.setTitle(getText(R.string.warning_dialog_title));
        builder.setPositiveButton(getText(R.string.warning_dialog_ok),
                                          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getText(R.string.warning_dialog_cancel),
                                         new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void startActivity() {
        File file = new File("/data/vendor/app");
        SharedPreferences sp = getSharedPreferences(PRE_INSTALL_CACHE, Context.MODE_PRIVATE);
        boolean initializeFinish = sp.getBoolean(INSTALLED_FINISH, false);
        if (file.exists() && file.listFiles().length > 1 && !initializeFinish) {
            startActivity(new Intent("com.android.wizard.INITIALIZE"));
        } else {
            startActivity(new Intent("com.android.wizard.STARTUSE"));
        }
    }

    public void onResume() {
        super.onResume();
    }

}
