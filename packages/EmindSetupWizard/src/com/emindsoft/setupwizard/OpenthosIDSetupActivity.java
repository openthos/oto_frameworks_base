package com.emindsoft.setupwizard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

public class OpenthosIDSetupActivity extends BaseActivity {
    private Button mButtonVerify;
    private Button mButtonPrev;
    private EditText mEditTextOpenthosID;
    private EditText mEditTextPassword;
    private TextView mSkip;
    private TextView mRegister;
    private String openthosID;
    private String password;
    private int result;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_openthos_id_setup);

        this.mButtonPrev = (Button) findViewById(R.id.button_prev);
        this.mButtonVerify = (Button) findViewById(R.id.button_verify);
        this.mEditTextOpenthosID = (EditText) findViewById(R.id.edittext_openthos_id);
        this.mEditTextPassword = (EditText) findViewById(R.id.edittext_openthos_password);
        this.mSkip = (TextView) findViewById(R.id.text_skip);
        this.mRegister = (TextView) findViewById(R.id.text_register);

        this.mButtonVerify.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                openthosID = mEditTextOpenthosID.getText().toString().trim();
                password = mEditTextPassword.getText().toString().trim();
                //verify openthos id and password
                Map<String, String> params = new HashMap<String, String>();
                params.put("openthosID", openthosID);
                params.put("password", password);

                result = submitPostData(params, "utf-8");

                if(result == HttpURLConnection.HTTP_OK) {
                    Intent intent = new Intent();
                    intent.setAction("com.android.wizard.FINISH");
                    startActivity(intent);
                } else {
                    Toast.makeText(OpenthosIDSetupActivity.this,
                                   "you did not register or you have put wrong password",
                                   Toast.LENGTH_SHORT).show();
                }

            }
        });
        this.mButtonPrev.setOnClickListener(new OnClickListener() {
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
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://www.baidu.com");
                intent.setData(content_url);
                startActivity(intent);
            }
        });
    }

    public void onResume() {
        super.onResume();
    }

    public static int submitPostData(Map<String, String> params, String encode) {

        byte[] data = HttpUtils.getRequestData(params, encode).toString().getBytes();
        try {
            HttpURLConnection httpURLConnection =
                        (HttpURLConnection)HttpUtils.getHttpsURLConnection("http://www.baidu.com");
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            //set the request body type is text
            httpURLConnection.setRequestProperty("Content-Type",
                                                  "application/x-www-form-urlencoded");
            //set the request body length
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //get the ouput stream and write to the service
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();
//            if(response == HttpURLConnection.HTTP_OK) {
//                InputStream inptStream = httpURLConnection.getInputStream();
//                return dealResponseResult(inptStream);
//            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
