package com.android.linerect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

public class LineRectView extends FrameLayout {

    private static final String TAG = "LineRectView";

    private static final float LINERECT_LINE_WIDTH = 5.0f;
    private static final int LINERECT_BACKGROUND_COLOR = 0x7F251A4B;
    public Rect mRect = new Rect();
    public boolean mHasBackground = false;

    public LineRectView(Context context) {
        super(context);
    }

    public LineRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas c) {
        Paint paint = new Paint();
        super.onDraw(c);
        if (mHasBackground) {
            paint.setColor(LINERECT_BACKGROUND_COLOR);
            c.drawRect(mRect, paint);
        }

        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(LINERECT_LINE_WIDTH);
        c.drawLine(mRect.left, mRect.top, mRect.right, mRect.top, paint);
        c.drawLine(mRect.right, mRect.top, mRect.right, mRect.bottom, paint);
        c.drawLine(mRect.right, mRect.bottom, mRect.left, mRect.bottom, paint);
        c.drawLine(mRect.left, mRect.bottom, mRect.left, mRect.top, paint);
    }
}
