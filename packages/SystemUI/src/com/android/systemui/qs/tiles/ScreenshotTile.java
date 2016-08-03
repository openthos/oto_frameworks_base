package com.android.systemui.qs.tiles;

import android.hardware.input.InputManager;
import android.view.KeyEvent;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnAttachStateChangeListener;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.ScreenshotController;
import com.android.systemui.statusbar.phone.QSTileHost;

public class ScreenshotTile extends QSTile<QSTile.BooleanState> {
    private static final String TAG = "ScreenshotTile";
    private QSTileHost mHost;

    public ScreenshotTile(Host host) {
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
        ((InputManager)mContext.getSystemService(Context.INPUT_SERVICE))
                                    .sendKeyEvent(KeyEvent.KEYCODE_SYSRQ);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label ="Screenshot";
        state.visible = true;
        state.icon = ResourceIcon.get(R.drawable.ic_notification_screenshot);
    }
}
