package android.graphics.drawable;

import android.annotation.NonNull;
import android.graphics.*;
import android.graphics.PorterDuff.Mode;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewDebug;

import com.android.internal.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

// Have to extend from ColorDrawable, for some applications (e.g. Clock)
// will still want ColorDrawable.
public class BackgroundDrawable extends ColorDrawable {
    private final Paint mPaint = new Paint();

    private int mColor;
    private int mPadding;

    public BackgroundDrawable() {
        mColor = Color.BLACK;
    }

    public BackgroundDrawable(int color) {
        mColor = color;
    }

    public void setDrawablePadding(int padding) {
        mPadding = padding;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect r = getBounds();

        mPaint.setColor(mColor);
        canvas.drawRect((float) (r.left + mPadding), (float) (r.top + mPadding),
                        (float) (r.right - mPadding), (float) (r.bottom - mPadding), mPaint);
    }

    @Override
    public int getColor() {
        return mColor;
    }

    @Override
    public void setColor(int color) {
        mColor = color;
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return mColor >>> 24;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public void setAlpha(int alpha) {
        alpha += alpha >> 7;   // make it 0..256
        final int baseAlpha = mColor >>> 24;
        final int useAlpha = baseAlpha * alpha >> 8;
        final int useColor = (mColor << 8 >>> 8) | (useAlpha << 24);
        if (mColor != useColor) {
            mColor = useColor;
            invalidateSelf();
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
