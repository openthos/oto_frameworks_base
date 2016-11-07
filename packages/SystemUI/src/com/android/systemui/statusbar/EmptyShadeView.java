/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import android.content.ComponentName;

public class EmptyShadeView extends StackScrollerDecorView {

    PhoneStatusBar  mBar;
    public EmptyShadeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPhoneStatusBar(PhoneStatusBar phoneStatusBar) {
        mBar = phoneStatusBar;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected View findContentView() {
        return findViewById(R.id.notification_center);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        makeLayout();
    }

    private void makeLayout() {
        Button btnNotificationManager = (Button) findViewById(R.id.notificationManager);
        btnNotificationManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent display = new Intent();
                ComponentName cDisplay = new ComponentName("com.android.settings",
                                         "com.android.settings.NotificationAppList");
                display.setComponent(cDisplay);
                display.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(display);
            }
        });

        Button btnClearAll = (Button) findViewById(R.id.clearAll);
        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBar.clearAllNotifications();
            }
        });
    }
}
