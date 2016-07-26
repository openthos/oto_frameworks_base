/*
 * Copyright (C) 2006 The Android Open Source Project
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

package android.widget;

import android.content.Context;
import android.view.Window;
import android.view.View;
import android.util.Log;

public class WindowDecorView extends FrameLayout {

    private static final String TAG = "WindowDecorView";

    Window mWindow;
    View mDialogView = null;
    Window mDialogParentWindow = null;

    public WindowDecorView(Context context) {
        super(context);
    }

    public void setWindow(Window window) {
        mWindow = window;
    }

    public boolean isMWWindow() {
        return mWindow.isMWPanel();
    }

    public void setFromDialog(View dialogView, Window dialogParentWindow) {
        mDialogView = dialogView;
        mDialogParentWindow = dialogParentWindow;
    }

    public boolean needHeader() {
        return (mDialogView == null) || (mDialogParentWindow == null)
               || ((mDialogParentWindow.getDecorView().getWidth() <= mDialogView.getWidth())
                   && (mDialogParentWindow.getDecorView().getHeight() <= mDialogView.getHeight()));
    }

    public boolean isDialogFromMWParent() {
        return (mDialogView != null) && (mDialogParentWindow != null)
               && mDialogParentWindow.isMWPanel();
    }

    public int getDialogLeftOffset() {
        return (mDialogParentWindow.getDecorView().getWidth() - mDialogView.getWidth()) / 2;
    }

    public int getDialogTopOffset() {
        return (mDialogParentWindow.getDecorView().getHeight() - mDialogView.getHeight()) / 2;
    }
}
