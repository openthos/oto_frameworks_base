/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.server.wm;

import static android.view.WindowManager.MultiWindow.*;
import static com.android.server.wm.WindowManagerService.DEBUG_TASK_MOVEMENT;
import static com.android.server.wm.WindowManagerService.TAG;

import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.os.Debug;
import android.os.RemoteException;
import android.util.EventLog;
import android.util.Slog;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.android.server.EventLogTags;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TaskStack  implements WindowManager.MultiWindow.Callback {
    /** Amount of time in milliseconds to animate the dim surface from one value to another,
     * when no window animation is driving it. */
    private static final int DEFAULT_DIM_DURATION = 200;

    /** Unique identifier */
    final int mStackId;

    /** Indicates whether task stack was relayouted using relayoutWindow. */
    private boolean mCrappyRelayouted = false;

    /** Is multiwindow ? */
    private boolean mIsFloating = false;

    /** The service */
    private final WindowManagerService mService;

    /** The display this stack sits under. */
    private DisplayContent mDisplayContent;

    /** The Tasks that define this stack. Oldest Tasks are at the bottom. The ordering must match
     * mTaskHistory in the ActivityStack with the same mStackId */
    private final ArrayList<Task> mTasks = new ArrayList<Task>();

    /** For comparison with DisplayContent bounds. */
    private Rect mTmpRect = new Rect();

    /** Content limits relative to the DisplayContent this sits in. */
    private Rect mBounds = new Rect();

    /** Whether mBounds is fullscreen */
    private boolean mFullscreen = true;

    /** Used to support {@link android.view.WindowManager.LayoutParams#FLAG_DIM_BEHIND} */
    private DimLayer mDimLayer;

    /** The particular window with FLAG_DIM_BEHIND set. If null, hide mDimLayer. */
    WindowStateAnimator mDimWinAnimator;

    /** Support for non-zero {@link android.view.animation.Animation#getBackgroundColor()} */
    DimLayer mAnimationBackgroundSurface;

    /** The particular window with an Animation with non-zero background color. */
    WindowStateAnimator mAnimationBackgroundAnimator;

    /** Set to false at the start of performLayoutAndPlaceSurfaces. If it is still false by the end
     * then stop any dimming. */
    boolean mDimmingTag;

    /** Application tokens that are exiting, but still on screen for animations. */
    final AppTokenList mExitingAppTokens = new AppTokenList();

    /** Detach this stack from its display when animation completes. */
    boolean mDeferDetach;

    WindowManager.MultiWindow mMultiWindow;
    Rect mDialogRect;
    boolean mEnableMultiWindow = false;

    WindowManager.MultiWindow.ResizeWindow mCurrentWindow;
    WindowManager.MultiWindow.ResizeWindow mResizeWindow;
    WindowManager.MultiWindow.MoveWindow mMoveWindow;

    TaskStack(WindowManagerService service, int stackId, boolean floating) {
        mService = service;
        mStackId = stackId;
        mIsFloating = floating;
        // TODO: remove bounds from log, they are always 0.
        EventLog.writeEvent(EventLogTags.WM_STACK_CREATED, stackId, mBounds.left, mBounds.top,
                mBounds.right, mBounds.bottom);
    }

    private boolean isOnFrame(int x, int y) {
        return (x <= mMultiWindow.getFramePadding()) || (y <= mMultiWindow.getTopFramePadding())
               || (x >= (mBounds.width() - mMultiWindow.getFramePadding()))
               || (y >= (mBounds.height() - mMultiWindow.getFramePadding()));
    }

    private boolean isOnHeader(int x, int y) {
        return (x > mMultiWindow.getFramePadding()) && (y > mMultiWindow.getTopFramePadding())
               && (x < (mBounds.width() - mMultiWindow.getFramePadding()))
               && (y < (mMultiWindow.getTopFramePadding() + mMultiWindow.mHeaderHeight));
    }

    private boolean isOnRect(Rect rect, int x, int y) {
        return (x > rect.left) && (x < rect.right) && (y > rect.top) && (y < rect.bottom);
    }

    private boolean isOnContentArea(int x, int y) {
        int offX = (mBounds.width() - mDialogRect.width()) / 2;
        int offY = (mBounds.height() - mDialogRect.height()) / 2;
        if ((x > offX) && (x < (mBounds.width() - offX))
            && (y > offY) && (y < (mBounds.height() - offY))) {
            return false;
        }
        return true;
    }

    public void onHoverEvent(int what, int x, int y) {
        if (!mEnableMultiWindow) {
            return;
        }
        switch (what) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                syncResizingIcon(mMultiWindow.getResizeWays(mBounds, x, y));
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                syncResizingIcon(mMultiWindow.getResizeWays(null, 0, 0));
                break;
        }
    }

    public void onTouchEvent(int what, int x, int y) {
        if (!mEnableMultiWindow) {
            return;
        }
        x -= mBounds.left;
        y -= mBounds.top;
        switch (what) {
            case MotionEvent.ACTION_DOWN:
                mCurrentWindow = null;
                if (isOnFrame(x, y)) {
                    mMultiWindow.onTouchWindow(what, x + mBounds.left, y + mBounds.top,
                                               mBounds, mResizeWindow, false);
                    mCurrentWindow = mResizeWindow;
                } else if (isOnHeader(x, y)) {
                    int w = mBounds.width();
                    if (!isOnRect(mMultiWindow.mBack, x, y)
                        && !isOnRect(mMultiWindow.mMin.toRect(w, mTmpRect), x, y)
                        && !isOnRect(mMultiWindow.mMax.toRect(w, mTmpRect), x, y)
                        && !isOnRect(mMultiWindow.mClose.toRect(w, mTmpRect), x, y)) {
                        mMultiWindow.onTouchWindow(what, x + mBounds.left, y + mBounds.top,
                                                   mBounds, mMoveWindow, true);
                        mCurrentWindow = mMoveWindow;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurrentWindow != null) {
                    mMultiWindow.onTouchWindow(what, x + mBounds.left, y + mBounds.top,
                                            mBounds, mCurrentWindow, mCurrentWindow == mMoveWindow);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mCurrentWindow != null) {
                    mMultiWindow.onTouchWindow(what, x + mBounds.left, y + mBounds.top,
                                            mBounds, mCurrentWindow, mCurrentWindow == mMoveWindow);
                    mCurrentWindow = null;
                    break;
                }
                if (isOnFrame(x, y)) {
                    ; // Skip, at present.
                } else if (isOnHeader(x, y)) {
                    int w = mBounds.width();
                    if (isOnRect(mMultiWindow.mBack, x, y)) {
                        KeyEvent.sendKeyEventBack();
                    } else if (isOnRect(mMultiWindow.mMin.toRect(w, mTmpRect), x, y)) {
                        mMultiWindow.onMinimize(mBounds);
                    } else if (isOnRect(mMultiWindow.mMax.toRect(w, mTmpRect), x, y)) {
                        mMultiWindow.toggleFullScreen(mBounds);
                    } else if (isOnRect(mMultiWindow.mClose.toRect(w, mTmpRect), x, y)) {
                        try {
                            mService.mActivityManager.closeActivityAsync(mStackId);
                        } catch (RemoteException e) {
                            Slog.e(TAG, "Close failed", e);
                        }
                    }
                } else if (isOnContentArea(x, y)) {
                    KeyEvent.sendKeyEventBack();
                }
                break;
            default:
                break;
        }
    }

    public Rect disableMultiWindow() {
        if (mEnableMultiWindow) {
            if (mMultiWindow.isResizing()) {
                syncResizingIcon(mMultiWindow.getResizeWays(null, 0, 0));
            }
            mEnableMultiWindow = false;
            return mMultiWindow.mOldSize;
        }
        return new Rect();
    }

    public void enableMultiWindow(WindowManager.MultiWindow mw, Rect dialogRect) {
        mEnableMultiWindow  = true;

        if (mDialogRect == null) {
            mDialogRect = new Rect();
        }
        mDialogRect.set(dialogRect); // Now, only consider about center dialog.

        syncMultiWindow(mw);
    }

    public void syncMultiWindow(WindowManager.MultiWindow mw) {
        if (mResizeWindow == null) {
            mResizeWindow = new WindowManager.MultiWindow.ResizeWindow();
        }
        if (mMoveWindow == null) {
            mMoveWindow = new WindowManager.MultiWindow.MoveWindow();
        }
        if (mMultiWindow == null) {
            mMultiWindow = new WindowManager.MultiWindow(mService.mContext);
        }

        mMultiWindow.mStackId = mStackId;
        mMultiWindow.mCallback = this;

        mMultiWindow.mShadowPadding = mw.mShadowPadding;
        mMultiWindow.mTopShadowPadding = mw.mTopShadowPadding;
        mMultiWindow.mBorderPadding = mw.mBorderPadding;
        mMultiWindow.mTopBorderPadding = mw.mTopBorderPadding;
        mMultiWindow.mHeaderHeight = mw.mHeaderHeight;

        mMultiWindow.mBack.set(mw.mBack);
        mMultiWindow.mMin.set(mw.mMin);
        mMultiWindow.mMax.set(mw.mMax);
        mMultiWindow.mClose.set(mw.mClose);

        mMultiWindow.mOldSize.set(mw.mOldSize);
        mMultiWindow.mFullScreen.set(mw.mFullScreen);
        mMultiWindow.mLeftDockFrame.set(mw.mLeftDockFrame);
        mMultiWindow.mRightDockFrame.set(mw.mRightDockFrame);
    }

    DisplayContent getDisplayContent() {
        return mDisplayContent;
    }

    ArrayList<Task> getTasks() {
        return mTasks;
    }

    void resizeWindows() {
        final boolean underStatusBar = mBounds.top == 0;

        final ArrayList<WindowState> resizingWindows = mService.mResizingWindows;
        for (int taskNdx = mTasks.size() - 1; taskNdx >= 0; --taskNdx) {
            final ArrayList<AppWindowToken> activities = mTasks.get(taskNdx).mAppTokens;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; --activityNdx) {
                final ArrayList<WindowState> windows = activities.get(activityNdx).allAppWindows;
                for (int winNdx = windows.size() - 1; winNdx >= 0; --winNdx) {
                    final WindowState win = windows.get(winNdx);
                    if (!resizingWindows.contains(win)) {
                        if (WindowManagerService.DEBUG_RESIZE) Slog.d(TAG,
                                "setBounds: Resizing " + win);
                        resizingWindows.add(win);
                    }
                    win.mUnderStatusBar = underStatusBar;
                }
            }
        }
    }

    private boolean isHomeStack() {
        TaskStack stack = mDisplayContent.getHomeStack();
        if ((stack != null) && (stack.mStackId == mStackId)) {
            return true;
        }
        return false;
    }

    boolean setBounds(Rect bounds) {

        if (!isHomeStack()) {
            return false;
        }

        boolean oldFullscreen = mFullscreen;
        if (mDisplayContent != null) {
            mDisplayContent.getLogicalDisplayRect(mTmpRect);
            mFullscreen = mTmpRect.equals(bounds);
        }

        if (mBounds.equals(bounds) && oldFullscreen == mFullscreen) {
            return false;
        }

        return setBoundsByForce(bounds);
    }

    boolean setBoundsByForce(Rect bounds) {
        mDimLayer.setBounds(bounds);
        mAnimationBackgroundSurface.setBounds(bounds);
        mBounds.set(bounds);
        return true;
    }

    void getBounds(Rect out) {
        out.set(mBounds);
    }

    void updateDisplayInfo() {
        if (mFullscreen && mDisplayContent != null) {
            mDisplayContent.getLogicalDisplayRect(mTmpRect);
            setBounds(mTmpRect);
        }
    }

    boolean isFullscreen() {
        return mFullscreen;
    }

    boolean isAnimating() {
        for (int taskNdx = mTasks.size() - 1; taskNdx >= 0; --taskNdx) {
            final ArrayList<AppWindowToken> activities = mTasks.get(taskNdx).mAppTokens;
            for (int activityNdx = activities.size() - 1; activityNdx >= 0; --activityNdx) {
                final ArrayList<WindowState> windows = activities.get(activityNdx).allAppWindows;
                for (int winNdx = windows.size() - 1; winNdx >= 0; --winNdx) {
                    final WindowStateAnimator winAnimator = windows.get(winNdx).mWinAnimator;
                    if (winAnimator.isAnimating() || winAnimator.mWin.mExiting) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Put a Task in this stack. Used for adding and moving.
     * @param task The task to add.
     * @param toTop Whether to add it to the top or bottom.
     */
    void addTask(Task task, boolean toTop) {
        int stackNdx;
        if (!toTop) {
            stackNdx = 0;
        } else {
            stackNdx = mTasks.size();
            if (!mService.isCurrentProfileLocked(task.mUserId)) {
                // Place the task below all current user tasks.
                while (--stackNdx >= 0) {
                    if (!mService.isCurrentProfileLocked(mTasks.get(stackNdx).mUserId)) {
                        break;
                    }
                }
                // Put it above first non-current user task.
                ++stackNdx;
            }
        }
        if (DEBUG_TASK_MOVEMENT) Slog.d(TAG, "addTask: task=" + task + " toTop=" + toTop
                + " pos=" + stackNdx);
        mTasks.add(stackNdx, task);

        task.mStack = this;
        mDisplayContent.moveStack(this, true);
        EventLog.writeEvent(EventLogTags.WM_TASK_MOVED, task.taskId, toTop ? 1 : 0, stackNdx);
    }

    void moveTaskToTop(Task task) {
        if (DEBUG_TASK_MOVEMENT) Slog.d(TAG, "moveTaskToTop: task=" + task + " Callers="
                + Debug.getCallers(6));
        mTasks.remove(task);
        addTask(task, true);
    }

    void moveTaskToBottom(Task task) {
        if (DEBUG_TASK_MOVEMENT) Slog.d(TAG, "moveTaskToBottom: task=" + task);
        mTasks.remove(task);
        addTask(task, false);
    }

    /**
     * Delete a Task from this stack. If it is the last Task in the stack, move this stack to the
     * back.
     * @param task The Task to delete.
     */
    void removeTask(Task task) {
        if (DEBUG_TASK_MOVEMENT) Slog.d(TAG, "removeTask: task=" + task);
        mTasks.remove(task);
        if (mDisplayContent != null) {
            if (mTasks.isEmpty()) {
                mDisplayContent.moveStack(this, false);
            }
            mDisplayContent.layoutNeeded = true;
        }
    }

    void attachDisplayContent(DisplayContent displayContent) {
        if (mDisplayContent != null) {
            throw new IllegalStateException("attachDisplayContent: Already attached");
        }

        mDisplayContent = displayContent;
        mDimLayer = new DimLayer(mService, this, displayContent);
        mAnimationBackgroundSurface = new DimLayer(mService, this, displayContent);
        updateDisplayInfo();
    }

    void detachDisplay() {
        EventLog.writeEvent(EventLogTags.WM_STACK_REMOVED, mStackId);

        boolean doAnotherLayoutPass = false;
        for (int taskNdx = mTasks.size() - 1; taskNdx >= 0; --taskNdx) {
            final AppTokenList appWindowTokens = mTasks.get(taskNdx).mAppTokens;
            for (int appNdx = appWindowTokens.size() - 1; appNdx >= 0; --appNdx) {
                final WindowList appWindows = appWindowTokens.get(appNdx).allAppWindows;
                for (int winNdx = appWindows.size() - 1; winNdx >= 0; --winNdx) {
                    mService.removeWindowInnerLocked(null, appWindows.get(winNdx));
                    doAnotherLayoutPass = true;
                }
            }
        }
        if (doAnotherLayoutPass) {
            mService.requestTraversalLocked();
        }

        mAnimationBackgroundSurface.destroySurface();
        mAnimationBackgroundSurface = null;
        mDimLayer.destroySurface();
        mDimLayer = null;
        mDisplayContent = null;
    }

    void resetAnimationBackgroundAnimator() {
        mAnimationBackgroundAnimator = null;
        mAnimationBackgroundSurface.hide();
    }

    private long getDimBehindFadeDuration(long duration) {
        TypedValue tv = new TypedValue();
        mService.mContext.getResources().getValue(
                com.android.internal.R.fraction.config_dimBehindFadeDuration, tv, true);
        if (tv.type == TypedValue.TYPE_FRACTION) {
            duration = (long)tv.getFraction(duration, duration);
        } else if (tv.type >= TypedValue.TYPE_FIRST_INT && tv.type <= TypedValue.TYPE_LAST_INT) {
            duration = tv.data;
        }
        return duration;
    }

    boolean animateDimLayers() {
        final int dimLayer;
        final float dimAmount;
        if (mDimWinAnimator == null) {
            dimLayer = mDimLayer.getLayer();
            dimAmount = 0;
        } else {
            dimLayer = mDimWinAnimator.mAnimLayer - WindowManagerService.LAYER_OFFSET_DIM;
            dimAmount = mDimWinAnimator.mWin.mAttrs.dimAmount;
        }
        final float targetAlpha = mDimLayer.getTargetAlpha();
        if (targetAlpha != dimAmount) {
            if (mDimWinAnimator == null) {
                mDimLayer.hide(DEFAULT_DIM_DURATION);
            } else {
                long duration = (mDimWinAnimator.mAnimating && mDimWinAnimator.mAnimation != null)
                        ? mDimWinAnimator.mAnimation.computeDurationHint()
                        : DEFAULT_DIM_DURATION;
                if (targetAlpha > dimAmount) {
                    duration = getDimBehindFadeDuration(duration);
                }
                mDimLayer.show(dimLayer, dimAmount, duration);
            }
        } else if (mDimLayer.getLayer() != dimLayer) {
            mDimLayer.setLayer(dimLayer);
        }
        if (mDimLayer.isAnimating()) {
            if (!mService.okToDisplay()) {
                // Jump to the end of the animation.
                mDimLayer.show();
            } else {
                return mDimLayer.stepAnimation();
            }
        }
        return false;
    }

    void resetDimmingTag() {
        mDimmingTag = false;
    }

    void setDimmingTag() {
        mDimmingTag = true;
    }

    boolean testDimmingTag() {
        return mDimmingTag;
    }

    boolean isDimming() {
        return mDimLayer.isDimming();
    }

    boolean isDimming(WindowStateAnimator winAnimator) {
        return mDimWinAnimator == winAnimator && mDimLayer.isDimming();
    }

    void startDimmingIfNeeded(WindowStateAnimator newWinAnimator) {
        // Only set dim params on the highest dimmed layer.
        final WindowStateAnimator existingDimWinAnimator = mDimWinAnimator;
        // Don't turn on for an unshown surface, or for any layer but the highest dimmed layer.
        if (newWinAnimator.mSurfaceShown && (existingDimWinAnimator == null
                || !existingDimWinAnimator.mSurfaceShown
                || existingDimWinAnimator.mAnimLayer < newWinAnimator.mAnimLayer)) {
            mDimWinAnimator = newWinAnimator;
        }
    }

    void stopDimmingIfNeeded() {
        if (!mDimmingTag && isDimming()) {
            mDimWinAnimator = null;
        }
    }

    void setAnimationBackground(WindowStateAnimator winAnimator, int color) {
        int animLayer = winAnimator.mAnimLayer;
        if (mAnimationBackgroundAnimator == null
                || animLayer < mAnimationBackgroundAnimator.mAnimLayer) {
            mAnimationBackgroundAnimator = winAnimator;
            animLayer = mService.adjustAnimationBackground(winAnimator);
            mAnimationBackgroundSurface.show(animLayer - WindowManagerService.LAYER_OFFSET_DIM,
                    ((color >> 24) & 0xff) / 255f, 0);
        }
    }

    void switchUser(int userId) {
        int top = mTasks.size();
        for (int taskNdx = 0; taskNdx < top; ++taskNdx) {
            Task task = mTasks.get(taskNdx);
            if (mService.isCurrentProfileLocked(task.mUserId)) {
                mTasks.remove(taskNdx);
                mTasks.add(task);
                --top;
            }
        }
    }

    void close() {
        mDimLayer.mDimSurface.destroy();
        mAnimationBackgroundSurface.mDimSurface.destroy();
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix); pw.print("mStackId="); pw.println(mStackId);
        pw.print(prefix); pw.print("mDeferDetach="); pw.println(mDeferDetach);
        for (int taskNdx = 0; taskNdx < mTasks.size(); ++taskNdx) {
            pw.print(prefix); pw.println(mTasks.get(taskNdx));
        }
        if (mAnimationBackgroundSurface.isDimming()) {
            pw.print(prefix); pw.println("mWindowAnimationBackgroundSurface:");
            mAnimationBackgroundSurface.printTo(prefix + "  ", pw);
        }
        if (mDimLayer.isDimming()) {
            pw.print(prefix); pw.println("mDimLayer:");
            mDimLayer.printTo(prefix, pw);
            pw.print(prefix); pw.print("mDimWinAnimator="); pw.println(mDimWinAnimator);
        }
        if (!mExitingAppTokens.isEmpty()) {
            pw.println();
            pw.println("  Exiting application tokens:");
            for (int i=mExitingAppTokens.size()-1; i>=0; i--) {
                WindowToken token = mExitingAppTokens.get(i);
                pw.print("  Exiting App #"); pw.print(i);
                pw.print(' '); pw.print(token);
                pw.println(':');
                token.dump(pw, "    ");
            }
        }
    }

    @Override
    public String toString() {
        return "{stackId=" + mStackId + " tasks=" + mTasks + "}";
    }

    public boolean isFloating() {
        return mIsFloating;
    }

    public void setCrappyRelayouted(boolean val) {
        mCrappyRelayouted = val;
    }

    public boolean isCrappyRelayouted() {
        return mCrappyRelayouted;
    }

    public void relayoutWindow(int stackId, Rect rect) {
        mService.relayoutWindowLocked(stackId, rect);
    }

    public void saveInfoInStatusbarActivity(int stackId, Rect rect) {
        try {
            mService.mActivityManager.saveInfoInStatusbarActivity(stackId, rect);
        } catch (RemoteException e) {
        }
    }

    public int getFocusedStackId() {
        try {
            return mService.mActivityManager.getFocusedStackId();
        } catch (RemoteException e) {
        }
        return -1;
    }

    public void setFocusedStack(int stackId) {
        try {
            mService.mActivityManager.setFocusedStackAsync(stackId);
        } catch (RemoteException e) {
        }
    }

    public void unsetFocusedStack(int stackId) {
        try {
            mService.mActivityManager.unsetFocusedStackAsync(stackId);
        } catch (RemoteException e) {
        }
    }

    public void syncResizingIcon(int ways) {
        InputManager.getInstance().setPointerIcon(mMultiWindow.wayToIcon(ways));
    }

    public int getScreenHeight(int stackId) {
        return mDisplayContent.getDisplayInfo().logicalHeight;
    }
}
