package com.android.systemui.statusbar.notificationbars;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.systemui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.text.TextUtils;
import android.content.ComponentName;

/**
 * Created by Mingkai on 2016/6/22.
 */
public final class WifiDialog extends BaseSettingDialog {
    private final String CLASSNAME_ETHERNET_SETTINGS =
            "com.android.settings.ethernet.EthernetSettings";
    private final String PACKAGENAME_SETTING = "com.android.settings";
    private final String TAG = "umic";

    private static final int MAX_DISPLAY_NUM = 13;

    private static final int UPDATE_LIST = 1;
    private static final int WIFI_ENABLE = 2;
    private static final int WIFI_UNABLE = 3;

    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;

    private ListView mWifiListView;
    private Switch mWifiEnableBtn;
    private TextView mWifiEnable;
    private TextView mEthernetConfigure;

    private List<ScanResult> mScanResults;
    private NetInfoAdapter mNetInfoAdapter;
    private int mMaxListViewHeight;

    final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_LIST:
                    initScanResults();
                    break;
                case WIFI_ENABLE:
                    wifiStateChangeViewUpdate(true);
                    mWifiManager.startScan();
                    break;
                case WIFI_UNABLE:
                    wifiStateChangeViewUpdate(false);
                    break;
                default:
                    return false;
            }
            return true;
        }
    });
    private LinearLayout mWifiEnableLayout;
    private LinearLayout mLayout;

    public WifiDialog(Context context) {
        super(context);
    }

    @Override
    protected void initViews() {
        mContentView = LayoutInflater.from(mContext).inflate(R.layout.wifi_list, null);
        mWifiListView = (ListView) mContentView.findViewById(R.id.wifi_list);
        mWifiEnableLayout = (LinearLayout) mContentView.findViewById(R.id.wifi_enable_layout);
        mLayout = (LinearLayout) mContentView.findViewById(R.id.wifi_layout);
        mWifiEnable = (TextView) mContentView.findViewById(R.id.wifi_enable);
        mWifiEnableBtn = (Switch) mContentView.findViewById(R.id.wifi_enable_btn);
        mEthernetConfigure = (TextView) mContentView.findViewById(R.id.ethernet_configure);

        mWifiEnableLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.EXACTLY);
        mLayout.setLayoutParams(new LinearLayout.LayoutParams(
                mWifiEnableLayout.getMeasuredWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
        mMaxListViewHeight =
                mContext.getResources().getDimensionPixelOffset(R.dimen.systemui_size_fourhundred);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mScanResults = new ArrayList<>();
        mNetInfoAdapter = new NetInfoAdapter();
        mWifiListView.setAdapter(mNetInfoAdapter);
        setContentView(mContentView);

        mWifiEnableBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!mWifiManager.isWifiEnabled()) {
                        mWifiManager.setWifiEnabled(true);
                    }
                } else {
                    if (mWifiManager.isWifiEnabled()) {
                        mWifiManager.setWifiEnabled(false);
                    }
                }
            }
        });
        mEthernetConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(PACKAGENAME_SETTING,
                        CLASSNAME_ETHERNET_SETTINGS));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    private void initScanResults() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        mScanResults.clear();
        Set<String> set = new HashSet<>();
        for (ScanResult result : mWifiManager.getScanResults()) {
            if (!TextUtils.isEmpty(result.SSID) && !set.contains(result.SSID)) {
                mScanResults.add(result);
                set.add(result.SSID);
            }
        }
        Collections.sort(mScanResults, new NetLevel());
        mNetInfoAdapter.notifyDataSetChanged();
        ViewGroup.LayoutParams params = mWifiListView.getLayoutParams();
        params.width = mWifiEnableLayout.getMeasuredWidth();
        if (mScanResults.size() > MAX_DISPLAY_NUM) {
            params.height = mMaxListViewHeight;
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        mWifiListView.setLayoutParams(params);
    }

    private void wifiStateChangeViewUpdate(boolean enable) {
        mWifiEnableBtn.setChecked(enable);
        if (enable) {
            mWifiEnable.setText(mContext.getText(R.string.wifi_enable));
            mWifiListView.setVisibility(View.VISIBLE);
        } else {
            mWifiEnable.setText(mContext.getText(R.string.wifi_unable));
            mWifiListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        wifiStateChangeViewUpdate(mWifiManager.isWifiEnabled());
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.startScan();
            initScanResults();
        }
    }

    @Override
    public void show(View v) {
        super.show(v);
    }

    private class NetInfoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mScanResults.size();
        }

        @Override
        public Object getItem(int position) {
            return mScanResults.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.wifi_content, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ScanResult scanResult = mScanResults.get(position);
            if (mWifiInfo != null
                    && scanResult.SSID.equals(mWifiInfo.getSSID().replace("\"", ""))) {
                holder.wifiConnect.setImageDrawable(
                        mContext.getDrawable(android.R.drawable.checkbox_on_background));
            } else {
                holder.wifiConnect.setImageDrawable(null);
            }
            holder.wifiName.setText(scanResult.SSID);
            if (hasPassword(scanResult)) {
                holder.wifiHasPWD.setImageDrawable(mContext.getDrawable(R.drawable.wifi_locked));
            } else {
                holder.wifiHasPWD.setImageDrawable(
                        mContext.getDrawable(android.R.drawable.ic_dialog_alert));
            }
            if (scanResult.level >= -55) {
                holder.wifiLevel.setImageDrawable(mContext.getDrawable(R.drawable.wifi_level_4));
            } else if (scanResult.level >= -65) {
                holder.wifiLevel.setImageDrawable(mContext.getDrawable(R.drawable.wifi_level_3));
            } else if (scanResult.level >= -75) {
                holder.wifiLevel.setImageDrawable(mContext.getDrawable(R.drawable.wifi_level_2));
            } else {
                holder.wifiLevel.setImageDrawable(mContext.getDrawable(R.drawable.wifi_level_1));
            }
            return convertView;
        }

        private boolean hasPassword(ScanResult scanResult) {
            return scanResult.capabilities.contains("WPA")
                    || scanResult.capabilities.contains("WPA2")
                    || scanResult.capabilities.contains("WEP");
        }

        private class ViewHolder {
            private ImageView wifiConnect;
            private TextView wifiName;
            private ImageView wifiHasPWD;
            private ImageView wifiLevel;

            public ViewHolder(View view) {
                wifiConnect = (ImageView) view.findViewById(R.id.wifi_content_is_cnted);
                wifiName = (TextView) view.findViewById(R.id.wifi_content_name);
                wifiHasPWD = (ImageView) view.findViewById(R.id.wifi_has_psw);
                wifiLevel = (ImageView) view.findViewById(R.id.wifi_content_level);
            }
        }
    }

    private class NetLevel implements Comparator<ScanResult> {

        @Override
        public int compare(ScanResult lhs, ScanResult rhs) {
            if (lhs.SSID.equals(mWifiInfo.getSSID().replace("\"", ""))) {
                return -1;
            }
            if (rhs.SSID.equals(mWifiInfo.getSSID().replace("\"", ""))) {
                return 1;
            }
            return rhs.level - lhs.level;
        }
    }

    public void updateWifiEnabled(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_DISABLED:
                mHandler.sendEmptyMessage(WIFI_UNABLE);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                mHandler.sendEmptyMessage(WIFI_ENABLE);
                break;
            default:
                break;
        }
    }

    public void updateList() {
        mHandler.sendEmptyMessage(UPDATE_LIST);
    }
}
