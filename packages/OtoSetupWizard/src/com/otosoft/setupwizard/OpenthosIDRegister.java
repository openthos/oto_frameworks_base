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

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

//Rewrite Register function
public class OpenthosIDRegister extends BaseActivity {
    private TextView mPrev;
    private TextView mRegister;
    private EditText mEditTextOpenthosID;
    private EditText mEditTextPassword;
    private EditText mEditTextAgainPassword;
    private String openthosID;
    private String password;
    private String againpassword;
    private int result;
    private Handler mHandler;
    private final Map<String,String> params = new HashMap<String,String>();
    private final String encode = "utf-8";
    static final int RG_REQUEST = 0;
    private ContentResolver mResolver;
    private String mCookie = "";
    public static final int MSG_GET_CSRF = 0x1001;
    public static final int MSG_GET_CSRF_OK = 0x1002;
    public static final int MSG_REGIST_SEAFILE = 0x1003;
    public static final int MSG_REGIST_SEAFILE_OK = 0x1004;

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

        mResolver = getContentResolver();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HttpURLConnection.HTTP_OK:
                        Bundle b = msg.getData();
                        String result = b.getString("result");
                        if (result != null
                              && result.equals(getText(R.string.toast_register_successful))) {
                            Toast.makeText(OpenthosIDRegister.this,result,
                                    Toast.LENGTH_SHORT).show();
                            mHandler.sendEmptyMessage(MSG_GET_CSRF);
                            Uri uriInsert =
                                  Uri.parse("content://com.otosoft.tools.myprovider/openthosID");
                            ContentValues values = new ContentValues();
                            values.put("openthosID", openthosID);
                            values.put("password", password);
                            mResolver.insert(uriInsert, values);
                        } else {
                            Toast.makeText(OpenthosIDRegister.this,result,
                                Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MSG_GET_CSRF:
                        getCsrf();
                        break;
                    case MSG_GET_CSRF_OK:
                        mCookie = (String) msg.obj;
                        mHandler.sendEmptyMessage(MSG_REGIST_SEAFILE);
                        break;
                    case MSG_REGIST_SEAFILE:
                        registSeafile();
                        break;
                    case MSG_REGIST_SEAFILE_OK:
                        OpenthosIDRegister.this.onBackPressed();
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
                openthosID = mEditTextOpenthosID.getText().toString().trim();
                password = mEditTextPassword.getText().toString().trim();
                againpassword = mEditTextAgainPassword.getText().toString().trim();
                //verify openthos id and password
                params.put("ID", openthosID);
                params.put("pwd", password);

                if (password.equals("") || againpassword.equals("")) {
                    params.put("pwd", password);
                } else {
                    if (!password.equals(againpassword)) {
                        Toast.makeText(OpenthosIDRegister.this,
                                getText(R.string.toast_openthos_register_password_not_equals),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        params.put("pwd", password);
                    }
                }
                submitPostData(params, encode);
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
                                        "http://dev.openthos.org/?q=u/register");
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

    private void getCsrf() {
        RequestThread thread = new RequestThread(mHandler,
               "https://dev.openthos.org/accounts/register/", null, RequestThread.RequestType.GET);
        thread.start();
    }

    private void registSeafile() {
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("csrfmiddlewaretoken",mCookie.split("=")[1].trim()));
        list.add(new BasicNameValuePair("email", openthosID));
        list.add(new BasicNameValuePair("password1", password));
        list.add(new BasicNameValuePair("password2", againpassword));
        RequestThread thread = new RequestThread(mHandler,
               "https://dev.openthos.org/accounts/register/", list, RequestThread.RequestType.POST,
                mCookie);
        thread.start();
    }
}
