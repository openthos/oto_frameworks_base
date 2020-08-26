package com.android.systemui.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.android.systemui.R;


/**
 * Created by Mingkai on 2016/6/22.
 */
public class VolumeDialog extends BaseDialog implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
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
        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.ringer_volume, null);
        setContentView(mContentView);
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
        mVolView.setImageDrawable(getContext().getDrawable(progress > 0 ?
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
    public void initView() {
        mVerticalSeekBar = (VerticalSeekBar) mContentView.findViewById(R.id.media_volume_seekbar);
        mVolView = (ImageView) mContentView.findViewById(R.id.media_volume_btn);
    }

    @Override
    public void initData() {
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mVerticalSeekBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        SharedPreferences sharePre = getContext().getSharedPreferences(VOL_KEY, Context.MODE_APPEND);
        mEditor = sharePre.edit();
        mVol = sharePre.getInt(VOL_KEY, DEFAULT_VALUE);
        if (mVol == 0) {
            mVolView.setImageDrawable(getContext().getDrawable(android.R.drawable.ic_lock_silent_mode));
        }
    }

    @Override
    public void initListener() {
        mVerticalSeekBar.setOnSeekBarChangeListener(this);
        mVolView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mVol != 0) {
            mTempValue = mVol;
            mVerticalSeekBar.setProgress(0);
        } else {
            mVerticalSeekBar.setProgress(mTempValue);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
