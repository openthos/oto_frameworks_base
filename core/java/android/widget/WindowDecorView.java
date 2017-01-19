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
import android.graphics.Rect;
import android.view.Window;
import android.view.View;
import android.util.Log;

public abstract class WindowDecorView extends FrameLayout {

    private static final String TAG = "WindowDecorView";
    private static final int DIALOG_HAS_HEADER_MIN_HEIGHT = 300;
    private static final int SCROLLVIEW_DIFF_MAX_WIDTH = 120;
    private static final int SCROLLVIEW_DIFF_MAX_HEIGHT = 500;

    private Window mWindow;

    private View mDialogView = null;
    private Window mDialogParentWindow = null;
    private View mDialogParentDecor = null;

    private View mScrollView = null;
    private int mScrollBarRangeH = 0;
    private int mScrollBarRangeV = 0;
    private int mScrollBarOffsetH = 0;
    private int mScrollBarOffsetV = 0;
    private int mScrollBarExtentH = 0;
    private int mScrollBarExtentV = 0;
    private boolean mScrollHEnabled = false;
    private boolean mScrollVEnabled = false;

    private boolean mMayMSOfficeFirstSkipView = false;

    public WindowDecorView(Context context) {
        super(context);
    }

    public void setMayMSOfficeFirstSkipViewDecor(boolean mayMSOfficeFirstSkipView) {
        mMayMSOfficeFirstSkipView = mayMSOfficeFirstSkipView;
    }

    public void setWindow(Window window) {
        mWindow = window;
    }

    public boolean isMWWindow() {
        return mWindow.isMWWindow();
    }

    public abstract void enableMultiWindowToWindowManager(Rect dialogRect);

    public abstract void syncMultiWindowToWindowManager();

    public abstract int getWindowHeaderPadding();
    public abstract int getWindowBorderPadding();

    public void setFromDialog(View dialogView, Window dialogParentWindow) {
        if ((dialogView == null) && (mDialogView != null) && (mDialogParentWindow != null)
            &&  mDialogParentWindow.isMWWindow()) {
            mDialogParentWindow.showCover(false);
        }

        mDialogView = dialogView;
        mDialogParentWindow = dialogParentWindow;
        mDialogParentDecor = (mDialogParentWindow != null) ? mDialogParentWindow.getDecorView()
                             : null;
    }

    public boolean isDialogFromMWParent() {
        return isDialogFromMWParent(false);
    }

    public boolean isDialogFromMWParent(boolean onlyFather) {
        if ((mDialogView == null) || (mDialogParentWindow == null)) {
            return false;
        }
        if (mDialogParentWindow.isMWWindow()) {
            return true;
        }
        return !onlyFather && ((WindowDecorView) mDialogParentDecor).isDialogFromMWParent();
    }

    public View getParentDecor() {
        return mDialogParentDecor;
    }

    public Window getParentWindow() {
        return mDialogParentWindow;
    }

    public int getDialogLeftOffset() {
        return (mDialogParentDecor.getWidth() - mDialogView.getWidth()) / 2;
    }

    public int getDialogTopOffset() {
        return (mDialogParentDecor.getHeight() - mDialogView.getHeight()) / 2;
    }

    public void enableScroll(View view, boolean enabledH, boolean enabledV) {
        if ((mScrollView == null) || (view != mScrollView)) {
            return;
        }
        mScrollHEnabled = enabledH;
        mScrollVEnabled = enabledV;
    }

    public boolean isEnableScrollH() {
        return mScrollHEnabled;
    }

    public boolean isEnableScrollV() {
        return mScrollVEnabled;
    }

    public boolean setScrollView(View view) {
        if (mScrollView == view) {
            return true;
        }
        if ((view != null) && ((getWidth() - view.getWidth() > SCROLLVIEW_DIFF_MAX_WIDTH)
                      || (getHeight() - view.getHeight() > SCROLLVIEW_DIFF_MAX_HEIGHT))) {
            return false;
        }
        mScrollView = view;
        enableScroll(view, false, false);
        return true;
    }

    public View getScrollView() {
        return mScrollView;
    }

    public void setScrollBarRangeH(int range) {
        mScrollBarRangeH = range;
    }

    public int getScrollBarRangeH() {
        return mScrollBarRangeH;
    }

    public void setScrollBarOffsetH(int offset) {
        mScrollBarOffsetH = offset;
    }

    public int getScrollBarOffsetH() {
        return mScrollBarOffsetH;
    }

    public void setScrollBarExtentH(int extent) {
        mScrollBarExtentH = extent;
    }

    public int getScrollBarExtentH() {
        return mScrollBarExtentH;
    }

    public void setScrollBarRangeV(int range) {
        mScrollBarRangeV = range;
    }

    public int getScrollBarRangeV() {
        return mScrollBarRangeV;
    }

    public void setScrollBarOffsetV(int offset) {
        mScrollBarOffsetV = offset;
    }

    public int getScrollBarOffsetV() {
        return mScrollBarOffsetV;
    }

    public void setScrollBarExtentV(int extent) {
        mScrollBarExtentV = extent;
    }

    public int getScrollBarExtentV() {
        return mScrollBarExtentV;
    }
}
