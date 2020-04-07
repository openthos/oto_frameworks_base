package com.android.systemui.startupmenu.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.startupmenu.bean.AppInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static String TAG = "StartupMenuView";
    private static String indexStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static String FILEMANAGER = "org.openthos.filemanager";
    public static String SETTINGS = "com.android.settings";
    public static String APP_RECENT = "app_recent_list";
    public static String DOC_RECENT = "doc_recent_list";
    public static HashMap<String, AppInfo> speApps = new HashMap<>();
    private static final String[] [] MIME_MapTable = {
        {"doc", "application/msword"},
        {"docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
        {"xls", "application/vnd.ms-excel"},
        {"xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
        {"ppt", "application/vnd.ms-powerpoint"},
        {"pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
        {"pqf", "application/x-cprplayer"},
        {"txt", "text/plain"}
    };

    public static String getTimeFormatText(Context context, AppInfo appInfo) {
        long minute = 60 * 1000;
        long hour = 60 * minute;
        long day = 24 * hour;
        long month = 31 * day;
        long year = 12 * month;

        long diff = System.currentTimeMillis() - appInfo.getTime();
        long r = 0;
        if (diff > year) {
            return appInfo.getYear() + context.getResources().getString(R.string.year)
                    + appInfo.getMonth() + context.getResources().getString(R.string.month)
                    + appInfo.getDay() + context.getResources().getString(R.string.day);
        }
        if (diff > day) {
            return appInfo.getMonth() + context.getResources().getString(R.string.month)
                    + appInfo.getDay() + context.getResources().getString(R.string.day);
        }
        if (diff > hour) {
            r = (diff / hour);
            return r + context.getResources().getString(R.string.hours_ago);
        }
        if (diff > minute) {
            r = (diff / minute);
            return r + context.getResources().getString(R.string.minutes_ago);
        }
        return context.getResources().getString(R.string.now);
    }

    public static void sortDatasByFirstLetter(List<AppInfo> appsData) {
        chineseToPinyin(appsData) ;
        Log.e("lxx","size = "+ appsData.size());
        Collections.sort(appsData, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                Log.e("lxx","firstletter="+o1.getFirstLetter()+" secondletter="+o2.getFirstLetter()+
                        " fisrtname="+o1.getLabel()+" secondname="+o2.getLabel());
                return o1.getFirstLetter().compareTo(o2.getFirstLetter());
            }
        });
    }

    public static void sortDatasByUseCount(Context context, List<AppInfo> appsUseCountData) {
        Collections.sort(appsUseCountData, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return o1.getUseCount() > o2.getUseCount() ? -1 : 1;
            }
        });
        Log.e("lxx-global","size="+appsUseCountData.size()+" string="+serialize(appsUseCountData));
        Settings.Global.putString(context.getContentResolver(),
                APP_RECENT, serialize(appsUseCountData));
    }

    public static void sorRecentDocsByTime(Context context, List<AppInfo> recentDocsData) {
        Collections.sort(recentDocsData, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return o1.getTime() > o2.getTime() ? -1 : 1;
            }
        });
        Settings.Global.putString(context.getContentResolver(),
                DOC_RECENT, serialize(recentDocsData));
    }

    public static void getPinyinList(AppInfo appInfo) {
        if (TextUtils.isEmpty(appInfo.getLabel())) {
            return;
        }
        StringBuffer bufferNamePiny = new StringBuffer();
        StringBuffer bufferNameMatch = new StringBuffer();
        String name = appInfo.getLabel();
        Log.e("lxx","111----size="+name.length());
        for (int i = 0; i < name.length(); i++) {
            StringBuffer bufferNamePer = new StringBuffer();
            String namePer = name.charAt(i) + "";
            for (int j = 0; j < namePer.length(); j++) {
                char character = namePer.charAt(j);
                String pinCh = getPinYin(String.valueOf(character));
                Log.e("lxx","111-----cha="+character+" pinCh="+pinCh+"size="+name.length());
                bufferNamePer.append(pinCh);
                bufferNameMatch.append(pinCh.charAt(0));
                bufferNamePiny.append(pinCh);
            }
            appInfo.getNamePinyinList().add(bufferNamePer.toString());
        }
        appInfo.setNamePinYin(bufferNamePiny.toString());
        appInfo.setMatchPin(bufferNameMatch.toString());
        String firstPinyin = appInfo.getNamePinYin().charAt(0) + "";
        if (indexStr.contains(firstPinyin)) {
            appInfo.setFirstLetter(firstPinyin);
        } else {
            appInfo.setFirstLetter("#");
        }
    }

    public static void updateUninstalledDatas(String packageName,
            List<AppInfo> appsData, List<AppInfo> appsUseCountData) {
        removeAppInfo(packageName, appsData);
        removeAppInfo(packageName, appsUseCountData);
    }

    public static void removeAppInfo(String packageName, List<AppInfo> datas) {
        Iterator<AppInfo> datasIterator = datas.iterator();
        while (datasIterator.hasNext()) {
            AppInfo next = datasIterator.next();
            if (packageName.equals(next.getPackageName()))
                datasIterator.remove();
        }
    }

    public static void updateInstalledDatas(Context context,
             String packageName, List<AppInfo> appsData, List<AppInfo> appsUseCountData) {
        try {
            PackageManager pm = context.getPackageManager();
            ResolveInfo info = findAppByPackageName(pm, packageName);
            AppInfo appAdded = new AppInfo();
            appAdded.setLabel((String) info.loadLabel(pm));
            appAdded.setComponentName(new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name));
            android.util.Log.e("lxx-install","ComponentName="+info.activityInfo.name+"--"+appAdded.getComponentName());
            getPinyinList(appAdded);
            appsData.add(appAdded);
            sortDatasByFirstLetter(appsData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ResolveInfo findAppByPackageName(PackageManager pm, String packageName) {
        ResolveInfo newAppInfo = null;
        List<ResolveInfo> tempAllApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);
        tempAllApps = pm.queryIntentActivities(mainIntent, 0);
        newAppInfo = tempAllApps.get(0);
        return newAppInfo;
    }

    public static String serialize(List<AppInfo> appUseCountData) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        String serStr = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(appUseCountData);
            serStr = byteArrayOutputStream.toString("ISO-8859-1");
            serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
            objectOutputStream.close();
            byteArrayOutputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error during serialize", e);
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error during serialize", e);
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error during serialize", e);
                }
            }
            return serStr;
        }
    }

    public static List<AppInfo> deSerialization(String str) {
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        List<AppInfo> appUseCountData = null;
        try {
            String redStr = java.net.URLDecoder.decode(str, "UTF-8");
            byteArrayInputStream = new ByteArrayInputStream(redStr.getBytes("ISO-8859-1"));
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            appUseCountData = (List<AppInfo>) objectInputStream.readObject();
            objectInputStream.close();
            byteArrayInputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error during serialize", e);
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error during serialize", e);
                }
            }
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error during serialize", e);
                }
            }
            return appUseCountData;
        }
    }

    public static AppInfo getSpeApps(String packageName){
        return speApps.get(packageName);
    }

    public static String getMIMEType(File file) {
        String type = "*/*";

        String name = file.getName();
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (end.equals("")) return type;
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    public static void filtDatas(String inputStr, List<AppInfo> original, List<AppInfo> appsList) {
        if (original.size() == 0) {
            return;
        }
        if (TextUtils.isEmpty(inputStr)) {
            resetSearchData(original);
            appsList.clear();
            appsList.addAll(original);
        } else {
            appsList.clear();
            resetSearchData(original);
            if (isNumeric(inputStr)) {
                findDataByNumberOrCN(inputStr, original, appsList);
            } else if (isContainChinese(inputStr)) {
                findDataByNumberOrCN(inputStr, original, appsList);
            } else if (isEnglishAlphabet(inputStr)) {
                findDataByEN(inputStr, original, appsList);
            } else {
                findDataByNumberOrCN(inputStr, original, appsList);
            }
        }
    }

    public static void resetSearchData(List<AppInfo> original) {
        for (int i = 0; i < original.size(); i++) {
            original.get(i).setMatchType(0);
        }
    }

    public static void findDataByNumberOrCN(String inputStr, List<AppInfo> original,
                                      List<AppInfo> appsList) {
        for (int i = 0; i < original.size(); i++) {
            AppInfo appInfo = original.get(i);
            if (!TextUtils.isEmpty(appInfo.getLabel()) && appInfo.getLabel().contains(inputStr)) {
                appInfo.setMatchType(1);
                appsList.add(appInfo);
                continue;
            }
            if (appInfo.getNumberList().size() > 0) {
                for (int j = 0; j < appInfo.getNumberList().size(); j++) {
                    String number = appInfo.getNumberList().get(j);
                    if (!TextUtils.isEmpty(number) && number.contains(inputStr)) {
                        appInfo.setMatchType(2);
                        appsList.add(appInfo);
                    }
                }
            }
        }
    }

    public static void findDataByEN(String inputStr, List<AppInfo> original,
                              List<AppInfo> appsList) {
        String searPinyin = getPinYin(inputStr);
        Log.e("lxx","333---search="+searPinyin);
        int searLength = searPinyin.length();
        String searPinyinFirst = searPinyin.charAt(0) + "";
        for (int i = 0; i < original.size(); i++) {
            AppInfo appInfo = original.get(i);
            appInfo.setMatchType(1);
            if (appInfo.getMatchPin().contains(searPinyin)) {
                appsList.add(appInfo);
            } else {
                boolean isMatch = false;
                for (int j = 0; j < appInfo.getNamePinyinList().size(); j++) {
                    String namePinyinPer = appInfo.getNamePinyinList().get(j);
                    if (!TextUtils.isEmpty(namePinyinPer) && namePinyinPer.startsWith(searPinyin)) {
                        appsList.add(appInfo);
                        isMatch = true;
                        break;
                    }
                }
                if (isMatch) {
                    continue;
                }
                if (!TextUtils.isEmpty(appInfo.getNamePinYin()) && appInfo.getNamePinYin().contains(searPinyin)) {
                    for (int j = 0; j < appInfo.getNamePinyinList().size(); j++) {
                        StringBuilder sbMatch = new StringBuilder();
                        for (int k = j; k < appInfo.getNamePinyinList().size(); k++) {
                            sbMatch.append(appInfo.getNamePinyinList().get(k));
                        }
                        if (sbMatch.toString().startsWith(searPinyin)) {
                            int length = 0;
                            for (int k = j; k < appInfo.getNamePinyinList().size(); k++) {
                                length = length + appInfo.getNamePinyinList().get(k).length();
                                if (length >= searLength) {
                                    break;
                                }
                            }
                            isMatch = true;
                            appsList.add(appInfo);
                        }
                    }
                }

                if (isMatch) {
                    continue;
                }

                if (appInfo.getNamePinyinList().size() > 2) {
                    for (int j = 0; j < appInfo.getNamePinyinList().size(); j++) {

                        StringBuilder sbMatch0 = new StringBuilder();
                        sbMatch0.append(appInfo.getNamePinyinList().get(j));
                        if (j < appInfo.getNamePinyinList().size() - 2) {
                            for (int k = j + 1; k < appInfo.getMatchPin().length(); k++) {
                                sbMatch0.append(appInfo.getMatchPin().charAt(k));
                                if (sbMatch0.toString().equals(searPinyin)) {
                                    appsList.add(appInfo);
                                    isMatch = true;
                                    break;
                                }
                            }
                        }

                        if (isMatch) {
                            break;
                        }

                        StringBuilder sbMatch1 = new StringBuilder();
                        for (int k = 0; k <= j; k++) {
                            sbMatch1.append(appInfo.getNamePinyinList().get(k));
                        }
                        if (j < appInfo.getNamePinyinList().size() - 2) {
                            for (int k = j + 1; k < appInfo.getMatchPin().length(); k++) {
                                sbMatch1.append(appInfo.getMatchPin().charAt(k));
                                if (sbMatch1.toString().equals(searPinyin)) {
                                    appsList.add(appInfo);
                                    isMatch = true;
                                    break;
                                }
                            }
                        }
                        if (isMatch) {
                            break;
                        }

                        if (j >= appInfo.getNamePinyinList().size() - 2) {
                            break;
                        }
                        StringBuilder sbMatch2 = new StringBuilder();
                        sbMatch2.append(appInfo.getNamePinyinList().get(j));
                        for (int k = j + 1; k < appInfo.getNamePinyinList().size(); k++) {
                            sbMatch2.append(appInfo.getNamePinyinList().get(k));
                            StringBuilder sbMatch3 = new StringBuilder();
                            sbMatch3.append(sbMatch2.toString());
                            if (j < appInfo.getNamePinyinList().size() - 2) {
                                for (int m = k + 1; m < appInfo.getMatchPin().length(); m++) {
                                    sbMatch3.append(appInfo.getMatchPin().charAt(m));
                                    if (sbMatch3.toString().equals(searPinyin)) {
                                        appsList.add(appInfo);
                                        isMatch = true;
                                        break;
                                    }
                                }
                            }
                            if (isMatch) {
                                break;
                            }
                        }

                        if (isMatch) {
                            break;
                        }
                    }
                }

            }
        }
    }

    private static boolean isNumeric(String str) {
        String regEx = "^-?[0-9]+$";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(str);
        return mat.find();
    }

    private static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }

    private static boolean isEnglishAlphabet(String str) {
        Pattern p = Pattern.compile("^[A-Za-z]+$");
        Matcher m = p.matcher(str);
        return m.find();
    }

    private static String getPinYin(String hanzi) {
        if (!isContainChinese(hanzi)) {
            Log.e("lxx","111-====---string="+hanzi.toUpperCase());
            return hanzi.toUpperCase();
        }
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(hanzi);
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            for (HanziToPinyin.Token token : tokens) {
                if (HanziToPinyin.Token.PINYIN == token.type) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        }
        return sb.toString().toUpperCase();
    }

    private static void chineseToPinyin(List<AppInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            AppInfo app = list.get(i);
            String pinyinString = getPinYin(app.getLabel());
            app.setPinYin(pinyinString);

            char tempChar = pinyinString.charAt(0);
            if ((tempChar < 'A' || tempChar > 'Z')) {
                app.setFirstLetter("#");
            } else {
                app.setFirstLetter(String.valueOf(tempChar));
            }
            Log.e("lxx-pinyin","pinyinstring="+pinyinString+" fisrtletter="+app.getFirstLetter()+" tempChar="+tempChar
                    +" name="+app.getLabel()+" pinyin="+app.getPinYin());
        }
    }
}
