package com.android.systemui.startupmenu.bean;

import android.content.ComponentName;

import java.io.Serializable;
import java.util.ArrayList;

public class AppInfo implements Serializable {
    private String initialLetter;
    private String name;
    private String packageName;
    private String activityName;
    private String pinYin;
    private String initialPinPer = "";
    private String namePinYin = "";
    private String year;
    private String month;
    private String day;
    private String path;
    private long time;
    private int useCount = 0;
    private int matchType = 0;
    private ArrayList<String> namePinyinList = new ArrayList<>();
    private ArrayList<String> numberList = new ArrayList<>();
    private boolean isLocked;

    public String getPackageName() {
        return packageName;
    }

    public String getInitialLetter() {
        return initialLetter;
    }

    public void setInitialLetter(String initialLetter) {
        this.initialLetter = initialLetter ;
    }

    public String getLabel() {
        return name;
    }

    public void setLabel(String name) {
        this.name = name ;
    }

    public String getPinYin() {
        return pinYin;
    }

    public void setPinYin(String pinYin) {
        this.pinYin = pinYin;
    }

    public ComponentName getComponentName() {
        return new ComponentName(packageName, activityName);
    }

    public void setComponentName(ComponentName componentName) {
        packageName = componentName.getPackageName();
        activityName = componentName.getClassName();
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public int getMatchType() {
        return matchType;
    }

    public void setMatchType(int matchType) {
        this.matchType = matchType;
    }

    public ArrayList<String> getNumberList() {
        return numberList;
    }

    public void setNumberList(ArrayList<String> numberList) {
        this.numberList = numberList;
    }

    public String getInitialPinPer() {
        return initialPinPer;
    }

    public void setInitialPinPer(String initialPinPer) {
        this.initialPinPer = initialPinPer;
    }

    public ArrayList<String> getNamePinyinList() {
        return namePinyinList;
    }

    public String getNamePinYin() {
        return namePinYin;
    }

    public void setNamePinYin(String namePinYin) {
        this.namePinYin = namePinYin;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
