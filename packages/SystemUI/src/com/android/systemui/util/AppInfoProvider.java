package com.android.systemui.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class AppInfoProvider {

	/**
	 * 得到手机里所有应用的信息
	 */

	public static List<AppInfo> getAppInfos(Context context) {
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		// 包管理器
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
			// 应用程序的一个标识，可以是任意组合
			int flages = packInfo.applicationInfo.flags;// 应用交的答题卡
			if ((ApplicationInfo.FLAG_SYSTEM & flages) == 0) {
				// 用户程序
				appInfo.setUser(true);
			} else {
				// 系统应用
				appInfo.setUser(false);
			}

			if ((ApplicationInfo.FLAG_EXTERNAL_STORAGE & flages) == 0) {
				// 手机内存
				appInfo.setRom(true);
			} else {
				// 外部存储
				appInfo.setRom(false);
			}

			appInfos.add(appInfo);

		}

		return appInfos;
	}

}
