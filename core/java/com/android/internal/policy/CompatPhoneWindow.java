package com.android.internal.policy;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl.ActivityConfigCallback;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.util.DisplayMetrics;
import android.os.SystemProperties;

import java.util.ArrayList;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class CompatPhoneWindow extends PhoneWindow {

    CompatDecorView compatDecor;
    Context mAppContext;

    public CompatPhoneWindow(Context context) {
        super(context);
    }

    public CompatPhoneWindow(Context context, Context appContext, Window preservedWindow,
            ActivityConfigCallback activityConfigCallback) {
        super(context, preservedWindow, activityConfigCallback);
        mAppContext = appContext;
    }

    @Override
    public View getDecorView() {
        View superDecor = super.getDecorView();
        if (compatDecor != null) {
            return compatDecor.getSpecialDecor();
        }
        return superDecor;
    }

    @Override
    protected DecorView generateDecor(int featureId) {
        Context context;
        if (mUseDecorContext) {
            Context applicationContext = getContext().getApplicationContext();
            if (applicationContext == null) {
                context = getContext();
            } else {
                context = new DecorContext(applicationContext, getContext().getResources());
                if (mTheme != -1) {
                    context.setTheme(mTheme);
                }
            }
        } else {
            context = getContext();
        }
        compatDecor = new CompatDecorView(context, featureId, this, getAttributes());
        return compatDecor;
    }

    private final class FakeDecor extends FrameLayout {
        FakeDecor(Context context) {
            super(context);
            mIsFakeDecor = true;
        }

        @Override
        public void getLocationOnScreen(int[] outLocation) {
            outLocation[0] = 0;
            outLocation[1] = compatDecor.mDecorCaptionView.hasCaption() ?
                    compatDecor.mDecorCaptionView.getCaption().getMeasuredHeight() : 0;
        }

        @Override
        public void getWindowVisibleDisplayFrame(Rect outRect) {
            super.getWindowVisibleDisplayFrame(outRect);
            int width = outRect.width();
            int height = outRect.height();
            outRect.left = 0;
            outRect.top = 0;
            outRect.right = width;
            outRect.bottom = height;
        }
    }

    private final class CompatDecorView extends DecorView {
        FakeDecor mFakeDecor;
        CompatDecorView(Context context, int featureId, PhoneWindow window,
                WindowManager.LayoutParams params) {
            super(context, featureId, window, params);
            mFakeDecor = new FakeDecor(context);
            getViewTreeObserver().addOnGlobalLayoutListener(
                         new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    View v = findViewById(ID_ANDROID_CONTENT);
                    if (v != null) {
                        View v1 = ((ViewGroup)v).getChildAt(0);
                        if (v1 != null && v1.getLayoutParams() != null &&
                                v1.getLayoutParams().height > v.getMeasuredHeight()) {
                            v1.getLayoutParams().height = v.getMeasuredHeight();
                            v1.requestLayout();
                        }
                    }
                }
            });
        }

        @Override
        public Resources getResources() {
            return mAppContext.getResources();
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            Rect hit = new Rect();
            mDecorCaptionView.getCaption().getHitRect(hit);
            final int x = (int) ev.getX();
            final int y = (int) ev.getY();
            if (hit.contains(x, y)) {
                ev.mmOffSet = false;
            } else {
                int[] out = getLocationOnScreen();
                ev.mmOffSet = true;
                ev.offSetX = out[0];
                ev.offSetY = out[1];
            }
            return super.onInterceptTouchEvent(ev);
        }

        void setRootToFakeDecorIfHasCaption() {
            if (mDecorCaptionView != null && mDecorCaptionView.getChildAt(0) != mFakeDecor) {
                if (mDecorCaptionView.getChildAt(0) == mContentRoot) {
                    mDecorCaptionView.removeView(mContentRoot);
                }
                mFakeDecor.addView(mContentRoot, 0,
                    new ViewGroup.MarginLayoutParams(MATCH_PARENT, MATCH_PARENT));
                mDecorCaptionView.addView(mFakeDecor,
                    new ViewGroup.MarginLayoutParams(MATCH_PARENT, MATCH_PARENT));
            }
        }

        @Override
        protected void onConfigurationChanged(Configuration newConfig) {
            setRootToFakeDecorIfHasCaption();
            mContext.getResources().getConfiguration().setTo(newConfig);
            super.onConfigurationChanged(newConfig);
            if (mContext.isCompatContext()) {
                Configuration appConfig = mAppContext.getResources().getConfiguration();
                DisplayMetrics dp = mAppContext.getResources().getDisplayMetrics();
                appConfig.densityDpi = 160;
                appConfig.setLocales(null);
                mContext.getResources().updateConfiguration(appConfig, dp);
            }
        }

        @Override
        void onResourcesLoaded(LayoutInflater inflater, int layoutResource) {
            super.onResourcesLoaded(inflater, layoutResource);
            setRootToFakeDecorIfHasCaption();
        }

        public View getSpecialDecor() {
            if (mDecorCaptionView != null) {
                return mFakeDecor;
            }
            return this;
        }
    }
}
