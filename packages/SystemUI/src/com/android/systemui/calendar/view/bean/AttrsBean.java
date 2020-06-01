package com.android.systemui.calendar.view.bean;

import android.graphics.Color;


import com.android.systemui.R;

import java.util.Map;

public class AttrsBean {

    private int[] startDate;
    private int[] endDate;
    private int[] singleDate;
    private boolean showLastNext = true;
    private boolean switchChoose = true;
    private int colorSolar = Color.BLACK;
    private int dayBg = R.drawable.date_current_bg;
    private Map<String, String> specifyMap;
    private int chooseType = 0;

    public int[] getStartDate() {
        return startDate;
    }

    public void setStartDate(int[] startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(int[] endDate) {
        this.endDate = endDate;
    }

    public int[] getSingleDate() {
        return singleDate;
    }

    public void setSingleDate(int[] singleDate) {
        this.singleDate = singleDate;
    }

    public boolean isShowLastNext() {
        return showLastNext;
    }

    public void setShowLastNext(boolean showLastNext) {
        this.showLastNext = showLastNext;
    }

    public boolean isSwitchChoose() {
        return switchChoose;
    }

    public int getColorSolar() {
        return colorSolar;
    }

    public int getDayBg() {
        return dayBg;
    }

    public void setDayBg(int dayBg) {
        this.dayBg = dayBg;
    }

    public Map<String, String> getSpecifyMap() {
        return specifyMap;
    }

    public void setSpecifyMap(Map<String, String> specifyMap) {
        this.specifyMap = specifyMap;
    }

    public int getChooseType() {
        return chooseType;
    }

    public void setChooseType(int chooseType) {
        this.chooseType = chooseType;
    }
}
