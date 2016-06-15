package com.android.systemui.statusbar.policy;

import android.content.Context;
import java.util.UUID;

public class SettingControllerImpl implements SettingController {
    private static final String TAG = "SettingControllerImpl";
    private final Context mContext;

    public SettingControllerImpl(Context context) {
        mContext = context;
    }
}
