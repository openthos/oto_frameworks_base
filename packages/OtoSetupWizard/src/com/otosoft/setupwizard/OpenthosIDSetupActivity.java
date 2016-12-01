package com.otosoft.setupwizard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.net.NetworkInfo;
import android.util.Log;
import android.net.ConnectivityManager;
import android.content.Context;

public class OpenthosIDSetupActivity extends BaseActivity {
    private TextView mVerify;
    private TextView mPrev;
    private EditText mEditTextOpenthosID;
    private EditText mEditTextPassword;
    private TextView mSkip;
    private TextView mRegister;
    private String openthosID;
    private String password;
    private int result;
    private Handler mHandler;
    private final Map<String,String> params = new HashMap<String,String>();
    private final String encode = "utf-8";
    static final int RG_REQUEST = 0;
    private static String CODE_WRONG_USERNAME ="1002";
    private static String CODE_WRONG_PASSWORD ="1001";
    private static String CODE_SUCCESS ="1000";

    private ConnectivityManager mCM;
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
                    case HttpURLConnection.HTTP_OK:
                        Bundle b = msg.getData();
                        String result = b.getString("result");
                        String code = result.split(":")[1].split("\"")[1].trim();
                        if(CODE_WRONG_USERNAME.equals(code)) {
                            Toast.makeText(OpenthosIDSetupActivity.this,
                                    getText(R.string.toast_openthos_id_invalid),
                                    Toast.LENGTH_SHORT).show();
                        } else if (CODE_WRONG_PASSWORD.equals(code)) {
                            Toast.makeText(OpenthosIDSetupActivity.this,
                                    getText(R.string.toast_openthos_password_wrong),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent();
                            intent.setAction("com.android.wizard.FINISH");
                            startActivity(intent);
                        }
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
                params.put("username", openthosID);
                params.put("password", password);
                submitPostData(params, encode);
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
                intent.setAction("com.android.wizard.FINISH");
                startActivity(intent);
            }
        });

        this.mRegister.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.android.wizard.REGISTER");
                // startActivityForResult(intent,RG_REQUEST);
                startActivity(intent);
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
                                              "http://dev.openthos.org/?q=check/userinfo");
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
                    if(response == HttpURLConnection.HTTP_OK) {
                        InputStream inptStream = httpURLConnection.getInputStream();
                        data = dealResponseResult(inptStream);
                    }
                    Message msg = Message.obtain();
                    msg.what = response;
                    Bundle bundle = new Bundle();
                    bundle.putString("result",data);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RG_REQUEST) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(OpenthosIDSetupActivity.this,
                               getText(R.string.toast_openthos_register_cancel),
                               Toast.LENGTH_SHORT).show();
            }
        }
    }
}
