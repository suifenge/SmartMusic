package com.suifeng.lib.playerengine.api;

/**
 * playback mode
 */

public enum  PlaybackMode {
    ALL,                //play all but not repeat
    SHUFFLE,            //play all in random index
    SINGLE_REPEAT;      //repeat one

    private PlaybackMode() {
    }
}
