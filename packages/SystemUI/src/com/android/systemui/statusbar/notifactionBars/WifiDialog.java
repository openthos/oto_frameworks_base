package com.android.systemui.statusbar.notificationbars;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.notifactionBars.WifiContentView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import android.view.Gravity;
import android.view.Window;
import android.text.TextUtils;
import android.content.ComponentName;

/**
 * Created by Mingkai on 2016/6/22.
 */
public final class WifiDialog extends BaseSettingDialog {
    private final String SCAN_WIFI = "scan wifi";
    private final String CLASSNAME_COMPONENTNAME =
                         "com.android.settings.ethernet.EthernetSettings";
    private final String PACKAGENAME_COMPONENTNAME = "com.android.settings";
    private final String TAG = "umic";

    private final int UPDATE_LIST = 7 << 1;
    private final int WIFI_DISCONNECT = 7 << 2;
    private final int WIFI_ENABLE = 7 << 3;
    private final int WIFI_UNABLE = 7 << 4;
    private final int START_WIFI_SACN = 7 << 5;
    private final int PAUSE_WIFI_SACN = 7 << 6;
    private final int UPDATE_CURRENT_WIFI = 7 << 7;
    private final int LISTVIEW_MOVE_TO_TOP = 7 << 8;
    private final int CONNECT_WIFI_AUTO = 18 << 1;
    private final int CONNECT_WIFI_PSW = 18 << 2;

    private PhoneStatusBar mPhoneStatusBar;

    private WifiInfo currentNet;
    private WifiManager netManager;
    private WifiCheckEnableReceiver mWifiCheckEnableReceiver;
    private WifiScanReceiver mWifiScanReceiver;

    private ListView netListView;
    private Switch netEnableBtn;
    private TextView netEnable;
    private LinearLayout container;

    private List<ScanResult> netList;
    private List<WifiConfiguration> wifiCfgList;
    private NetInfoAdapter mNetInfoAdapter;
    private LinearLayout mEthnetSetLl;

    private String waitConnectWifi;
    private String waitInputPswWifi;
    private String startCntWifi;

    final WifiConnector.NetConnectListener wifiCntListener = new WifiConnector
                                                                     .NetConnectListener() {
        @Override
        public void OnWifiConnectCompleted(boolean isConnected) {
            if (isConnected) {
                Log.d(TAG, "connect success.");
            } else {
                Log.d(TAG, "connect fail.");
            }
            waitConnectWifi = null;
            waitInputPswWifi = null;
            startCntWifi = null;
            mHandler.sendEmptyMessage(START_WIFI_SACN);
            mHandler.sendEmptyMessage(LISTVIEW_MOVE_TO_TOP);
        }
    };

