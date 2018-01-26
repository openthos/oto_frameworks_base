package com.otosoft.setupwizard;

import android.os.Bundle;
import android.os.AsyncTask;
import android.os.IBinder;
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
import android.content.ComponentName;
import android.view.View;
import android.view.View.OnClickListener;
import java.io.File;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
import java.util.Map;
import java.util.HashMap;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.RemoteException;
import com.openthos.seafile.ISeafileService;

public class InitializeActivity extends BaseActivity {
    private static final String APP_PATH = "/data/vendor/app";
    private static final String APP_DOWNLOAD_PATH = "/storage/emulated/legacy/Download/app";
    private static final String KEY_SPACE = " ";
    private static final String KEY_SPRIT = "/";
    private static final int INDEX_WALLPAPER = 0;
    private static final int INDEX_WIFI = 1;
    private static final int INDEX_EMAIL = 2;
    private static final int INDEX_APPDATA = 3;
    private static final int INDEX_STARTUPMENU = 4;
    private static final int INDEX_BROWSER = 5;
    private static final int INDEX_APP = 6;
    private LinearLayout mLinearLayout;
    private TextView mInitializedProgress;
    private TextView mInitializedNext;
    private ArrayList<String> mAppNames;
    private ArrayList<String> mApkPaths;
    private InstallAsyncTask mInstallTask;
    private SharedPreferences mSp;
    private int mTotalApks, mDownloadApks, mTotal;
    private static InitializeActivity mInitializeActivity;
    private Map<String, String> mAppStoreAppMap;
    private boolean mIsAppStoreApps;
    private ISeafileService iSeafileService;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppStoreAppMap = new HashMap<>();
        mInitializeActivity = this;
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

    public static InitializeActivity getInitializeActivity() {
        return mInitializeActivity;
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

    private void initializeAppStoreApp() {
        mAppNames = new ArrayList();
        mApkPaths = new ArrayList();
        File file = new File(APP_DOWNLOAD_PATH);
        File[] files = file.listFiles();
        mTotalApks = files.length;
        try {
            for (int i = 0; i < mTotalApks; i++) {
                String name = files[i].getName();
                if (name.endsWith(".apk")) {
                    mApkPaths.add(files[i].getAbsolutePath());
                     getAppName(files[i].getAbsolutePath());
                }
            }
            mTotalApks = mApkPaths.size();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void addAppStoreAppInfoToMap(
            boolean hasData, String appFileName, String appName, int total) {
        if (hasData) {
            mTotal = total;
            mDownloadApks++;
            mAppStoreAppMap.put(appFileName, appName);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                mInitializedProgress.setText(getText(R.string.appstore_download_app)
                        + " : " + mDownloadApks + "/" + mTotal);
                }
            });
        } else {
            startActivity();
        }
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
        String cmd;
        if (apkPath.endsWith(".apk")) {
            cmd = "pm install " + apkPath;
        } else {
            cmd = "pm install -r " + apkPath;
        }
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

    public void InstallAppStoreApps() {
        initializeAppStoreApp();
        mInstallTask = new InstallAsyncTask();
        mInstallTask.execute();
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
            if (mInitializedProgress.getVisibility() == View.GONE) {
                mInitializedProgress.setVisibility(View.VISIBLE);
            }
            int index = (int)values[0];
            String appName = (String)values[1];
            if (!mIsAppStoreApps) {
                mInitializedProgress.setText(getText(R.string.initialize_progress)
                              + KEY_SPACE + appName + KEY_SPACE + index + KEY_SPRIT + mTotalApks);
            } else {
                mInitializedProgress.setText(getText(R.string.restore_progress)
                              + KEY_SPACE + appName + KEY_SPACE + index + KEY_SPRIT + mTotalApks);
            }
        }

        @Override
        protected void onPostExecute(Void avoid) {
            mSp.edit().putBoolean(INSTALLED_FINISH, true).commit();
            if (!mIsAppStoreApps) {
                // check cloudservice account
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.openthos.seafile",
                        "com.openthos.seafile.SeafileService"));
                intent.setAction("com.openthos.seafile.Seafile_Service");
                bindService(intent, new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        iSeafileService = ISeafileService.Stub.asInterface(service);
                        try {
                            if (iSeafileService.getUserName() != null) {
                                mIsAppStoreApps = true;
                                showSyncOptionDialog();
                            } else {
                                startActivity();
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            startActivity();
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                    }
                }, BIND_AUTO_CREATE);
            } else {
                startActivity();
            }
        }
    }

    private void showSyncOptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InitializeActivity.this);
        final String[] items = new String[] {getString(R.string.multichoice_item_wallpaper),
                getString(R.string.multichoice_item_wifi),
                getString(R.string.multichoice_item_email),
                getString(R.string.multichoice_item_appdata),
                getString(R.string.multichoice_item_startupmenu),
                getString(R.string.multichoice_item_browser),
                getString(R.string.multichoice_item_app)};
        final boolean[] selectedItems = new boolean[] {
                false, false, false, false, false, false, false};
        builder.setMultiChoiceItems(items,null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selectedItems[which] = !selectedItems[which];
            }
        });

        builder.setPositiveButton(getString(R.string.multichoice_button_restore),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try {
                    iSeafileService.restoreSettings(selectedItems[INDEX_WALLPAPER],
                            selectedItems[INDEX_WIFI], selectedItems[INDEX_EMAIL],
                            selectedItems[INDEX_APPDATA], selectedItems[INDEX_STARTUPMENU],
                            selectedItems[INDEX_BROWSER], selectedItems[INDEX_APP]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        mInitializedProgress.setText(getText(R.string.appstore_download_app));
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                    startActivity();
                }
            }
        });

        builder.setNegativeButton(getString(R.string.multichoice_button_cancel),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
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
