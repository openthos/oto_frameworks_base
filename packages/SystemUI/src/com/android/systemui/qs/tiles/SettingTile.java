package com.android.systemui.qs.tiles;

import android.hardware.input.InputManager;
import android.view.KeyEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.provider.Settings;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.statusbar.phone.QSTileHost;

public class SettingTile extends QSTile<QSTile.BooleanState> {
    private static final String TAG = "SettingTile";
    private QSTileHost mHost;

    public SettingTile(Host host) {
        super(host);
        mHost = (QSTileHost) host;
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {

    }

    @Override
    protected void handleClick() {
        //mHost.mStatusBar.mStatusBarView.collapseAllPanels(true);
        //mHost.mStatusBar.makeExpandedInvisible();
        if (android.os.Build.VERSION.SDK_INT > 13) {
            mContext.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } else {
            mContext.startActivity(new Intent(android.provider.Settings.ACTION_APN_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
        //mHost.mStatusBar.makeExpandedInvisible();
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label = mContext.getResources().getString(R.string.quick_settings_settings_label);
        state.visible = true;
        state.value = true;
        state.icon = ResourceIcon.get(R.drawable.ic_notification_setting);
    }
}
