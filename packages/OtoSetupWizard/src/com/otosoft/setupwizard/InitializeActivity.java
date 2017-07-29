package com.otosoft.setupwizard;

import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import java.io.File;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InitializeActivity extends BaseActivity {

    private static final String APP_PATH = "/data/vendor/app";
    private static final String KEY_SPACE = " ";
    private static final String KEY_SPRIT = "/";
    private LinearLayout mLinearLayout;
    private TextView mInitializedProgress;
    private TextView mInitializedNext;
    private ArrayList<String> mAppNames;
    private ArrayList<String> mApkPaths;
    private InstallAsyncTask mInstallTask;
    private SharedPreferences mSp;
    private int mTotalApks;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize_pager);
        mInitializedProgress = (TextView) findViewById(R.id.initialize_progress);
        mInitializedNext = (TextView) findViewById(R.id.tv_next);
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_next);
        mSp = getSharedPreferences(PRE_INSTALL_CACHE, Context.MODE_PRIVATE);
        initializeApp();
        mInstallTask = new InstallAsyncTask();
        if (!new File("/sdcard/Pictures/wallpaper").exists()) {
            new File("/sdcard/Pictures/wallpaper").mkdirs();
            new Thread(){
                public void run(){
                    setWallpaper();
                }
            }.start();
        }
    }

    public void onResume() {
        if (mSp.getBoolean(INSTALLED_FINISH, false)) {
            mInitializedProgress.setText(getText(R.string.initialize_finish));
            mLinearLayout.setVisibility(View.VISIBLE);
            mInitializedNext.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity();
                }
            });
        } else {
            mInstallTask.execute();
        }
        super.onResume();
    }

    private void initializeApp() {
        mAppNames = new ArrayList();
        mApkPaths = new ArrayList();
        File file = new File(APP_PATH);
        File[] files = file.listFiles();
        mTotalApks = files.length;
        try {
        for (int i = 0; i < mTotalApks; i++) {
            String pkgName = files[i].getName();
            File[] apks = files[i].listFiles();
            for (File apk : apks) {
                mApkPaths.add(apk.getAbsolutePath());
                getAppName(apk.getAbsolutePath());
            }
        }
        } catch(Exception e){}
    }

    private void getAppName(String path) {
        File sourceFile = new File(path);
        final PackageParser parser = new PackageParser();
        try {
            PackageParser.Package pkg = parser.parseMonolithicPackage(sourceFile, 0);
            parser.collectManifestDigest(pkg);
            PackageInfo info = PackageParser.generatePackageInfo(pkg, null,
                     PackageManager.GET_PERMISSIONS, 0, 0, null,
                     new PackageUserState());
            Resources pRes = getResources();
            AssetManager assmgr = new AssetManager();
            assmgr.addAssetPath(path);
            Resources res = new Resources(assmgr,
                                 pRes.getDisplayMetrics(), pRes.getConfiguration());
            CharSequence label = null;
            if (info.applicationInfo.labelRes != 0) {
                label = res.getText(info.applicationInfo.labelRes);
            }
            if (label == null) {
                label = (info.applicationInfo.nonLocalizedLabel != null) ?
                      info.applicationInfo.nonLocalizedLabel : info.applicationInfo.packageName;
            }
            mAppNames.add(label.toString());
        } catch (Exception e) {
        }
    }

    private void installSlient(String apkPath) {
        String cmd = "pm install -r " + apkPath;
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startActivity() {
        Intent intent = new Intent();
        intent.setAction("com.android.wizard.STARTUSE");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (!mSp.getBoolean(INSTALLED_FINISH, false)) {
            return;
        }
        super.onBackPressed();
    }

    public class InstallAsyncTask extends AsyncTask<Void, Object, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < mTotalApks; i++ ) {
                Object[] result = new Object[2];
                result[0] = i + 1;
                result[1] = mAppNames.get(i);
                publishProgress(result);
                installSlient(mApkPaths.get(i));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            int index = (int)values[0];
            String appName = (String)values[1];
            mInitializedProgress.setText(getText(R.string.initialize_progress)
                          + KEY_SPACE + appName + KEY_SPACE + index + KEY_SPRIT + mTotalApks);
        }

        @Override
        protected void onPostExecute(Void avoid) {
            mSp.edit().putBoolean(INSTALLED_FINISH, true).commit();
            startActivity();
        }
    }

    private void setWallpaper() {
        String outputDirectory = "/sdcard/Pictures";
        File file = new File(outputDirectory);
        try {
            InputStream inputStream = getAssets().open("wallpaper.zip");
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry entry = zipInputStream.getNextEntry();
            byte[] buffer = new byte[1024 * 1024];
            int count = 0;
            while (entry != null) {
                if (entry.isDirectory()) {
                    file = new File(outputDirectory + File.separator + entry.getName());
                    file.mkdir();
                } else {
                    file = new File(outputDirectory + File.separator + entry.getName());
                    file.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(file);
                    while ((count = zipInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, count);
                    }
                    outputStream.close();
                }
                entry = zipInputStream.getNextEntry();
            }
            zipInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
