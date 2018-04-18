package com.otosoft.setupwizard;

import android.app.Service;
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
    private static final String APP_PATH = "/data/vendor/app";
    private static final String KEY_SPACE = " ";
    private static final String KEY_SPRIT = "/";
    private static final int INDEX_WALLPAPER = 0;
    private static final int INDEX_WIFI = 1;
    private static final int INDEX_EMAIL = 2;
    private static final int INDEX_APPDATA = 3;
    private static final int INDEX_STARTUPMENU = 4;
    private static final int INDEX_BROWSER = 5;
    private static final int INDEX_APP = 6;
    private TextView mInitializedProgress;
    private ArrayList<String> mAppNames = new ArrayList();
    private ArrayList<String> mApkPaths = new ArrayList();
    private int mTotalApks;
    private ISeafileService iSeafileService;


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
            for (int i = 0; i < mTotalApks; i++) {
                String pkgName = files[i].getName();
                File[] apks = files[i].listFiles();
                for (File apk : apks) {
                    String appName = getAppName(apk.getAbsolutePath());
                    if (TextUtils.isEmpty(appName)) {
                        continue;
                    }
                    mAppNames.add(appName);
                    mApkPaths.add(apk.getAbsolutePath());
                }
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
        try {
            iSeafileService.unsetBinder(mSeafileBinder);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.setAction("com.android.wizard.STARTUSE");
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
    private IBinder mSeafileBinder = new SeafileBinder();

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
            iSeafileService = ((SetupWizardApplication) getApplication()).mISeafileService;
            try {
                iSeafileService.setBinder(mSeafileBinder);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                if (!TextUtils.isEmpty(iSeafileService.getUserName())) {
                    showSyncOptionDialog();
                } else {
                    startActivity();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                startActivity();
            }
        }
    }

    private void showSyncOptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] items = new String[]{getString(R.string.multichoice_item_wallpaper),
                getString(R.string.multichoice_item_wifi),
                getString(R.string.multichoice_item_email),
                getString(R.string.multichoice_item_appdata),
                getString(R.string.multichoice_item_startupmenu),
                getString(R.string.multichoice_item_browser),
                getString(R.string.multichoice_item_app)};
        final boolean[] selectedItems = new boolean[]{
                false, false, false, false, false, false, false};
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        Uri uri = Uri.parse("https://");
        intent.setData(uri);
        final List<ResolveInfo> browsers = getPackageManager().queryIntentActivities(
                intent, PackageManager.GET_INTENT_FILTERS);
        final List<String> syncBrowsers = new ArrayList();
        builder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selectedItems[which] = !selectedItems[which];
                if (which == INDEX_BROWSER && isChecked) {
                    final String[] browsersName = new String[browsers.size()];
                    final boolean[] browsersSelect = new boolean[browsers.size()];
                    for (int i = 0; i < browsers.size(); i++) {
                        syncBrowsers.add(browsers.get(i).activityInfo.packageName);
                        browsersName[i] = browsers.get(i).loadLabel(getPackageManager()).toString();
                        browsersSelect[i] = true;
                    }
                    showSyncBrowserDialog(browsersName, browsersSelect);
                }
            }
        });

        builder.setPositiveButton(getString(R.string.multichoice_button_restore),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try {
                            List<String> browsers = new ArrayList();
                            browsers.add("OtoSetupWizard");
                            iSeafileService.restoreSettings(selectedItems[INDEX_WALLPAPER],
                                    selectedItems[INDEX_WIFI], selectedItems[INDEX_EMAIL],
                                    selectedItems[INDEX_APPDATA], selectedItems[INDEX_STARTUPMENU],
                                    selectedItems[INDEX_BROWSER], syncBrowsers, selectedItems[INDEX_APP]);
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

    private void showSyncBrowserDialog(String[] browsersName, boolean[] browsersSelect) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMultiChoiceItems(browsersName, browsersSelect, null);
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
        File f = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "wallpaper");
        if (!f.exists()) {
            f.mkdirs();
            new Thread() {
                public void run() {
                    unzipWallpapers();
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

    private class SeafileBinder extends Binder {
        
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == iSeafileService.getCodeSendOut()) {
                mInitializedProgress.setText(data.readString());
                reply.writeNoException();
                return true;
            } else if (code == iSeafileService.getCodeRestoreFinish()) {
                startActivity();
                reply.writeNoException();
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }
}
