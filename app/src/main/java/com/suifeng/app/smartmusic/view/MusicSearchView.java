package com.suifeng.app.smartmusic.view;

import com.suifeng.lib.playerengine.entity.Music;
import com.suifeng.library.base.view.MvpView;

import java.util.List;

public interface MusicSearchView extends MvpView{
    void searchEmpty();
    void searMusicList(List<Music> musicList);
}
