package com.android.systemui.qs.tiles;

import android.view.KeyEvent;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnAttachStateChangeListener;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.ScreenshotController;

public class ScreenshotTile extends QSTile<QSTile.BooleanState> {
    private static final String TAG = "ScreenshotTile";

    //private static final boolean DEBUG = true;
    //private final ScreenshotController mController=null;
    //private boolean mListening;
    //private int mDataState = R.drawable.ic_qs_inversion_off;

    public ScreenshotTile(Host host) {
        super(host);
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
        //Instrumentation mInst = new Instrumentation();
        //mInst.sendKeyDownUpSync(KeyEvent.KEYCODE_CAMERA);

        Instrumentation mInst = new Instrumentation();
        mInst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_POWER));
        mInst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_VOLUME_DOWN));

        mInst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_VOLUME_DOWN));
        mInst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_POWER));
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label ="Screenshot";
        state.visible = true;
        //state.iconId = mDataState;

        //Instrumentation mInst = new Instrumentation();
        //mInst.sendKeyDownUpSync(KeyEvent.KEYCODE_CAMERA);
    }
}
