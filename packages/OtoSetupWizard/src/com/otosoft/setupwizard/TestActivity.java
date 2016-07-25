package com.otosoft.setupwizard;

import android.app.Activity;
import android.os.Bundle;

public class TestActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((SetupWizardApplication) getApplication()).runForTest(this);
    }
}
