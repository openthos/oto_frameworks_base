package com.android.systemui.startupmenu;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.android.systemui.R;

import java.lang.reflect.Method;

/**
 * Created by ljh on 17-9-25.
 */

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
     * launch app by compententName
     * @param context
     * @param componentName
     */
    public static void launchApp(Context context, ComponentName componentName) {
        launchApp(context, componentName, Display.STANDARD_MODE);
    }

    /**
     * launch app by packageName
     * @param context
     * @param packageName
     */
    public static void launchApp(Context context, String packageName) {
        launchApp(context, context.getPackageManager().
                getLaunchIntentForPackage(packageName), Display.DESKTOP_MODE);
    }

    /**
     * launch app by CompententName and select starting mode
     * @param context
     * @param componentName
     * @param startMode
     */
    public static void launchApp(Context context, ComponentName componentName, int startMode) {
        Intent intent = new Intent();
        intent.setComponent(componentName);
        launchApp(context, intent, startMode);
    }

    /**
     * launch app by intent
     * @param context
     * @param intent
     */
    public static void launchApp(Context context, Intent intent) {
        launchApp(context, intent, Display.DESKTOP_MODE);
    }

    /**
     * launch app by intent and select starting mode
     * @param context
     * @param intent
     * @param startMode
     */
    public static void launchApp(Context context, Intent intent, int startMode) {
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent, getBundle(context, startMode));
    }

    /**
     * get bundle by starting mode
     * @param context
     * @param startMode
     * @return
     */
    private static Bundle getBundle(Context context, int startMode) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ActivityOptions options = ActivityOptions.makeBasic();
            try {
                options.setLaunchStackId(FREEFORM_WORKSPACE_STACK_ID);
                switch (startMode) {
                    case Display.STANDARD_MODE:
                        options.setFreeformBoundsMode(Display.STANDARD_MODE);
                        break;
                    case Display.DESKTOP_MODE:
                        options.setFreeformBoundsMode(Display.DESKTOP_MODE);
                        break;
                    case Display.PHONE_MODE:
                        options.setFreeformBoundsMode(Display.PHONE_MODE);
                        break;
                }
                return options.toBundle();
            } catch (Exception e) { /* Gracefully fail */
                return null;
            }
        } else {
            return null;
        }
    }
}
