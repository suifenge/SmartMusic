package com.suifeng.lib.playerengine.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.suifeng.lib.playerengine.api.PlaybackMode;
import com.suifeng.lib.playerengine.api.PlayerEngine;
import com.suifeng.lib.playerengine.api.PlayerListener;

import java.io.IOException;

import static android.media.MediaPlayer.MEDIA_ERROR_IO;
import static android.media.MediaPlayer.MEDIA_ERROR_MALFORMED;
import static android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED;
import static android.media.MediaPlayer.MEDIA_ERROR_TIMED_OUT;
import static android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN;
import static android.media.MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;

/**
 * base on PlayerEngine implement functions
 */

public class PlayerEngineImpl implements PlayerEngine, OnCompletionListener, OnBufferingUpdateListener, OnErrorListener{

    private static final String TAG = "PlayerEngineImpl";

    private static final long PROGRESS_UPDATE_INTERVAL = 500;
    private static final long FAIL_TIME_FRAME = 1000L;
    private static final int ACCEPTABLE_FAIL_NUMBER = 5;

    private Context mContext;
    private InternalMediaPlayer currentMediaPlayer;
    private PlayListManager playListManager;
    private PlayerListener playerListener;
    private Handler progressHandler;
    private long progressUpdateInterval = PROGRESS_UPDATE_INTERVAL;
    private boolean isFadeVolume;
    private boolean wakeMode;
    private PlaybackMode mPlaybackMode;
    private OnPlayStateChangeListener playStateListener;
    private long mLastFailTime;
    private long mTimesFailed;
    private boolean playNextWhenError = false;
    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if(currentMediaPlayer != null) {
                int currentDuration = currentMediaPlayer.getCurrentPosition();
                int duration = currentMediaPlayer.getDuration();
                if(playerListener != null) {
                    if(!currentMediaPlayer.isPlaying()) {
                        // when player is pause, stop callback
                        return;
                    }
                    playerListener.onTrackProgress(currentMediaPlayer.uri, (int)((float)currentDuration / duration * 1000.0F), currentDuration, duration);
                }
            }
            progressHandler.postDelayed(this, PlayerEngineImpl.this.progressUpdateInterval);
        }
    };

    public PlayerEngineImpl(Context context) {
        this.mContext = context;
        this.progressHandler = new Handler();
    }

    private InternalMediaPlayer build() {
        final InternalMediaPlayer player = new InternalMediaPlayer();
        if(this.wakeMode) {
            player.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        }
        player.setOnBufferingUpdateListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int what, int i1) {
                switch (what) {
                    case MEDIA_INFO_BUFFERING_END:
                        if (playerListener != null) {
                            playerListener.onTrackBuffering(player.uri, 100);
                        }
                    case MEDIA_INFO_BUFFERING_START:
                    default:
                        return false;
                }
            }
        });
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                player.preparing = false;
                player.start();
                if(playerListener != null) {
                    playerListener.onTrackStart(currentMediaPlayer.uri);
                }
                if(playStateListener != null) {
                    playStateListener.onPlayStateChange(true);
                }
                progressHandler.postDelayed(progressRunnable, progressUpdateInterval);
            }
        });
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        return player;
    }

    public int getAudioSessionId() {
        return currentMediaPlayer != null ? currentMediaPlayer.getAudioSessionId() : -1;
    }

    public boolean isPlaying() {
        return currentMediaPlayer != null && currentMediaPlayer.isPlaying();
    }

    void setPlayStateListener(OnPlayStateChangeListener playStateListener) {
        this.playStateListener = playStateListener;
    }

    @Override
    public void setPlayListManager(PlayListManager playListManager) {
        this.playListManager = playListManager;
        if(playListManager != null && this.mPlaybackMode != null) {
            playListManager.setPlayListPlaybackMode(mPlaybackMode);
        }
    }

    @Override
    public PlayListManager getPlayListManager() {
        return this.playListManager;
    }

    @Override
    public void play() {
        if(playListManager != null) {
            play(playListManager.getSelectedUri(), false);
        }
    }

    private void play(String uri, boolean restart) {
        if(!TextUtils.isEmpty(uri)) {
            if(currentMediaPlayer != null) {
                if(uri.equals(currentMediaPlayer.uri) && !restart) {
                    resume();
                } else {
                    cleanUp();
                    onStartBuffer(uri);
                    currentMediaPlayer = build();
                    start(uri);
                }
            } else {
                currentMediaPlayer = build();
                onStartBuffer(uri);
                start(uri);
            }
        }
    }

    private void onStartBuffer(String uri) {
        if(uri.startsWith("http") || uri.startsWith("www")) {
            int bufferPercent = 0;
            if(currentMediaPlayer != null) {
                bufferPercent = currentMediaPlayer.bufferPercent;
            }
            if(playerListener != null) {
                playerListener.onTrackBuffering(uri, bufferPercent);
            }
        }
    }

    private void restart() {
        if(currentMediaPlayer != null) {
            play(currentMediaPlayer.uri, true);
        }
    }

    private void start(String uri) {
        currentMediaPlayer.internalPlayListManager = this.playListManager;
        if(!uri.equals(currentMediaPlayer.uri)) {
            currentMediaPlayer.uri = uri;
            if(playerListener != null) {
                playerListener.onTrackChange(uri);
            }
        }
        try {
            Log.d(TAG, uri);
            currentMediaPlayer.setDataSource(uri);
            currentMediaPlayer.preparing = true;
            currentMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if(playerListener != null && currentMediaPlayer != null) {
            playerListener.onTrackProgress(currentMediaPlayer.uri, 0, 0, 0);
            // percent = 1 not 0 is avoid user need show buffer progress on buffer percent is 0
            playerListener.onTrackBuffering(currentMediaPlayer.uri, 1);
            playerListener.onTrackStop(currentMediaPlayer.uri);
        }
        cleanUp();
    }

    @Override
    public void pause() {
        if(currentMediaPlayer != null) {
            if(currentMediaPlayer.preparing) {
                return;
            }

            if(currentMediaPlayer.isPlaying()) {
                currentMediaPlayer.pause();
                if(playerListener != null) {
                    playerListener.onTrackPause(currentMediaPlayer.uri);
                }

                if(playStateListener != null) {
                    playStateListener.onPlayStateChange(false);
                }
            }
        }
    }

    @Override
    public void resume() {
        if(currentMediaPlayer != null) {
            if(currentMediaPlayer.preparing) {
                return;
            }
            if(!currentMediaPlayer.isPlaying()) {
                currentMediaPlayer.start();
                progressHandler.postDelayed(progressRunnable, progressUpdateInterval);
                if(playerListener != null) {
                    playerListener.onTrackStart(currentMediaPlayer.uri);
                }
                if(playStateListener != null) {
                    playStateListener.onPlayStateChange(true);
                }
            }
        } else {
            play();
        }
    }

    @Override
    public boolean toggle() {
        if(currentMediaPlayer == null) {
            play();
            return true;
        } else if(currentMediaPlayer.isPlaying()) {
            pause();
            return false;
        } else {
            resume();
            return true;
        }
    }

    @Override
    public void next() {
        if(playListManager != null) {
            playListManager.selectNext();
            play();
        }
    }

    @Override
    public void prev() {
        if(playListManager != null) {
            playListManager.selectPrev();
            play();
        }
    }

    @Override
    public void seekTo(int percent) {
        if(currentMediaPlayer != null) {
            percent = Math.min(percent, 1000);
            currentMediaPlayer.seekTo(currentMediaPlayer.getDuration() * percent / 1000);
        }
    }

    @Override
    public void skipTo(int position) {
        if(playListManager != null) {
            playListManager.select(position);
            play();
        }
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if(currentMediaPlayer != null) {
            currentMediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    @Override
    public void setListener(PlayerListener playerListener) {
        this.playerListener = playerListener;
        if(currentMediaPlayer != null) {
            onStartBuffer(currentMediaPlayer.uri);  // check the buffer
        }
    }

    @Override
    public PlayerListener getListener() {
        return this.playerListener;
    }

    @Override
    public void setPlaybackMode(PlaybackMode playbackMode) {
        this.mPlaybackMode = playbackMode;
        if(playListManager != null) {
            playListManager.setPlayListPlaybackMode(playbackMode);
        }
    }

    @Override
    public PlaybackMode getPlaybackMode() {
        //Playback Mode is based on the play list manager
        return playListManager != null ? playListManager.getPlaylistPlaybackMode() : null;
    }

    @Override
    public void setWakeMode() {
        this.wakeMode = true;
    }

    @Override
    public int getCurrentPlayPercent() {
        return currentMediaPlayer != null ? (int)((float)currentMediaPlayer.getCurrentPosition() / currentMediaPlayer.getDuration() * 1000.0F) : 0;
    }

    @Override
    public void setFadeVolumeWhenStartOrPause(boolean isFadeVolume) {
        this.isFadeVolume = isFadeVolume;
    }

    @Override
    public boolean isFadeVolumeWhenStartOrPause() {
        return this.isFadeVolume;
    }

    @Override
    public void setPlayNextWhenError(boolean playNextWhenError) {
        this.playNextWhenError = playNextWhenError;
    }

    @Override
    public boolean isPlayNextWhenError() {
        return this.playNextWhenError;
    }

    private void cleanUp() {
        progressHandler.removeCallbacks(progressRunnable);
        if(currentMediaPlayer != null) {
            currentMediaPlayer.release();
            currentMediaPlayer = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(getPlaybackMode() == PlaybackMode.SINGLE_REPEAT) {
            restart();
        } else if(!currentMediaPlayer.preparing) {
            next();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        switch (what) {
            case MEDIA_ERROR_UNKNOWN:
                Log.e(TAG, "unknown media playback error");
                break;
            case MEDIA_ERROR_SERVER_DIED:
                Log.e(TAG, "server connection died");
                break;
            default:
                Log.e(TAG, "generic audio playback error");
                break;
        }

        switch (extra) {
            case -2147483648:
                Log.e(TAG, "file has damaged");
                break;
            case MEDIA_ERROR_UNSUPPORTED:
                Log.e(TAG, "unsupported media content");
                break;
            case MEDIA_ERROR_MALFORMED:
                Log.e(TAG, "media error, malformed");
                break;
            case MEDIA_ERROR_IO:
                Log.e(TAG, "IO media error");
                break;
            case MEDIA_ERROR_TIMED_OUT:
                Log.e(TAG, "media timeout error");
                break;
            default:
                Log.e(TAG, "unknown playback error");
        }
        if(playerListener != null) {
            playerListener.onTrackStreamError(currentMediaPlayer.uri, what, extra);
        }
        stop();
        if(!playNextWhenError) {
            return true;
        }
        long failTime = System.currentTimeMillis();
        if(failTime - mLastFailTime > FAIL_TIME_FRAME) {
            mTimesFailed = 1;
            mLastFailTime = failTime;
            Log.w(TAG, mTimesFailed + "fail within 1 second");
        }
        else {
            ++mTimesFailed;
            if(mTimesFailed > ACCEPTABLE_FAIL_NUMBER) {
                cleanUp();
                Log.w(TAG, "Continue occur errors, stop play music");
            }
        }
        if(playListManager != null && !playListManager.isLastTrackOnList() && mTimesFailed <= 5) {
            next();
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        if(currentMediaPlayer != null) {
            currentMediaPlayer.bufferPercent = percent;

            if(playerListener != null && percent > 0) {
                playerListener.onTrackBuffering(currentMediaPlayer.uri, percent);
            }
        }
    }

    private class InternalMediaPlayer extends MediaPlayer {
        boolean preparing = false;
        PlayListManager internalPlayListManager;
        String uri;
        private float currentVolumeValue;
        private boolean pause;
        private InternalMediaPlayer instance = this;
        int bufferPercent;
        private Handler handler = new Handler();
        private Runnable volumeRunnable = new Runnable() {
            public void run() {
                if(instance != null) {
                    if(pause) {
                        currentVolumeValue = currentVolumeValue - 0.05F;
                    } else {
                        currentVolumeValue = currentVolumeValue + 0.02F;
                    }

                    if(currentVolumeValue >= 0.0F && currentVolumeValue <= 1.0F) {
                        instance.setVolume(currentVolumeValue, currentVolumeValue);
                        handler.postDelayed(this, 50L);
                    } else if(currentVolumeValue < 0.0F) {
                        pause = false;
                        InternalMediaPlayer.super.pause();
                    }
                }

            }
        };

        public InternalMediaPlayer() {}

        /**
         * Override MediaPlayer pause function, do fade volume
         */
        @Override
        public void pause() {
            this.pause = true;
            if(PlayerEngineImpl.this.isFadeVolume) {
                this.handler.removeCallbacks(this.volumeRunnable);
                this.handler.post(this.volumeRunnable);
            } else {
                super.pause();
            }
        }

        /**
         * Override MediaPlayer pause/start function, do fade volume
         */
        public void start() {
            this.pause = false;
            super.setVolume(this.currentVolumeValue, this.currentVolumeValue);
            super.start();
            if(PlayerEngineImpl.this.isFadeVolume) {
                this.handler.removeCallbacks(this.volumeRunnable);
                this.currentVolumeValue = Math.max(0.0F, this.currentVolumeValue);
                this.handler.post(this.volumeRunnable);
            } else {
                super.setVolume(1.0F, 1.0F);
            }
        }

        public void release() {
            this.handler.removeCallbacks(this.volumeRunnable);
            this.instance = null;
            super.release();
        }

        @Override
        public boolean isPlaying() {
            return super.isPlaying() && !this.pause;
        }
    }

    public interface OnPlayStateChangeListener {
        void onPlayStateChange(boolean play);
    }
}
