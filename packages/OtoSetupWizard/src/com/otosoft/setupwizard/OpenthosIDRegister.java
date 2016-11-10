package com.otosoft.setupwizard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

//Rewrite Register function
public class OpenthosIDRegister extends BaseActivity {
    private TextView mPrev;
    private TextView mRegister;
    private EditText mEditTextOpenthosID;
    private EditText mEditTextPassword;
    private EditText mEditTextAgainPassword;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_register);
        //back
        mPrev = (TextView) findViewById(R.id.tv_prev);
        //register
        mRegister = (TextView) findViewById(R.id.tv_register);
        //input user name
        mEditTextOpenthosID = (EditText) findViewById(R.id.edittext_openthos_id);
        //input password
        mEditTextPassword = (EditText) findViewById(R.id.edittext_openthos_password);
        //again password
        mEditTextAgainPassword = (EditText) findViewById(R.id.enter_password_again);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HttpURLConnection.HTTP_OK:
                        Bundle b = msg.getData();
                        String result = b.getString("result");
                        String code = result.split(":")[1].split("\"")[1].trim();
                        //name wrong
                        if(CODE_WRONG_USERNAME.equals(code)) {
                            Toast.makeText(OpenthosIDRegister.this,
                                    getText(R.string.toast_openthos_id_invalid),
                                    Toast.LENGTH_SHORT).show();
                        } else if (CODE_WRONG_PASSWORD.equals(code)) {
                            Toast.makeText(OpenthosIDRegister.this,
                                    getText(R.string.toast_openthos_password_wrong),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent();
                            intent.setAction("com.android.wizard.REGISTER");
                            startActivity(intent);
                        }
                        break;
                    default:
                        Toast.makeText(OpenthosIDRegister.this,
                                getText(R.string.toast_openthos_register_password_wrong),
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
                OpenthosIDRegister.this.onBackPressed();
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
                        if (response == HttpURLConnection.HTTP_OK) {
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
}
