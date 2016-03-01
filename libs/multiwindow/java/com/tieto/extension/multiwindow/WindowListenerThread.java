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
import java.util.List;
import java.util.Vector;

/*
 * WindowListenerThread
 * Temporary solution for checking windows from MultiwindowManager.
 * Thread should be called from methods in ActivityManagerService in further version.
 */
public class WindowListenerThread extends Thread {
    private MultiwindowManager mMultiwindow;
    private OnWindowChangeListener mOnAppListener;
    private Vector<Window> mActualWindows;
    private final int REFRESH_TIME = 500;
    private final boolean DEBUG = false;

    public WindowListenerThread(MultiwindowManager mw) {
        mMultiwindow = mw;
        mActualWindows = mw.getAllWindows();
    }

    @Override
    public void run() {
        while(!isInterrupted()) {
            Vector<Window> refreshWindows = mMultiwindow.getAllWindows();
            if (checkForNewWindows(mActualWindows, refreshWindows)) {
                for (Window window : refreshWindows) {
                    if (!mActualWindows.contains(window)) {
                        if (DEBUG) {
                            Log.d(MultiwindowManager.TAG, window.getPackage());
                        }
                        mOnAppListener.onWindowAdd(window);
                    }
                }
                for (Window window : mActualWindows) {
                    if (!refreshWindows.contains(window)) {
                        if (DEBUG) {
                            Log.d(MultiwindowManager.TAG, window.getPackage());
                        }
                        mOnAppListener.onWindowRemoved(window);
                    }
                }
                mActualWindows = refreshWindows;
            }
            try {
                Thread.sleep(REFRESH_TIME);
            } catch(InterruptedException e) {
                Log.d(MultiwindowManager.TAG, "Application Listener thread dead", e);
                interrupt();
            }
        }
    }

    public void setOnWindowChangeListener(OnWindowChangeListener listener) {
        mOnAppListener = listener;
        if (!isAlive()) {
            start();
        }
    }

    private boolean checkForNewWindows(Vector<Window> actualWindows, Vector<Window> newWindows) {
        if (actualWindows.size() != newWindows.size()) {
            return true;
        }

        for (Window window : actualWindows) {
            if (!(newWindows.contains(window))) {
                return true;
            }
        }
        return false;
    }
}
