package com.android.systemui.startupmenu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class LaunchAppUtil {

    private static final int FULLSCREEN_WORKSPACE_STACK_ID = 1;
    private static final int FREEFORM_WORKSPACE_STACK_ID = 2;

    private static SharedPreferences pref;

    /**
     * get sharepreference entity
     * @param context
     * @return
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        if (pref == null) pref = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pref;
    }

    /**
     * launche App by packageName, activityName
     * @param context
     * @param packageName
     * @param activityName
     */
    public static void launchApp(Context context, String packageName, String activityName) {
        launchApp(context, new ComponentName(packageName, activityName));
    }

    /**
     * launch app by packageName
     * @param context
     * @param packageName
     */
    public static void launchApp(Context context, String packageName) {
        launchApp(context, context.getPackageManager().getLaunchIntentForPackage(packageName));
    }

    /**
     * launch app by CompententName and select starting mode
     * @param context
     * @param componentName
     */
    public static void launchApp(Context context, ComponentName componentName) {
        Intent intent = new Intent();
        intent.setComponent(componentName);
        launchApp(context, intent);
    }

    /**
     * launch app by intent and select starting mode
     * @param context
     * @param intent
     */
    public static void launchApp(Context context, Intent intent) {
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent, null);
    }
}