    final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_LIST:
                    updateWifiCfgs();
                    createNetListView();
                    break;
                case WIFI_DISCONNECT:
                    startCntWifi = null;
                    disconnectWifi();
                    break;
                case WIFI_ENABLE:
                    wifiStateChangeViewUpdate(true);
                    break;
                case WIFI_UNABLE:
                    wifiStateChangeViewUpdate(false);
                    break;
                case START_WIFI_SACN:
                    startWifiScan();
                    break;
                case UPDATE_CURRENT_WIFI:
                    updateCurrentWifi();
                    break;
                case LISTVIEW_MOVE_TO_TOP:
                    listViewMove2Top();
                    break;
                default:
                    return false;
            }
            return true;
        }
    });

    public WifiDialog(Context context) {
        super(context);
        initWifiInfo();
    }

    public void setPhoneStatusBar(PhoneStatusBar phoneStatusBar) {
        mPhoneStatusBar = phoneStatusBar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWifiInfo();
    }

    private void initWifiInfo() {
        netManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        updateWifiCfgs();
    }

    private void initReceiver() {
        if (mWifiCheckEnableReceiver == null) {
            mWifiCheckEnableReceiver = new WifiCheckEnableReceiver();
        }
        if (mWifiScanReceiver == null) {
            mWifiScanReceiver = new WifiScanReceiver();
        }
    }

    @Override
    protected void initViews() {
        LinearLayout wifiRootView = (LinearLayout) LayoutInflater.from(mContext)
                                                                      .inflate(R.layout.wifi_list,
                                                                               null);
        container = (LinearLayout) wifiRootView.findViewById(R.id.net_list_container);
        netListView = new ListView(mContext);
        netListView.setItemsCanFocus(false);
        netListView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        netListView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                             ViewGroup.LayoutParams.WRAP_CONTENT));
        netListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                                          int position, long id) {
                ScanResult scanResult = netList.get(position);
                waitConnectWifi = scanResult.SSID;
                mHandler.sendEmptyMessage(UPDATE_LIST);
            }
        });
        netList = new ArrayList<>();
        createNetList();
        mNetInfoAdapter = new NetInfoAdapter(netList);
        netListView.setAdapter(mNetInfoAdapter);
        container.addView(netListView);
        setContentView(wifiRootView);
        mContentView = wifiRootView;
        netEnable = (TextView) wifiRootView.findViewById(R.id.wifi_enable);
        mEthnetSetLl = (LinearLayout) wifiRootView.findViewById(R.id.ethernet_set_dg_list);
        netEnableBtn = (Switch) wifiRootView.findViewById(R.id.wifi_enable_btn);
        if (wifiIsEnable()) {
            wifiStateChangeViewUpdate(true);
        } else {
            wifiStateChangeViewUpdate(false);
        }
        netEnableBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!netManager.isWifiEnabled()) {
                        netManager.setWifiEnabled(true);
                    }
                } else {
                    if (netManager.isWifiEnabled()) {
                        netManager.setWifiEnabled(false);
                    }
                }
                netEnableBtn.setClickable(false);
            }
        });
        mEthnetSetLl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName (PACKAGENAME_COMPONENTNAME,
                                                       CLASSNAME_COMPONENTNAME));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    private boolean wifiIsEnable() {
        return netManager.isWifiEnabled();
    }

    private void registerWifiReceiver() {
        initReceiver();
        mContext.registerReceiver(mWifiCheckEnableReceiver,
                        new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        mContext.registerReceiver(mWifiScanReceiver,
                        new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void unRegisterWifiReceiver() {
        if (mWifiCheckEnableReceiver != null) {
            mContext.unregisterReceiver(mWifiCheckEnableReceiver);
        }
        if (mWifiScanReceiver != null) {
            mContext.unregisterReceiver(mWifiScanReceiver);
        }
    }

    private boolean hasPassword(ScanResult scanResult) {
        return scanResult.capabilities.contains("WPA") || scanResult.capabilities.contains("WPA2")
               || scanResult.capabilities.contains("WEP");
    }

    private synchronized boolean createNetList() {

        currentNet = netManager.getConnectionInfo();
        List<ScanResult> tmp = new ArrayList<>();
        HashSet<String> ssidSet = new HashSet<>();
        for (ScanResult result : netManager.getScanResults()) {
            if (!ssidSet.contains(result.SSID) && !TextUtils.isEmpty(result.SSID)) {
                tmp.add(result);
                ssidSet.add(result.SSID);
            }
        }
        Collections.sort(tmp, new NetLevel());
        netList.clear();
        netList.addAll(tmp);
        mHandler.sendEmptyMessage(UPDATE_LIST);
        return true;
    }

    private synchronized boolean updateCurrentWifi(){
        currentNet = netManager.getConnectionInfo();
        return true;
    }

    private synchronized void createNetListView() {
        if (mNetInfoAdapter != null){
            mNetInfoAdapter.notifyDataSetChanged();
        }
    }
    private void listViewMove2Top(){
        netListView.setSelectionAfterHeaderView();
    }
    private void wifiStateChangeViewUpdate(boolean enable) {
        netEnableBtn.setClickable(true);
        if (enable) {
            netEnableBtn.setChecked(true);
            netEnable.setText(mContext.getText(R.string.wifi_enable));
            container.setVisibility(View.VISIBLE);
            netListView.setVisibility(View.VISIBLE);
        } else {
            netEnableBtn.setChecked(false);
            netEnable.setText(mContext.getText(R.string.wifi_unable));
            container.setVisibility(View.GONE);
            netListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerWifiReceiver();
        waitConnectWifi = null;
        waitInputPswWifi = null;
        startCntWifi = null;
        mHandler.sendEmptyMessage(START_WIFI_SACN);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterWifiReceiver();
    }

    @Override
    public void show(View v) {
        super.show(v);
    }

    private void startWifiScan() {
        netManager.startScan();
    }

    public void disconnectWifi() {
        netManager.disconnect();
    }

    private void tryCntNet(ScanResult scanRs, String psw) {

        WifiConnector cnt = new WifiConnector(mContext, wifiCntListener);
        if (scanRs.capabilities.contains("WEP")) {
            cnt.connect(scanRs.SSID, psw, WifiConnector.SecurityMode.WEP);
        } else if (scanRs.capabilities.contains("WPA2")) {
            cnt.connect(scanRs.SSID, psw, WifiConnector.SecurityMode.WPA2);
        } else if (scanRs.capabilities.contains("WPA")) {
            cnt.connect(scanRs.SSID, psw, WifiConnector.SecurityMode.WPA);
        } else {
            cnt.connect(scanRs.SSID, psw, WifiConnector.SecurityMode.OPEN);
        }
    }

    private void tryCntNetAuto(WifiConfiguration wifiConfiguration) {
        WifiConnector cnt = new WifiConnector(mContext, wifiCntListener);
        cnt.connect(wifiConfiguration);
    }

    private void updateWifiCfgs() {
        wifiCfgList = netManager.getConfiguredNetworks();
    }

    private boolean extendEquals(String s1, String s2) {
        return s1.equals(s2) || s2.replace("\"", "").equals(s1) || s1.replace("\"", "").equals(s2);
    }

    private WifiConfiguration cfgsContained(ScanResult scanResult) {
        if (wifiCfgList == null) {
            updateWifiCfgs();
        }
        for (WifiConfiguration cfg : wifiCfgList) {
            if (extendEquals(cfg.SSID, scanResult.SSID)) {
                return cfg;
            }
        }
        return null;
    }

    private class NetInfoAdapter extends ArrayAdapter<ScanResult> {

        public NetInfoAdapter(List<ScanResult> scanResults) {
            super(mContext, 0, scanResults);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ScanResult scanResult = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.wifi_content, null);
                ((WifiContentView) convertView).setPhoneStatusBar(mPhoneStatusBar);
            }
            convertView.setBackgroundColor(mContext.getResources()
                                                        .getColor(android.R.color.transparent));
            LinearLayout linearLayout = (LinearLayout) convertView;
            LinearLayout content_base = (LinearLayout) linearLayout.findViewById(
                                                                        R.id.wifi_content_main);
            FrameLayout content_sub = (FrameLayout) linearLayout.findViewById(
                                                                     R.id.wifi_content_sub);
            final Button subConnectBtn = (Button) content_sub.findViewById(
                                                                  R.id.wifi_content_cnt_btn);
            final LinearLayout wifiInputPanel = (LinearLayout) content_sub.findViewById(
                                                                    R.id.wifi_content_input_panel);
            final TextView startCnting = (TextView) content_sub.findViewById(
                                                                   R.id.wifi_connecting);
            content_sub.setVisibility(View.GONE);
            subConnectBtn.setVisibility(View.GONE);
            wifiInputPanel.setVisibility(View.GONE);
            startCnting.setVisibility(View.GONE);
            //if (waitConnectWifi != null && waitConnectWifi.equals(scanResult.SSID)) {
            //    convertView.setBackgroundColor(mContext.getResources()
            //                                                .getColor(R.color.transparent_white));
            //    content_sub.setVisibility(View.VISIBLE);
            //    subConnectBtn.setVisibility(View.VISIBLE);
            //    if (extendEquals(currentNet.getSSID(), scanResult.SSID)) {
            //        subConnectBtn.setText(mContext.getText(R.string.wifi_discnt));
            //        subConnectBtn.setOnClickListener(new View.OnClickListener() {
            //            @Override
            //            public void onClick(View v) {
            //                mHandler.sendEmptyMessage(WIFI_DISCONNECT);
            //                mHandler.sendEmptyMessage(UPDATE_LIST);
            //            }
            //        });
            //    } else {
            //        subConnectBtn.setText(mContext.getText(R.string.wifi_try_cnt));
            //        subConnectBtn.setOnClickListener(new View.OnClickListener() {
            //            @Override
            //            public void onClick(View v) {
            //                WifiConfiguration cfg = cfgsContained(scanResult);
            //                waitConnectWifi = null;
            //                waitInputPswWifi = null;
            //                if (cfg != null) {
            //                    startCntWifi = scanResult.SSID;
            //                    mHandler.sendEmptyMessage(UPDATE_LIST);
            //                    tryCntNetAuto(cfg);
            //                } else if (hasPassword(scanResult)) {
            //                    waitInputPswWifi = scanResult.SSID;
            //                    startCntWifi = null;
            //                    mHandler.sendEmptyMessage(UPDATE_LIST);
            //                } else {
            //                    startCntWifi = scanResult.SSID;
            //                    mHandler.sendEmptyMessage(UPDATE_LIST);
            //                    tryCntNet(scanResult, null);
            //                }
            //            }
            //        });
            //    }
            //} else if (waitInputPswWifi != null && waitInputPswWifi.equals(scanResult.SSID)) {
            //    convertView.setBackgroundColor(mContext.getResources()
            //                                               .getColor(R.color.transparent_white));
            //    content_sub.setVisibility(View.VISIBLE);
            //    wifiInputPanel.setVisibility(View.VISIBLE);
            //    Button cancel = (Button) wifiInputPanel.findViewById(R.id.wifi_dialog_cancel);
            //    Button connect = (Button) wifiInputPanel.findViewById(R.id.wifi_dialog_ok);
            //    final EditText psw = (EditText) wifiInputPanel.findViewById(
            //                                                       R.id.wifi_dialog_input);
            //    psw.setFocusable(true);
            //    psw.requestFocusFromTouch();
            //    cancel.setOnClickListener(new View.OnClickListener() {
            //        @Override
            //        public void onClick(View v) {
            //            waitConnectWifi = null;
            //            waitInputPswWifi = null;
            //            mHandler.sendEmptyMessage(UPDATE_LIST);
            //        }
            //    });
            //    connect.setOnClickListener(new View.OnClickListener() {
            //        @Override
            //        public void onClick(View v) {
            //            waitConnectWifi = null;
            //            waitInputPswWifi = null;
            //            startCntWifi = scanResult.SSID;
            //            mHandler.sendEmptyMessage(UPDATE_LIST);
            //            tryCntNet(scanResult, psw.getText().toString());
            //        }
            //    });
            //} else if (startCntWifi != null && startCntWifi.equals(scanResult.SSID)) {
            //    convertView.setBackgroundColor(mContext.getResources()
            //                                                .getColor(R.color.transparent_white));
            //    content_sub.setVisibility(View.VISIBLE);
            //    startCnting.setVisibility(View.VISIBLE);
            //}
            ImageView imageView = (ImageView) linearLayout
                                                  .findViewById(R.id.wifi_content_is_cnted);
            imageView.setImageDrawable(null);
            if (currentNet != null &&
                    scanResult.SSID.equals(currentNet.getSSID().replace("\"", ""))) {
                imageView.setImageDrawable(
                              mContext.getDrawable(android.R.drawable.checkbox_on_background));
            }
            TextView textView = (TextView) linearLayout.findViewById(R.id.wifi_content_name);
            textView.setText(scanResult.SSID);

            ImageView hasPWD = (ImageView) linearLayout.findViewById(R.id.wifi_has_psw);
            if (hasPassword(scanResult)) {
                hasPWD.setImageDrawable(mContext.getDrawable(R.drawable.wifi_locked));
            } else {
                hasPWD.setImageDrawable(mContext.getDrawable(android.R.drawable.ic_dialog_alert));
            }
            ImageView imageViewWifi = (ImageView)linearLayout
                                                     .findViewById(R.id.wifi_content_level);
            if (scanResult.level >= -55) {
                imageViewWifi.setImageDrawable(mContext.getDrawable(R.drawable.wifi_level_4));
            } else if (scanResult.level >= -65) {
                imageViewWifi.setImageDrawable(mContext.getDrawable(R.drawable.wifi_level_3));
            } else if (scanResult.level >= -75) {
                imageViewWifi.setImageDrawable(mContext.getDrawable(R.drawable.wifi_level_2));
            } else {
                imageViewWifi.setImageDrawable(mContext.getDrawable(R.drawable.wifi_level_1));
            }
            return convertView;
        }

    }

    private class NetLevel implements Comparator<ScanResult> {

        @Override
        public int compare(ScanResult lhs, ScanResult rhs) {
            if (lhs.SSID.equals(currentNet.getSSID().replace("\"", ""))) {
                return -1;
            }
            if (rhs.SSID.equals(currentNet.getSSID().replace("\"", ""))) {
                return 1;
            }
            return rhs.level - lhs.level;
        }
    }

    private class WifiCheckEnableReceiver extends BroadcastReceiver {
        public WifiCheckEnableReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            switch (state) {
                case WifiManager.WIFI_STATE_DISABLED:
                    mHandler.sendEmptyMessage(WIFI_UNABLE);
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    mHandler.sendEmptyMessage(WIFI_ENABLE);
                    break;
                default:
                    netEnableBtn.setClickable(false);
                    break;
            }
        }
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            createNetList();
        }
    }
}
