package com.emindsoft.setupwizard;

import android.content.Intent;
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


public class OpenthosIDSetupActivity extends BaseActivity {
    private Button mButtonVerify;
    private Button mButtonPrev;
    private EditText mEditTextOpenthosID;
    private EditText mEditTextPassword;
    private TextView mSkip;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_openthos_id_setup);

        this.mButtonPrev = (Button) findViewById(R.id.button_prev);
        this.mButtonVerify = (Button) findViewById(R.id.button_verify);
        this.mEditTextOpenthosID = (EditText) findViewById(R.id.edittext_openthos_id);
        this.mEditTextPassword = (EditText) findViewById(R.id.edittext_openthos_password);
        this.mSkip = (TextView) findViewById(R.id.text_skip);

        this.mButtonVerify.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

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
    }

    public void onResume() {
        super.onResume();
    }
}
