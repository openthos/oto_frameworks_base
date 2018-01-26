package com.otosoft.setupwizard;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import com.otosoft.IAppStoreCallback;
import com.otosoft.setupwizard.InitializeActivity;

public class AppStoreCallbackService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IAppStoreCallback.Stub() {
            @Override
            public void downloadCallback(boolean hasData, String appFileName, String appName,
                                         int total, boolean isDone) throws RemoteException {
                InitializeActivity initializeActivity = InitializeActivity.getInitializeActivity();
                if (initializeActivity != null) {
                    initializeActivity.addAppStoreAppInfoToMap(
                            hasData, appFileName, appName, total);
                    if (isDone) {
                        initializeActivity.InstallAppStoreApps();
                    }
                }
            }
        };
    }
}
