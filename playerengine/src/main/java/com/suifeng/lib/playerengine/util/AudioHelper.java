package com.suifeng.lib.playerengine.util;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.suifeng.lib.playerengine.api.PlayerEngine;

import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
import static android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

/**
 * audio helper
 */

public class AudioHelper implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "AudioHelper";

    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private ComponentName componentName;
    private PlayerEngine mPlayerEngine;
    private boolean pauseFromUser;
    private TelephonyManager telephonyManager;
    private MobilePhoneStateListener phoneStateListener;

    public AudioHelper(Context context, PlayerEngine playerEngine, Class<? extends BroadcastReceiver> cls) {
        this(context, null);
        mPlayerEngine = playerEngine;
        componentName = new ComponentName(context.getPackageName(), cls.getName());
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new MobilePhoneStateListener();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public AudioHelper(Context context, AudioManager.OnAudioFocusChangeListener listener, Class<? extends BroadcastReceiver> cls) {
        this(context, listener);
        componentName = new ComponentName(context.getPackageName(), cls.getName());
    }

    public AudioHelper(Context context, AudioManager.OnAudioFocusChangeListener listener) {
        this.audioFocusChangeListener = listener;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setIsUserPause(boolean fromUser) {
        this.pauseFromUser = fromUser;
    }

    public boolean requestFocus() {
        return 1 == mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public boolean abandonFocus() {
        return 1 == mAudioManager.abandonAudioFocus(this);
    }

    public void registerMediaButtonEventReceiver() {
        mAudioManager.registerMediaButtonEventReceiver(componentName);
    }

    public void unRegisterMediaButtonEventReceiver() {
        mAudioManager.unregisterMediaButtonEventReceiver(componentName);
    }

    public void destroy() {
        if(telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        unRegisterMediaButtonEventReceiver();
        abandonFocus();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if(audioFocusChangeListener != null) {
            audioFocusChangeListener.onAudioFocusChange(focusChange);
        }
        if(mPlayerEngine != null) {
            switch (focusChange) {
                case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    if(mPlayerEngine.isPlaying()) {
                        mPlayerEngine.setVolume(0.1F, 0.1F);
                    }
                    break;
                case AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    if(mPlayerEngine.isPlaying()) {
                        mPlayerEngine.pause();
                    }
                    break;
                case AUDIOFOCUS_LOSS:
                    Log.d(TAG, "AUDIOFOCUS_LOSS");
                    if(mPlayerEngine.isPlaying() && !pauseFromUser) {
                        mPlayerEngine.pause();
                    }
                    break;
                case AUDIOFOCUS_REQUEST_GRANTED:
                    Log.d(TAG, "AUDIOFOCUS_REQUEST_GRANTED");
                    if(!mPlayerEngine.isPlaying() && !pauseFromUser) {
                        mPlayerEngine.resume();
                    }
                    registerMediaButtonEventReceiver();
                    mPlayerEngine.setVolume(1.0F, 1.0F);
                    break;
            }
        }
    }

    private class MobilePhoneStateListener extends PhoneStateListener {
        private MobilePhoneStateListener(){}

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "CALL_STATE_IDLE");
                    if(mPlayerEngine != null && !mPlayerEngine.isPlaying() && !pauseFromUser) {
                        mPlayerEngine.resume();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "CALL_STATE_OFFHOOK");
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "CALL_STATE_RINGING");
                    if(mPlayerEngine != null && mPlayerEngine.isPlaying()) {
                        mPlayerEngine.pause();
                    }
                    break;
            }
        }
    }
}
