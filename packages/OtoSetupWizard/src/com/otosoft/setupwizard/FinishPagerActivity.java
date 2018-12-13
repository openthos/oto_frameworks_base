package com.otosoft.setupwizard;

import android.content.Intent;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.app.LocalePicker;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;

import org.openthos.seafile.ISeafileService;

public class FinishPagerActivity extends BaseActivity {
    private Button mButtonStart;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_pager);

        this.mButtonStart = (Button) findViewById(R.id.button_start_use);
        mButtonStart.setPadding(1,1,1,1);
        mButtonStart.getBackground().setAlpha(25);
        this.mButtonStart.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                openDesktop();
                try {
                    ISeafileService iSeafileService
                            = ((SetupWizardApplication) getApplication()).mISeafileService;
                    String userName = iSeafileService.getUserName();
                    //if (!TextUtils.isEmpty(userName)) {
                    //    AlertDialog.Builder builder
                    //            = new AlertDialog.Builder(FinishPagerActivity.this);
                    //    builder.setTitle(getString(R.string.title_open_recoverui));
                    //    builder.setPositiveButton(getString(R.string.warning_dialog_ok),
                    //            new DialogInterface.OnClickListener() {
                    //        @Override
                    //        public void onClick(DialogInterface dialog, int which) {
                    //            dialog.dismiss();
                    //            openDesktop();
                    //            Intent intent = new Intent();
                    //            intent.setComponent(new ComponentName("org.openthos.seafile",
                    //                    "org.openthos.seafile.RecoveryActivity"));
                    //            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //            startActivity(intent);
                    //        }
                    //    });

                    //    builder.setNegativeButton(getString(R.string.warning_dialog_cancel),
                    //            new DialogInterface.OnClickListener() {
                    //        @Override
                    //        public void onClick(DialogInterface dialog, int which) {
                    //            dialog.dismiss();
                    //            openDesktop();
                    //        }
                    //    });
                    //    builder.setCancelable(false);
                    //    builder.create().show();
                    //} else {
                    //    openDesktop();
                    //}
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void openDesktop() {
        Intent intent = new Intent();
        intent.setAction(Intent.STATUS_BAR_SHOW_FINISH_ACTIVITY);
        FinishPagerActivity.this.sendBroadcast(intent);
        ((SetupWizardApplication) getApplication()).onSetupFinishedReally();
    }

    public void onResume() {
        super.onResume();
    }
}
