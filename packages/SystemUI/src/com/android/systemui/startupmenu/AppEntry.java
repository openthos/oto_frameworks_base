/* Copyright 2016 Braden Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.startupmenu;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.os.UserManager;

import java.io.Serializable;

public class AppEntry implements Serializable {
    static final long serialVersionUID = -3982172488299272068L;

    private String label;
    private String packageName;
    private String activityName;
    private Long userId;
    private Long installTime;
    private Long lastTimeUsed;
    private int useCounts;
    private Drawable icon;
    private ComponentName componentName;

    public AppEntry() {
    }

    public AppEntry(String packageName) {
        this.packageName = packageName;
    }

    public AppEntry(String packageName, String activityName) {
        this.packageName = packageName;
        this.activityName = activityName;
    }

    public AppEntry(ComponentName componentName) {
        this.componentName = componentName;
        this.packageName = componentName.getPackageName();
        this.activityName = componentName.getClassName();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public ComponentName getComponentName() {
        if (componentName == null) {
            componentName = new ComponentName(packageName, activityName);
        }
        return componentName;
    }

    public void setComponentName(ComponentName componentName) {
        this.componentName = componentName;
    }

    public Long getUserId(Context context) {
        if (userId == null) {
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            return userManager.getSerialNumberForUser(Process.myUserHandle());
        } else
            return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getInstallTime() {
        return installTime;
    }

    public void setInstallTime(Long installTime) {
        this.installTime = installTime;
    }

    public Long getLastTimeUsed() {
        return lastTimeUsed;
    }

    public void setLastTimeUsed(Long lastTimeUsed) {
        this.lastTimeUsed = lastTimeUsed;
    }

    public int getUseCounts() {
        return useCounts;
    }

    public void setUseCounts(int useCounts) {
        this.useCounts = useCounts;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }


}