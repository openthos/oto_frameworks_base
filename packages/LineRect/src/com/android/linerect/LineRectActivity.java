package com.android.linerect;

import android.R.layout;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import com.android.internal.os.BackgroundThread;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnHoverListener;
import android.util.Log;

public class LineRectActivity extends Activity {

    private static final String TAG = "LineRectActivity";

    private LineRectView mView;
    private static final long WAITING_INTERVAL = 100; // 0.1 second
    private static final long WAITING_TIMEOUT = 20;   // 2 second totally
    private int ANIMATION_TIME = 500;
    private int mInterval = 0;
    private Thread mThread;

    @Override
    protected void onCreate(Bundle context) {
        super.onCreate(context);
        setContentView(R.layout.line_rect_activity);
        mView = (LineRectView)findViewById(R.id.line_rect);
        invalidateRect();
        if (getIntent().getBooleanExtra(Intent.EXTRA_RECT_ANIMATION, false)) {
            Animation animation;
            if (getIntent().getIntExtra(Intent.EXTRA_RECT_LEFT, 0) == 0){
                animation = new ScaleAnimation(0, 1f, 0, 1f,
                                               Animation.RELATIVE_TO_SELF, 0f,
                                               Animation.RELATIVE_TO_SELF, 0.5f);
            } else {
                animation = new ScaleAnimation(0, 1f, 0, 1f,
                                               Animation.RELATIVE_TO_SELF, 1f,
                                               Animation.RELATIVE_TO_SELF, 0.5f);
            }
            animation.setDuration(ANIMATION_TIME);
            mView.startAnimation(animation);
        }
        mThread = new Thread("Line Rect Monitor Thread") {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(WAITING_INTERVAL);
                        mInterval++;
                        if (mInterval >= WAITING_TIMEOUT) {
                            System.exit(0);
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "Unexpected exception for Line Rect Monitor Thread");
                    }
                }
            }
        };
        mThread.start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        invalidateRect();
        mInterval = 0;
    }

    private void invalidateRect() {
        Rect r = new Rect();
        Intent intent = getIntent();
        r.left = intent.getIntExtra(Intent.EXTRA_RECT_LEFT, 0);
        r.top = intent.getIntExtra(Intent.EXTRA_RECT_TOP, 0);
        r.right = intent.getIntExtra(Intent.EXTRA_RECT_RIGHT, 0);
        r.bottom = intent.getIntExtra(Intent.EXTRA_RECT_BOTTOM, 0);
        boolean hasBackground = intent.getBooleanExtra(Intent.EXTRA_RECT_ANIMATION, false);
        if ((r.left == -1) && (r.top == -1) && (r.right == -1) && (r.bottom == -1)) {
            System.exit(0);
            return;
        }
        mView.mRect.set(r);
        mView.mHasBackground = hasBackground;
        mView.invalidate();
    }
}
