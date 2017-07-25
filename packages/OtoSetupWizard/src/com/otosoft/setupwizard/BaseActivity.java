package com.otosoft.setupwizard;

import android.app.Activity;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

public class BaseActivity extends Activity implements OnPreDrawListener {
    public static final String PRE_INSTALL_CACHE = "pre_install_cache";
    public static final String INSTALLED_FINISH = "installed_finish";

    public void onStart() {
        super.onStart();
        getWindow().getDecorView().setSystemUiVisibility(5638);
        //hide the status bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(5638);
        getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(this);
    }

    public void onPause() {
        super.onPause();
        getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
    }

    public boolean onPreDraw() {
        getWindow().getDecorView().setSystemUiVisibility(5638);
        return true;
    }
}
