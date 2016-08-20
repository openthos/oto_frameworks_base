package com.otosoft.userappwizard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class OpenthosIDRegisterActivity extends BaseActivity {
    private TextView mFinish;
    private TextView mCancel;
    private WebView mWebView;
    private  final String URL="http://dev.openthos.org/register";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_openthos_id_register);

        this.mWebView = (WebView) findViewById(R.id.webview_register);
        mFinish = (TextView) findViewById(R.id.tv_register_finish);
        mCancel = (TextView) findViewById(R.id.tv_cancel);

        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(URL);

        //mWebView.setWebViewClient(new WebViewClient(){
          //  @Override
            //public boolean shouldOverrideUrlLoading(WebView view, String url) {
              //  view.loadUrl(url);
                //return true;
       //     }
       // });

        mFinish.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("opethosid","xxxx@xx.com");
                Intent mIntent = new Intent();
                mIntent.putExtras(bundle);
                setResult(RESULT_OK, mIntent);
                finish();
            }
        });
        mCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Intent mIntent1 = new Intent();
                mIntent1.putExtras(bundle);
                setResult(RESULT_CANCELED, mIntent1);
                finish();
            }
        });
    }
}
