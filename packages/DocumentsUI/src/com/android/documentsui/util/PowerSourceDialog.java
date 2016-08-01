package com.android.documentsui.util;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.android.documentsui.R;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.IOException;
import android.graphics.Color;

import android.app.Activity;
import android.provider.Settings;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class PowerSourceDialog extends BaseSettingDialog implements OnClickListener {

    public static final int COLOR_VIEW_FOCUS = Color.parseColor("#2b1f52");

    public PowerSourceDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViews() {
        final AudioManager audioManager = (AudioManager) mContext.getSystemService(
                                                                      Context.AUDIO_SERVICE);
        View mediaView = LayoutInflater.from(mContext)
                                            .inflate(R.layout.power_source_activity, null);
        setContentView(mediaView);
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = LayoutParams.FILL_PARENT;
        layoutParams.height = layoutParams.FILL_PARENT;
        layoutParams.alpha = 0.5f;
        window.setAttributes(layoutParams);
        mContentView = mediaView;
        ImageView powerClose = (ImageView) mediaView.findViewById(R.id.power_close);
        LinearLayout powerOff = (LinearLayout) mediaView.findViewById(R.id.power_off);
        LinearLayout powerSleep = (LinearLayout) mediaView.findViewById(R.id.power_sleep);
        LinearLayout powerLock = (LinearLayout) mediaView.findViewById(R.id.power_lock);
        LinearLayout powerRestart = (LinearLayout) mediaView.findViewById(R.id.power_restart);
        powerClose.setOnClickListener(this);
        powerOff.setOnClickListener(this);
        powerSleep.setOnClickListener(this);
        powerLock.setOnClickListener(this);
        powerRestart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.power_close:
               PowerSourceDialog.this.dismiss();
            break;
            case R.id.power_off:
                Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
                intent.putExtra(Intent.EXTRA_KEY_CONFIRM, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_RUN_FULLSCREEN);
                mContext.startActivity(intent);
                break;
            case R.id.power_sleep:
                try {
                    String cmd = "/system/xbin/echo mem > /sys/power/state";
                    Runtime.getRuntime().exec(new String[] {"/system/bin/su", "-c", cmd});
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.power_lock:
                PowerSourceDialog.this.dismiss();
                Intent intentLock = new Intent("android.intent.action.LOCKNOW");
                intentLock.addFlags(Intent.FLAG_RUN_FULLSCREEN | Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(intentLock);
                break;
            case R.id.power_restart:
                try {
                    Runtime.getRuntime().exec("su -c \"/system/bin/reboot\"");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

        }
    }
}
