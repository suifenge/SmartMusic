package com.suifeng.lib.playerengine.api;

import com.suifeng.lib.playerengine.entity.Music;

import java.util.List;

/**
 * load local music callback
 */

public interface LoadMusicListener {
    void onLoadMusic(List<Music> musics);
    void onLoadingMusic(Music music);
}
