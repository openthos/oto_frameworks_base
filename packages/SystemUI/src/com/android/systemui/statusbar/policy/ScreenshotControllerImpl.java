package com.android.systemui.statusbar.policy;

import android.content.Context;
import java.util.UUID;

public class ScreenshotControllerImpl implements ScreenshotController {
    private static final String TAG = "ScreenshotControllerImpl";
    private final Context mContext;

    public ScreenshotControllerImpl(Context context) {
        mContext = context;
    }
}
