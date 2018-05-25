package com.otosoft.setupwizard;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import org.openthos.seafile.ISeafileService;

//Rewrite Register function
public class OpenthosIDRegister extends BaseActivity {
    private TextView mPrev;
    private TextView mRegister;
    private EditText mEditTextOpenthosID, mEditTextOpenthosEmail;
    private EditText mEditTextOpenthosPass, mEditTextOpenthosPassConfirm;
    private String openthosID, openthosEmail;
    private String password;
    private String confirmPassword;
    private Handler mHandler;
    private ConnectivityManager mCM;
    private String TAG = "OpenthosIDRegister";
    private ISeafileService iSeafileService;
    private IBinder mSeafileBinder = new SeafileBinder();
    public static final int MSG_REGIST_SEAFILE = 0x1001;
    public static final int MSG_REGIST_SEAFILE_OK = 0x1004;
    public static final int MSG_REGIST_SEAFILE_FAILED = 0x1005;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_register);
        //back
        mPrev = (TextView) findViewById(R.id.tv_prev);
        //register
        mRegister = (TextView) findViewById(R.id.tv_register);
        //input user name
        mEditTextOpenthosID = (EditText) findViewById(R.id.edittext_openthos_id);
        //input user email
        mEditTextOpenthosEmail = (EditText) findViewById(R.id.edittext_openthos_email);
        //input user password
        mEditTextOpenthosPass = (EditText) findViewById(R.id.edittext_openthos_pass);
        //confirm user password
        mEditTextOpenthosPassConfirm = (EditText) findViewById(R.id.edittext_openthos_pass_confirm);

        mCM = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_REGIST_SEAFILE:
                        iSeafileService
                                = ((SetupWizardApplication) getApplication()).mISeafileService;
                        try {
                            iSeafileService.setBinder(mSeafileBinder);
                            iSeafileService.registeAccount(openthosID, openthosEmail, password);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MSG_REGIST_SEAFILE_OK:
                        try {
                            iSeafileService.unsetBinder(mSeafileBinder);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(OpenthosIDRegister.this,
                                msg.obj.toString(),
                                Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case MSG_REGIST_SEAFILE_FAILED:
                        Toast.makeText(OpenthosIDRegister.this,
                                msg.obj.toString(),
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(OpenthosIDRegister.this,
                                getText(R.string.toast_network_not_connect),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        mPrev.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                OpenthosIDRegister.this.onBackPressed();
            }
        });
        mRegister.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                NetworkInfo networkINfo = mCM.getActiveNetworkInfo();
                if (networkINfo == null) {
                   Toast.makeText(OpenthosIDRegister.this,
                        getText(R.string.toast_network_not_connect),
                        Toast.LENGTH_SHORT).show();
                }

                openthosID = mEditTextOpenthosID.getText().toString().trim();
                openthosEmail = mEditTextOpenthosEmail.getText().toString().trim();
                password = mEditTextOpenthosPass.getText().toString().trim();
                confirmPassword = mEditTextOpenthosPassConfirm.getText().toString().trim();
                mHandler.sendEmptyMessage(MSG_REGIST_SEAFILE);
            }
        });

    }

    public void onResume() {
        super.onResume();
    }

    private class SeafileBinder extends Binder {

        @Override
        protected boolean onTransact(
                int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                    android.util.Log.i("chenpeng_SeafileBinder", code + "");
            if (code == iSeafileService.getCodeRegiestSuccess()) {
                Message msg = new Message();
                msg.obj = data.readString();
                msg.what = MSG_REGIST_SEAFILE_OK;
                mHandler.sendMessage(msg);
                reply.writeNoException();
                return true;
            } else if (code == iSeafileService.getCodeRegiestFailed()) {
                Message msg = new Message();
                msg.obj = data.readString();
                msg.what = MSG_REGIST_SEAFILE_FAILED;
                mHandler.sendMessage(msg);
                reply.writeNoException();
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }
}
