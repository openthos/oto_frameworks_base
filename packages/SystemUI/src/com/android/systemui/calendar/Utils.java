package com.android.systemui.calendar;

public class Utils {
    public static boolean iskeywordsSearched(String titleStr, String keywords) {
        if (titleStr.indexOf(keywords) != -1) {
            return true;
        } else {
            return false;
        }
    }
}