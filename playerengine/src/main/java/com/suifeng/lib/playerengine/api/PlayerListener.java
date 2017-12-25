package com.suifeng.lib.playerengine.api;

/**
 * player status callback
 */

public interface PlayerListener {

    void onTrackBuffering(String uri, int percent);
    void onTrackStart(String uri);
    void onTrackChange(String uri);
    void onTrackProgress(String uri, int percent, int currentDuration, int duration);
    void onTrackPause(String uri);
    void onTrackStop(String uri);
    void onTrackStreamError(String uri, int what, int extra);
}
