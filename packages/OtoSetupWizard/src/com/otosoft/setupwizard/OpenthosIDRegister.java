package com.otosoft.setupwizard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;
import com.otosoft.tools.DatabaseHelper;
import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;

import java.io.FileWriter;
import java.io.File;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openthos.seafile.ISeafileService;

import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import android.content.Context;
import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

//Rewrite Register function
public class OpenthosIDRegister extends BaseActivity {
    private TextView mPrev;
    private TextView mRegister;
    private EditText mEditTextOpenthosID, mEditTextOpenthosEmail;
    private EditText mEditTextOpenthosPass, mEditTextOpenthosPassConfirm;
    private String openthosID, openthosEmail;
    private String password;
    private String confirmPassword;
    private int result;
    private Handler mHandler;
    private final Map<String,String> params = new HashMap<String,String>();
    private final String encode = "utf-8";
    static final int RG_REQUEST = 0;
    private ContentResolver mResolver;
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

        mResolver = getContentResolver();

        mCM = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HttpURLConnection.HTTP_OK:
                        Bundle b = msg.getData();
                        String result = b.getString("result");
                        Document doc = Jsoup.parse(result);
                        Log.i(TAG, "register result:");
                        Element masthead = doc.select("div.messages").first();
                        if ((masthead !=null)) {
                            Log.i(TAG, "register failed");
                            Toast.makeText(OpenthosIDRegister.this,
                                getText(R.string.toast_register_fail), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.i(TAG, "register OK");
                            Toast.makeText(OpenthosIDRegister.this,
                                getText(R.string.toast_register_successful),
                                Toast.LENGTH_SHORT).show();
                            NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(getApplicationContext())
                                    .setContentTitle("System notification")
                                    .setContentText("xxxx");
                            NotificationManager mNotificationManager =
                               (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(0, mBuilder.build());
                        }
                        break;
                    case MSG_REGIST_SEAFILE:
                        iSeafileService
                                = ((SetupWizardApplication) getApplication()).mISeafileService;
                        try {
                            iSeafileService.setBinder(mSeafileBinder);
                            iSeafileService.regiestAccount(openthosID, openthosEmail, password);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MSG_REGIST_SEAFILE_OK:
                        Toast.makeText(OpenthosIDRegister.this,
                                getText(R.string.toast_register_successful),
                                Toast.LENGTH_SHORT).show();
                        try {
                            iSeafileService.unsetBinder(mSeafileBinder);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        OpenthosIDRegister.this.onBackPressed();
                        break;
                    case MSG_REGIST_SEAFILE_FAILED:
                        Toast.makeText(OpenthosIDRegister.this,
                                getText(R.string.toast_register_failed),
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
                //Bundle bundle = new Bundle();
                //bundle.putString("id", openthosID);
                //bundle.putString("email", openthosEmail);
                //bundle.putString("passwd", password);
                //intent.putExtra("regist info", bundle);
                //params.put("name", openthosID);
                //params.put("mail", openthosID);
                //params.put("form_id", "user_register_form");
                //params.put("form_build_id", "form-WkUSPmAzO4z-HBjYe03NyRvjNsx44ZDrMGJ8nYAJWfU");

                NetworkInfo networkINfo = mCM.getActiveNetworkInfo();
                if (networkINfo == null) {
                   Toast.makeText(OpenthosIDRegister.this,
                        getText(R.string.toast_network_not_connect),
                        Toast.LENGTH_SHORT).show();
                }

                //submitPostData(params, encode);
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

    public void submitPostData(final Map<String, String> params, final String encode) {

        final  byte[] data = getRequestData(params, encode).toString().getBytes();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpURLConnection httpURLConnection =
                                (HttpURLConnection) HttpUtils.getHttpsURLConnection(
                                        "http://dev.openthos.org/?q=user/register");
                        httpURLConnection.setConnectTimeout(3000);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setUseCaches(false);
                        //set the request body type is text
                        httpURLConnection.setRequestProperty("Content-Type",
                                "application/x-www-form-urlencoded");
                        //set the request body length
                        httpURLConnection.setRequestProperty("Content-Length",
                                String.valueOf(data.length));
                        //get the ouput stream and write to the service
                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        outputStream.write(data);

                        int response = httpURLConnection.getResponseCode();
                        //get the service response
                        String data = new String();
                        if (response == HttpURLConnection.HTTP_OK) {
                            InputStream inptStream = httpURLConnection.getInputStream();
                            data = dealResponseResult(inptStream);
                        }
                        Message msg = Message.obtain();
                        msg.what = response;
                        Bundle bundle = new Bundle();
                        bundle.putString("result",data);
                        bundle.putString("resultID",openthosID);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }

    private class SeafileBinder extends Binder {

        @Override
        protected boolean onTransact(
                int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                    android.util.Log.i("chenpeng_SeafileBinder", code + "");
            if (code == iSeafileService.getCodeRegiestSuccess()) {
                mHandler.sendEmptyMessage(MSG_REGIST_SEAFILE_OK);
                reply.writeNoException();
                return true;
            } else if (code == iSeafileService.getCodeRegiestFailed()) {
                mHandler.sendEmptyMessage(MSG_REGIST_SEAFILE_FAILED);
                reply.writeNoException();
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }
}
