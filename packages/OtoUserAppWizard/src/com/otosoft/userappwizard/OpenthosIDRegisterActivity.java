package com.otosoft.userappwizard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class OpenthosIDRegisterActivity extends BaseActivity {
    private Button mButtonFinish;
    private Button mButtonCancel;
    private WebView mWebView;
    private  final String URL="http://dev.openthos.org/?q=user/register";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_openthos_id_register);

        this.mWebView = (WebView) findViewById(R.id.webview_register);
        this.mButtonFinish = (Button) findViewById(R.id.button_register_finish);
        this.mButtonCancel = (Button) findViewById(R.id.button_cancel);

        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(URL);

        //mWebView.setWebViewClient(new WebViewClient(){
          //  @Override
            //public boolean shouldOverrideUrlLoading(WebView view, String url) {
              //  view.loadUrl(url);
                //return true;
       //     }
       // });

        this.mButtonFinish.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("opethosid","xxxx@xx.com");
                Intent mIntent = new Intent();
                mIntent.putExtras(bundle);
                setResult(RESULT_OK, mIntent);
                finish();
            }
        });
        this.mButtonCancel.setOnClickListener(new OnClickListener() {
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
