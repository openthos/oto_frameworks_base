package com.android.systemui.calendar.view.bean;

public class DateBean {
    private int[] solar;
    private int type;

    public int[] getSolar() {
        return solar;
    }

    public void setSolar(int year, int month, int day) {
        this.solar = new int[]{year, month, day};
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
