package com.android.documentsui.util;

import android.graphics.drawable.Drawable;

public class AppInfo {

	private Drawable icon;
	private String name;
	private String packName;
	private boolean isUser;
	private boolean isRom;

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackName() {
		return packName;
	}

	public void setPackName(String packName) {
		this.packName = packName;
	}

	public boolean isUser() {
		return isUser;
	}

	public void setUser(boolean isUser) {
		this.isUser = isUser;
	}

	public boolean isRom() {
		return isRom;
	}

	public void setRom(boolean isRom) {
		this.isRom = isRom;
	}

	@Override
	public String toString() {
		return "AppInfo [name=" + name + ", packName=" + packName + ", isUser="
				+ isUser + ", isRom=" + isRom + "]";
	}

}
