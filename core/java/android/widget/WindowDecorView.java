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
    private static final int DIALOG_HAS_HEADER_MIN_HEIGHT = 300;

    private Window mWindow;

    private View mDialogView = null;
    private Window mDialogParentWindow = null;
    private View mDialogParentDecor = null;

    private boolean mMayMSOfficeFirstSkipView = false;

    public WindowDecorView(Context context) {
        super(context);
    }

    public void setMayMSOfficeFirstSkipViewDecor(boolean mayMSOfficeFirstSkipView) {
        mMayMSOfficeFirstSkipView = mayMSOfficeFirstSkipView;
    }

    public boolean isMSOfficeFirstSkipView() {
        return mMayMSOfficeFirstSkipView && canMoveDialog();
    }

    public void setWindow(Window window) {
        mWindow = window;
    }

    public void setShadow(boolean hasShadow) {
        if (mWindow != null) {
            mWindow.setShadow(hasShadow);
        }
    }

    public boolean isMWWindow() {
        return mWindow.isMWWindow();
    }

    public void setFromDialog(View dialogView, Window dialogParentWindow) {
        mDialogView = dialogView;
        mDialogParentWindow = dialogParentWindow;
        mDialogParentDecor = (mDialogParentWindow != null) ? mDialogParentWindow.getDecorView()
                             : null;
    }

    public boolean needDialogHeader() {
        if ((mDialogView == null) || (mDialogParentWindow == null)) {
            return false;
        }
        int h = mDialogView.getHeight();
        return (h > DIALOG_HAS_HEADER_MIN_HEIGHT) && (mDialogParentDecor.getHeight() <= h);
    }

    public boolean canMoveDialog() {
        if ((mDialogView == null) || (mDialogParentWindow == null)) {
            return false;
        }
        return needDialogHeader() && (mDialogParentDecor.getWidth() <= mDialogView.getWidth());
    }

    public boolean isDialogFromMWParent() {
        return (mDialogView != null) && (mDialogParentWindow != null)
               && mDialogParentWindow.isMWWindow();
    }

    public View getParentDecor() {
        return mDialogParentDecor;
    }

    public int getDialogLeftOffset() {
        return (mDialogParentDecor.getWidth() - mDialogView.getWidth()) / 2;
    }

    public int getDialogTopOffset() {
        return (mDialogParentDecor.getHeight() - mDialogView.getHeight()) / 2;
    }
}
