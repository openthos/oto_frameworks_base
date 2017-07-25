package com.android.systemui.statusbar.notificationbars;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import com.android.systemui.R;

/**
 * Created by Mingkai on 2016/6/22.
 */
public class VolumeDialog extends BaseSettingDialog implements SeekBar.OnSeekBarChangeListener {
    private VerticalSeekBar mediaVolume;
    private ImageView volView;

    public VolumeDialog(Context context) {
        super(context);
    }

    final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int progress = msg.what;
            final AudioManager audioManager = (AudioManager) mContext.getSystemService(
                                                                          Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                         progress, AudioManager.FLAG_PLAY_SOUND );
            mediaVolume.setProgress(progress);
            if (progress > 0) {
                volView.setImageDrawable(mContext.getDrawable(
                                                      android.R.drawable.ic_lock_silent_mode_off));
            } else {
                volView.setImageDrawable(mContext.getDrawable(
                                                      android.R.drawable.ic_lock_silent_mode));
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        handler.sendEmptyMessage(progress);
    }

    @Override
    public void show(View v) {
        super.show(v);
        final AudioManager audioManager = (AudioManager) mContext.getSystemService(
                                                                      Context.AUDIO_SERVICE);
        int vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mediaVolume.setProgress(vol);
        if (vol==0) {
            volView.setImageDrawable(mContext.getDrawable(android.R.drawable.ic_lock_silent_mode));
        }
        else {
            volView.setImageDrawable(mContext.getDrawable(
                                                  android.R.drawable.ic_lock_silent_mode_off));
        }
    }

    @Override
    protected void initViews() {
        final AudioManager audioManager = (AudioManager) mContext.getSystemService(
                                                                      Context.AUDIO_SERVICE);
        View mediaView = LayoutInflater.from(mContext).inflate(R.layout.ringer_volume, null);
        setContentView(mediaView);
        mediaVolume = (VerticalSeekBar) mediaView.findViewById(R.id.media_volume_seekbar);
        mediaVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mediaVolume.setOnSeekBarChangeListener(this);
        volView = (ImageView) mediaView.findViewById(R.id.media_volume_btn);
        volView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volView.setImageDrawable(mContext.getDrawable(
                                                      android.R.drawable.ic_lock_silent_mode));
                handler.sendEmptyMessage(0);
            }
        });
        mContentView = mediaView;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
