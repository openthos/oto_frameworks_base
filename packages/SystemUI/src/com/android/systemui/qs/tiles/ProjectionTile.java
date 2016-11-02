package com.android.systemui.qs.tiles;

import android.view.KeyEvent;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.statusbar.phone.QSTileHost;

public class ProjectionTile extends QSTile<QSTile.BooleanState> {
    private static final String TAG = "ProjectionTile";
    private QSTileHost mHost;
    private Boolean mBoolean = false;
    private BooleanState mBooleanState;

    public ProjectionTile(Host host) {
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
        sendInfoPopup();
        mBoolean = true;
        mBooleanState.icon = ResourceIcon.get(R.drawable.ic_notification_projection_on);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        mBooleanState = state;
        state.label = mContext.getResources().getString(R.string.quick_settings_projection_label);
        state.visible = true;
        state.value = true;
        if (mBoolean) {
            state.icon = ResourceIcon.get(R.drawable.ic_notification_projection_on);
        } else {
            state.icon = ResourceIcon.get(R.drawable.ic_notification_projection_off);
        }
    }

    public void sendInfoPopup() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_NOTIFICATION_PROJECTION);
        mContext.sendBroadcast(intent);
    }
}
