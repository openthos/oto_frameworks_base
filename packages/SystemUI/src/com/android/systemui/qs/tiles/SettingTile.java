package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnAttachStateChangeListener;
import com.android.internal.telephony.TelephonyIntents;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.SettingController;

public class SettingTile extends QSTile<QSTile.BooleanState> {
    private static final String TAG = "SettingTile";

    //private static final boolean DEBUG = true;
    //private final SettingController mController=null;
    //private boolean mListening;
    //private int mDataState = R.drawable.ic_qs_inversion_off;

    public SettingTile(Host host) {
        super(host);
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {

    }

    //boolean enabled =false;
    @Override
    protected void handleClick() {
        //enabled =!enabled;
        //if (enabled) {
            //mDataState = R.drawable.ic_qs_inversion_on;
        //} else {
            //mDataState = R.drawable.ic_qs_inversion_off;
        //}
        //refreshState();

        //ActivityStarter mActivityStarter = new ActivityStarter();
        //mActivityStarter.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS), true);

    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label ="Setting";
        state.visible = true;
        //state.iconId = mDataState;
    }
}
