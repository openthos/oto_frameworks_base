/*
 * Copyright (C) 2014 Tieto Poland Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tieto.extension.multiwindow;

import android.os.RemoteException;
import android.util.Log;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
//import android.app.ActivityManager.StackBoxInfo;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import java.util.List;
import java.util.Vector;

public class MultiwindowManager {

    public static String TAG = "MultiwindowManager";

    private IActivityManager mService = null;
    private Context mContext = null;
    private OnWindowChangeListener mOnAppListener;
    private static WindowListenerThread sWindowListener =  null;
    private static String sPermissionName = "android.permission.MULTIWINDOW";

    public MultiwindowManager(Context ctx) {
        mService = ActivityManagerNative.getDefault();
        mContext = ctx;
        checkPermission();
    }

    public void setOnWindowChangeListener(OnWindowChangeListener listener) {
        if(sWindowListener != null){
            sWindowListener.interrupt();
        }
        sWindowListener = new WindowListenerThread(this);
        sWindowListener.setOnWindowChangeListener(listener);
    }

    public void startActivity(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_RUN_IN_WINDOW);
        mContext.startActivity(intent);
    }

    public boolean relayoutWindow(int stackId, Rect windowFrame) {
        try {
            return mService.relayoutWindow(stackId, windowFrame);
        } catch (RemoteException e) {
            Log.e(TAG, "relayoutWindow failed ", e);
        }
        return false;
    }

    /**
     * Method for closing application by stackbox id, it close tasks by their ids
     * from stackboxes.
     *
     * @param stackId Argument for id of application stack.
     * @return true if close succeed, false if not.
     */
    public boolean closeApplication(int stackId) {
        try {
            return mService.closeActivity(stackId);
        } catch (RemoteException e) {
            Log.e(TAG, "closeApplication failed ", e);
        }
        return false;
    }

    public Vector<Window> getAllWindows() {
        Vector<Window> ret = new Vector<Window>();
        //List<StackBoxInfo> list;
        //String pkg;
        //try {
        //    list = mService.getStackBoxes();
        //} catch (RemoteException e) {
        //    Log.e(TAG, "getAllWindows failes", e);
        //    return ret;
        //}
        //for (StackBoxInfo sb : list) {
        //    if (sb.floating) {
                /*
                 * Split in pkg is nessesary, we need only name of the app without activity name.
                 * First we need to split StackInfo message
                 */
        //        pkg = sb.stack.toString().split(" ")[5].split("/")[0];
        //        ret.add(new Window(sb.bounds,pkg, sb.stackId));
        //    }
        //}
        return ret;
    }

    /**
     * Method for setting custom size of maximized window of Application window
     * by pressing a maximize button. This method is used when you want different
     * window size on mazimize than normal.
     *
     * @param fullScreen takes size of window in fullscreen.
     * @return true if method succeed, false if not.
     */
    //public boolean setMaximizedWindowSize(Rect size) {
    //    try {
    //        mService.setMaximizedWindowSize(size);
    //        return true;
    //    } catch (RemoteException e) {
    //        Log.e(TAG, "setFullScreenRect failed ", e);
    //        return false;
    //    }
    //}

    public void checkPermission(){
        PackageManager pm = mContext.getPackageManager();
        String packageName = mContext.getPackageName();
        int havePermission = pm.checkPermission(sPermissionName, packageName);

        if (havePermission != pm.PERMISSION_GRANTED) {
            throw new SecurityException("You don't have permission to use Multiwindow!");
        }
    }

}
