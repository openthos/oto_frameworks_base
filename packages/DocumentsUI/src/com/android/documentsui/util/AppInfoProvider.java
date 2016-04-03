package com.android.documentui.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class AppInfoProvider {

	public static List<AppInfo> getAppInfos(Context context) {
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packInfos = pm.getInstalledPackages(0);
		for (PackageInfo packInfo : packInfos) {
			AppInfo appInfo = new AppInfo();
			Drawable icon = packInfo.applicationInfo.loadIcon(pm);
			appInfo.setIcon(icon);
			String name = packInfo.applicationInfo.loadLabel(pm).toString()
					+ packInfo.applicationInfo.uid;
			appInfo.setName(name);
			String packName = packInfo.packageName;
			appInfo.setPackName(packName);
			int flages = packInfo.applicationInfo.flags;
			if ((ApplicationInfo.FLAG_SYSTEM & flages) == 0) {
				appInfo.setUser(true);
			} else {
				appInfo.setUser(false);
			}

			if ((ApplicationInfo.FLAG_EXTERNAL_STORAGE & flages) == 0) {
				appInfo.setRom(true);
			} else {
				appInfo.setRom(false);
			}

			appInfos.add(appInfo);
		}
		return appInfos;
	}
}
