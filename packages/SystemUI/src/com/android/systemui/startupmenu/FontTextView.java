package com.android.systemui.startupmenu;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class FontTextView extends TextView {
    public FontTextView(Context context) {
        super(context);
        initFont(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFont(context);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFont(context);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFont(context);
    }

    private void initFont(Context context) {
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/PingFang_SC_Regular.ttf"));
    }
}
