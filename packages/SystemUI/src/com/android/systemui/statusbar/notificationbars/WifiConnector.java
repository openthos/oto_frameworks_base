package com.android.systemui.statusbar.notificationbars;
/**
 *
 */
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiConnector {

    private static final int WIFI_TIMEOUT_LIMIT = 30; //wifi connect timeout

    private Context mContext;
    private WifiManager netManager;
    private Lock mLock;
    private Condition mCondition;
    private NetConnectReceiver netConnectReceiver;
    private NetConnectListener netConnectListener;
    private boolean isConnected = false;
    private int mNetworkID = -1;
    public  enum SecurityMode {
        OPEN, WEP, WPA, WPA2
    }

    public interface NetConnectListener {
        public void OnWifiConnectCompleted( boolean isConnected );
    }

    protected class NetConnectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (!WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                return;
            }
            mLock.lock();
            WifiInfo info = netManager.getConnectionInfo();
            if (info.getNetworkId() == mNetworkID
                    && info.getSupplicantState() == SupplicantState.COMPLETED ) {
                isConnected = true;
                mCondition.signalAll();
            }
            mLock.unlock();
        }
    }

    public WifiConnector( Context context , NetConnectListener listener ) {
        mContext = context;
        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
        netManager =(WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        netConnectReceiver = new NetConnectReceiver();
        netConnectListener = listener;
    }

    public void connect( final String ssid, final String password, final SecurityMode mode ) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                if( !netManager.isWifiEnabled() ) {
                    netManager.setWifiEnabled(true);
                }

                mContext.registerReceiver(netConnectReceiver,
                                          new IntentFilter(WifiManager
                                                               .SUPPLICANT_STATE_CHANGED_ACTION));

                if( !onConnect(ssid,password,mode) ) {
                    netConnectListener.OnWifiConnectCompleted(false);
                }
                else {
                    netConnectListener.OnWifiConnectCompleted(true);
                }

                mContext.unregisterReceiver(netConnectReceiver);
            }
        }).start();
    }

    public void connect(final WifiConfiguration wifiConfiguration){
        new Thread(new Runnable() {

            @Override
            public void run() {

                if( !netManager.isWifiEnabled() ) {
                    netManager.setWifiEnabled(true);
                }

                mContext.registerReceiver(netConnectReceiver,
                                          new IntentFilter(WifiManager
                                                               .SUPPLICANT_STATE_CHANGED_ACTION));

                if( !onConnect(wifiConfiguration) ) {
                    netConnectListener.OnWifiConnectCompleted(false);
                }
                else {
                    netConnectListener.OnWifiConnectCompleted(true);
                }

                mContext.unregisterReceiver(netConnectReceiver);
            }
        }).start();
    }

    private boolean attemptConnect(WifiConfiguration cgf){
        List<WifiConfiguration> list = netManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals(cgf.SSID)) {
                netManager.disconnect();
                netManager.enableNetwork(i.networkId, true);
                netManager.reconnect();

                return true;
            }
        }
        return false;
    }

    protected synchronized boolean onConnect(WifiConfiguration cfg){
        mNetworkID = cfg.networkId;
        mLock.lock();
        isConnected = false;

        if( !attemptConnect(cfg) ) {
            mLock.unlock();
            return false;
        }

        try {
            mCondition.await(WIFI_TIMEOUT_LIMIT, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        mLock.unlock();

        return isConnected;
    }

    protected boolean onConnect( String ssid, String password, SecurityMode mode ) {

        WifiConfiguration cfg = new WifiConfiguration();
        cfg.SSID = "\"" + ssid + "\"";
        if( password !=null && !"".equals(password) ) {
            if( mode == SecurityMode.WEP ) {
                cfg.wepKeys[0] = "\"" + password + "\"";
                cfg.wepTxKeyIndex = 0;
                cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            } else {
                cfg.preSharedKey = "\"" + password + "\"";
            }
        } else {
            cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        cfg.status = WifiConfiguration.Status.ENABLED;

        mNetworkID = netManager.addNetwork(cfg);
        return onConnect(cfg);
    }
}
