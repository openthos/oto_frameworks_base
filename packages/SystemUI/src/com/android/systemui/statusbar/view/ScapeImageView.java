package com.android.systemui.statusbar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.systemui.R;

public class ScapeImageView extends ImageView {
    private final float SCAPE_WIDTH_HEIGHT_SIZE;

    public ScapeImageView(Context context) {
        this(context, null);
    }

    public ScapeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScapeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScapeImageView);
        SCAPE_WIDTH_HEIGHT_SIZE = a.getFloat(R.styleable.ScapeImageView_scapeSize, 1);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (SCAPE_WIDTH_HEIGHT_SIZE == 1) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        } else {
            int widthSize = (int) (MeasureSpec.getSize(heightMeasureSpec) * SCAPE_WIDTH_HEIGHT_SIZE);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
