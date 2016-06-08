package com.android.linerect;

import android.R.layout;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnHoverListener;
import android.util.Log;

public class LineRectActivity extends Activity {

    private static final String TAG = "LineRectActivity";

    private LineRectView mView;

    @Override
    protected void onCreate(Bundle context) {
        super.onCreate(context);
        setContentView(R.layout.line_rect_activity);
        mView = (LineRectView)findViewById(R.id.line_rect);
        invalidateRect();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        invalidateRect();
    }

    private void invalidateRect() {
        Rect r = new Rect();
        Intent intent = getIntent();

        r.left = intent.getIntExtra(Intent.EXTRA_RECT_LEFT, 0);
        r.top = intent.getIntExtra(Intent.EXTRA_RECT_TOP, 0);
        r.right = intent.getIntExtra(Intent.EXTRA_RECT_RIGHT, 0);
        r.bottom = intent.getIntExtra(Intent.EXTRA_RECT_BOTTOM, 0);
        if ((r.left == -1) && (r.top == -1) && (r.right == -1) && (r.bottom == -1)) {
            System.exit(0);
            return;
        }
        mView.mRect.set(r);
        mView.invalidate();
    }
}
