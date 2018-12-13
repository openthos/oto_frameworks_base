package com.otosoft.setupwizard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import org.openthos.seafile.ISeafileService;

public class OpenthosIDSetupActivity extends BaseActivity {
    private TextView mVerify;
    private TextView mPrev;
    private EditText mEditTextOpenthosID;
    private EditText mEditTextPassword;
    private TextView mSkip;
    private TextView mRegister;
    private String openthosID;
    private String openthosEmail;
    private String password;
    private Handler mHandler;
    static final int RG_REQUEST = 0;
    public static final int MSG_LOGIN_SEAFILE_OK = 0x1006;
    public static final int MSG_LOGIN_SEAFILE_FAILED = 0x1007;
    public static final int MSG_LOGIN_SEAFILE = 0x1008;
    private ConnectivityManager mCM;
    private ISeafileService iSeafileService;
    private IBinder mSeafileBinder = new SeafileBinder();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_openthos_id_setup);

        mPrev = (TextView) findViewById(R.id.tv_prev);
        mVerify = (TextView) findViewById(R.id.tv_verify);
        this.mEditTextOpenthosID = (EditText) findViewById(R.id.edittext_openthos_id);
        this.mEditTextPassword = (EditText) findViewById(R.id.edittext_openthos_password);
        mSkip = (TextView) findViewById(R.id.tv_skip);
        mRegister = (TextView) findViewById(R.id.tv_register);

        mCM = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_LOGIN_SEAFILE:
                        iSeafileService
                                = ((SetupWizardApplication) getApplication()).mISeafileService;
                        try {
                            iSeafileService.setBinder(mSeafileBinder);
                            iSeafileService.loginAccount(openthosID,  password);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MSG_LOGIN_SEAFILE_OK:
                        Toast.makeText(OpenthosIDSetupActivity.this,
                                getText(R.string.toast_verify_successful),
                                Toast.LENGTH_SHORT).show();
                        try {
                            iSeafileService.unsetBinder(mSeafileBinder);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent();
                        intent.setAction("com.android.wizard.STARTUSE");
                        startActivity(intent);
                        break;
                    case MSG_LOGIN_SEAFILE_FAILED:
                        Toast.makeText(OpenthosIDSetupActivity.this, msg.obj.toString(),
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(OpenthosIDSetupActivity.this,
                                getText(R.string.toast_network_not_connect),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        mVerify.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                openthosID = mEditTextOpenthosID.getText().toString().trim();
                password = mEditTextPassword.getText().toString().trim();
                NetworkInfo networkINfo = mCM.getActiveNetworkInfo();
                if (networkINfo == null) {
                    Toast.makeText(OpenthosIDSetupActivity.this,
                            getText(R.string.toast_network_not_connect),
                            Toast.LENGTH_SHORT).show();
                }
                //verify openthos id and password
                if (openthosID.isEmpty()) {
                    Toast.makeText(OpenthosIDSetupActivity.this,
                            getText(R.string.toast_openthos_id_empty),
                            Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(OpenthosIDSetupActivity.this,
                            getText(R.string.toast_openthos_password_empty),
                            Toast.LENGTH_SHORT).show();
                } else {
                    mHandler.sendEmptyMessage(MSG_LOGIN_SEAFILE);
                }
            }
        });
        mPrev.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                OpenthosIDSetupActivity.this.onBackPressed();
            }
        });
        this.mSkip.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.android.wizard.STARTUSE");
                startActivity(intent);
            }
        });

        this.mRegister.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.android.wizard.REGISTER");
                startActivity(intent);
            }
        });
    }

    public void onResume() {
        super.onResume();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RG_REQUEST) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(OpenthosIDSetupActivity.this,
                               getText(R.string.toast_openthos_register_cancel),
                               Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SeafileBinder extends Binder {

        @Override
        protected boolean onTransact(
                int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                    android.util.Log.i("chenpeng_SeafileBinder", code + "");
            if (code == iSeafileService.getCodeLoginSuccess()) {
                mHandler.sendEmptyMessage(MSG_LOGIN_SEAFILE_OK);
                reply.writeNoException();
                return true;
            } else if (code == iSeafileService.getCodeLoginFailed()) {
                Message msg = new Message();
                msg.obj = data.readString();
                msg.what = MSG_LOGIN_SEAFILE_FAILED;
                mHandler.sendMessage(msg);
                reply.writeNoException();
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }
}
