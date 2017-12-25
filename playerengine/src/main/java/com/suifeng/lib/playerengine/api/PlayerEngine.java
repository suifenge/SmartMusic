package com.suifeng.lib.playerengine.api;

import com.suifeng.lib.playerengine.core.PlayListManager;

/**
 *player engine function
 */

public interface PlayerEngine {

    void setPlayListManager(PlayListManager playListManager);

    PlayListManager getPlayListManager();

    void play();

    boolean isPlaying();

    void stop();

    void pause();

    void resume();

    boolean toggle();

    void next();

    void prev();

    void seekTo(int percent);

    void skipTo(int position);

    void setVolume(float leftVolume, float rightVolume);

    void setListener(PlayerListener playerListener);

    PlayerListener getListener();

    void setPlaybackMode(PlaybackMode playbackMode);

    PlaybackMode getPlaybackMode();

    void setWakeMode();

    int getCurrentPlayPercent();

    void setFadeVolumeWhenStartOrPause(boolean isFadeVolume);

    boolean isFadeVolumeWhenStartOrPause();

    void setPlayNextWhenError(boolean playNextWhenError);

    boolean isPlayNextWhenError();
}
