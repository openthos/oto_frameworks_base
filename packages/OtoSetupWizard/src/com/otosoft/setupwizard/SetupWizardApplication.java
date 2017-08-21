package com.otosoft.setupwizard;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.io.IOException;
import com.android.internal.app.LocalePicker;

public class SetupWizardApplication extends Application {
    private final ComponentName REAL_HOME = new ComponentName("com.otosoft.filemanager", "com.otosoft.launcher.Launcher");
    private final Runnable mDisableSetupRunnable = new Runnable() {
        public void run() {
            SetupWizardApplication.this.getPackageManager().setComponentEnabledSetting(new ComponentName(SetupWizardApplication.this, SetupWizardActivity.class), 2, 0);
        }
    };
    private final ArrayList<Runnable> mFinishRunnables = new ArrayList();
    private final Runnable mProvisionedRunnable = new Runnable() {
        public void run() {
            Global.putInt(SetupWizardApplication.this.getContentResolver(), "device_provisioned", 1);
            Secure.putInt(SetupWizardApplication.this.getContentResolver(), "user_setup_complete", 1);
        }
    };

    @Override
    public void onCreate() {
        try {
            Runtime.getRuntime().exec(new String[]{"su","-c", "pm enable com.openthos.factorytest"});
        } catch (IOException e) {
            e.printStackTrace();
        }
        LocalePicker.updateLocale(Locale.CHINA);
        super.onCreate();
    }

    public void onSetupFinished(Activity activity) {
        Intent i = new Intent(this, SetupWizardActivity.class);
        i.setFlags(67108864);
        i.putExtra("extra_clear_top", true);
        activity.startActivity(i);
    }

    public void onSetupFinishedReally(Activity activity) {
        this.mFinishRunnables.clear();
        this.mFinishRunnables.add(this.mProvisionedRunnable);
        this.mFinishRunnables.add(this.mDisableSetupRunnable);
        Iterator i$ = this.mFinishRunnables.iterator();
        while (i$.hasNext()) {
            ((Runnable) i$.next()).run();
        }
        activity.finish();
    }

    public void runForTest(Activity activity) {
        Global.putInt(getContentResolver(), "device_provisioned", 0);
        Secure.putInt(getContentResolver(), "user_setup_complete", 0);
        PackageManager pm = getPackageManager();
        ComponentName cn = new ComponentName(this, SetupWizardActivity.class);
        pm.setComponentEnabledSetting(cn, 1, 1);
        Intent i = new Intent("android.intent.action.MAIN");
        i.setComponent(cn);
        i.addCategory("android.intent.category.HOME");
        i.setFlags(i.getFlags() | 268435456);
        startActivity(i);
        activity.finish();
    }
}
