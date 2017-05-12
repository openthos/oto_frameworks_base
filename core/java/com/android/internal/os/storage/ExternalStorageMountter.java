package com.android.internal.os.storage;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.internal.R;

/**
 * Takes care of unmounting and formatting external storage.
 */
public class ExternalStorageMountter extends Service implements DialogInterface.OnCancelListener {
    static final String TAG = "ExternalStorageMountter";

    public static final String UMOUNT_ONLY = "com.android.internal.os.storage.UMOUNT_ONLY";

    private StorageVolume mStorageVolume;

    public static final ComponentName COMPONENT_NAME
            = new ComponentName("android", ExternalStorageMountter.class.getName());

    // Access using getMountService()
    private IMountService mMountService = null;

    private StorageManager mStorageManager = null;

    private PowerManager.WakeLock mWakeLock;

    private ProgressDialog mProgressDialog = null;

    StorageEventListener mStorageListener = new StorageEventListener() {

        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i(TAG, "Received storage state changed notification that " +
                    path + " changed state from " + oldState +
                    " to " + newState);
            updateProgressState();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            mStorageManager.registerListener(mStorageListener);
        }
        mWakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ExternalStorageMountter");
        mWakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (UMOUNT_ONLY.equals(intent.getAction())) {
            mStorageVolume = intent.getParcelableExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(true);
                mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                updateProgressState();
                mProgressDialog.show();
            }
        }
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (mStorageManager != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mWakeLock.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        IMountService mountService = getMountService();
        String extStoragePath = mStorageVolume == null ?
                Environment.getLegacyExternalStorageDirectory().toString() :
                mStorageVolume.getPath();
        try {
            mountService.mountVolume(extStoragePath);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed talking with mount service", e);
        }
        stopSelf();
    }

    void updateProgressState() {
        String status = mStorageVolume == null ?
                Environment.getExternalStorageState() :
                mStorageManager.getVolumeState(mStorageVolume.getPath());
        if (Environment.MEDIA_MOUNTED.equals(status)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(status)) {
            updateProgressDialog(R.string.progress_unmounting);
            IMountService mountService = getMountService();
            final String extStoragePath = mStorageVolume == null ?
                    Environment.getLegacyExternalStorageDirectory().toString() :
                    mStorageVolume.getPath();
            try {
                // Remove encryption mapping if this is an unmount for a factory reset.
                mountService.unmountVolume(extStoragePath, true, false);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed talking with mount service", e);
            }
        } else {
            stopSelf();
        }
    }

    void fail(int msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        stopSelf();
    }

    public void updateProgressDialog(int msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mProgressDialog.show();
        }

        mProgressDialog.setMessage(getText(msg));
    }

    IMountService getMountService() {
        if (mMountService == null) {
            IBinder service = ServiceManager.getService("mount");
            if (service != null) {
                mMountService = IMountService.Stub.asInterface(service);
            } else {
                Log.e(TAG, "Can't get mount service");
            }
        }
        return mMountService;
    }
}
