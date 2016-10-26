package com.android.systemui.qs.tiles;

import android.view.KeyEvent;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.content.BroadcastReceiver;
import com.android.systemui.qs.GlobalSetting;
import android.provider.Settings.Global;
import android.net.ConnectivityManager;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.statusbar.phone.QSTileHost;

public class IsolationModeTile extends QSTile<QSTile.BooleanState> {
    private QSTileHost mHost;
    private boolean mListening;
    private final GlobalSetting mSetting;

    public IsolationModeTile(Host host) {
        super(host);
        mSetting = new GlobalSetting(mContext, mHandler, Global.AIRPLANE_MODE_ON) {
            @Override
            protected void handleValueChanged(int value) {
                handleRefreshState(value);
            }
        };
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        setEnabled(!mState.value);
        handleUpdateState(mState, null);
    }

    private void setEnabled(boolean enabled) {
        final ConnectivityManager mgr = (ConnectivityManager) mContext
                                     .getSystemService(Context.CONNECTIVITY_SERVICE);
        mgr.setAirplaneMode(enabled);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final int value = arg instanceof Integer ? (Integer)arg : mSetting.getValue();
        final boolean isolationMode = value != 0;
        state.value = isolationMode;
        state.visible = true;
        state.label = mContext.getResources().getString(
                      R.string.quick_settings_isolation_mode_label);
        if (isolationMode) {
            state.icon = ResourceIcon.get(R.drawable.ic_notification_isolation_on);
            state.contentDescription =  mContext.getString(
                      R.string.accessibility_quick_settings_airplane_on);
        } else {
            state.icon = ResourceIcon.get(R.drawable.ic_notification_isolation_off);
            state.contentDescription =  mContext.getString(
                      R.string.accessibility_quick_settings_airplane_off);
        }
    }

    @Override
    public void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        if (listening) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            mContext.registerReceiver(mReceiver, filter);
        } else {
            mContext.unregisterReceiver(mReceiver);
        }
        mSetting.setListening(listening);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                refreshState();
            }
        }
    };
}
