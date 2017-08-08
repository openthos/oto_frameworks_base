package com.android.systemui.statusbar.notificationbars;

import com.android.systemui.R;
import android.content.SharedPreferences;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

/**
 * Created by Mingkai on 2016/6/22.
 */
public class VolumeDialog extends BaseSettingDialog implements SeekBar.OnSeekBarChangeListener {
    private final static String VOL_KEY = "VOLUME_KEY";
    private final static int DEFAULT_VALUE = 5;
    private SharedPreferences.Editor mEditor;
    private VerticalSeekBar mVerticalSeekBar;
    private AudioManager mAudioManager;
    private ImageView mVolView;
    private int mTempValue = DEFAULT_VALUE;
    private int mVol = 0;

    public VolumeDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                         progress, AudioManager.FLAG_PLAY_SOUND);
        mVerticalSeekBar.setProgress(progress);
        mVolView.setImageDrawable(mContext.getDrawable(progress > 0 ?
            android.R.drawable.ic_lock_silent_mode_off : android.R.drawable.ic_lock_silent_mode));
        mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mEditor.putInt(VOL_KEY, mVol).commit();
    }

    @Override
    public void show(View v) {
        super.show(v);
        mVerticalSeekBar.setProgress(mVol);
    }

    @Override
    protected void initViews() {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        View mediaView = LayoutInflater.from(mContext).inflate(R.layout.ringer_volume, null);
        mVerticalSeekBar = (VerticalSeekBar) mediaView.findViewById(R.id.media_volume_seekbar);
        mVerticalSeekBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mVerticalSeekBar.setOnSeekBarChangeListener(this);
        mVolView = (ImageView) mediaView.findViewById(R.id.media_volume_btn);
        setContentView(mediaView);
        SharedPreferences sharePre = mContext.getSharedPreferences(VOL_KEY, Context.MODE_APPEND);
        mEditor = sharePre.edit();
        mVol = sharePre.getInt(VOL_KEY, DEFAULT_VALUE);
        if (mVol == 0) {
            mVolView.setImageDrawable(mContext.getDrawable(android.R.drawable.ic_lock_silent_mode));
        }
        mVolView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVol != 0) {
                    mTempValue = mVol;
                    mVerticalSeekBar.setProgress(0);
                } else {
                    mVerticalSeekBar.setProgress(mTempValue);
                }
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
