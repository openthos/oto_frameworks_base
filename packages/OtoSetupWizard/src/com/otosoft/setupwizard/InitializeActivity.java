package com.otosoft.setupwizard;

import android.app.Service;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;

import android.os.Environment;
import android.os.RemoteException;

import org.openthos.seafile.ISeafileService;

public class InitializeActivity extends BaseActivity {
    private static final String APP_PATH = "/system/preinstall";
    private static final String KEY_SPACE = " ";
    private static final String KEY_SPRIT = "/";
    private static final int INDEX_WALLPAPER = 0;
    private static final int INDEX_WIFI = 1;
    private static final int INDEX_APPDATA = 2;
    private static final int INDEX_STARTUPMENU = 3;
    private static final int INDEX_BROWSER = 4;
    private static final int INDEX_APP = 5;
    private TextView mInitializedProgress;
    private ArrayList<String> mAppNames = new ArrayList();
    private ArrayList<String> mApkPaths = new ArrayList();
    private int mTotalApks;
    private ISeafileService iSeafileService;
    private ArrayList<String> files = new ArrayList<>();
    private ArrayList<String> types = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize_pager);
        mInitializedProgress = (TextView) findViewById(R.id.initialize_progress);
        new InstallAsyncTask().execute();
    }

    private void initializeApp() {
        mAppNames.clear();
        mApkPaths.clear();
        File file = new File(APP_PATH);
        if (file.exists()){
            File[] files = file.listFiles();
            mTotalApks = files.length;
            for (File apk : files) {
                String appName = getAppName(apk.getAbsolutePath());
                if (TextUtils.isEmpty(appName)) {
                    continue;
                }
                mAppNames.add(appName);
                mApkPaths.add(apk.getAbsolutePath());
            }
        }
    }

    private final PackageParser parser = new PackageParser();

    private String getAppName(String path) {
        File sourceFile = new File(path);
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
            return label.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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
        intent.setAction("com.android.wizard.FINISH");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }

    public class InstallAsyncTask extends AsyncTask<Void, Object, Void> {
        private static final int CURRENT_INDEX = 0;
        private static final int APPNAME = 1;


        @Override
        protected Void doInBackground(Void... params) {
            initializeApp();
            for (int i = 0; i < mTotalApks; i++) {
                Object[] result = new Object[2];
                result[CURRENT_INDEX] = i + 1;
                result[APPNAME] = mAppNames.get(i);
                publishProgress(result);
                installSlient(mApkPaths.get(i));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            int index = (int) values[CURRENT_INDEX];
            String appName = (String) values[APPNAME];
            mInitializedProgress.setText(getText(R.string.initialize_progress)
                    + KEY_SPACE + appName + KEY_SPACE + index + KEY_SPRIT + mTotalApks);
        }

        @Override
        protected void onPostExecute(Void avoid) {
            new WallpaperAsyncTask().execute();
        }
    }

    public class WallpaperAsyncTask extends AsyncTask<Void, Object, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Object[] result = new Object[2];
            result[0] = getString(R.string.init_wallpaper);
            publishProgress(result);
            initWallpaper();
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            mInitializedProgress.setText((String) values[0]);
        }

        @Override
        protected void onPostExecute(Void avoid) {
        }
    }

    private void showSyncAppDialog(List<ResolveInfo> localList, List<String> syncList) {
        String[] names = new String[localList.size()];
        final boolean[] selects = new boolean[localList.size()];
        int i = 0;
        for (ResolveInfo info : localList) {
            syncList.add(info.activityInfo.packageName);
            names[i] = info.loadLabel(getPackageManager()).toString();
            selects[i] = true;
            i++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMultiChoiceItems(names, selects,
                new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selects[which] = !selects[which];
            }
        });
        builder.setPositiveButton(getString(R.string.warning_dialog_ok),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    private void initWallpaper() {
        final File f = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "wallpaper");
        if (!f.exists()) {
            f.mkdirs();
            new Thread() {
                public void run() {
                    unzipWallpapers();
                    MediaScannerConnection.scanFile(InitializeActivity.this,
                            files.toArray(new String[files.size()]),
                            types.toArray(new String[types.size()]),
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    startActivity();
                                }
                            });
                }
            }.start();
        }
    }

    private void unzipWallpapers() {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String outputDirectory = file.getAbsolutePath();
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
                    files.add(file.getAbsolutePath());
                    types.add("image/*");
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
