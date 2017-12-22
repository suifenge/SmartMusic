package com.suifeng.app.smartmusic.presenter;

import com.suifeng.app.smartmusic.entity.SearchMusicEntity;
import com.suifeng.app.smartmusic.net.RetrofitFactory;
import com.suifeng.app.smartmusic.net.api.SearchMusicService;
import com.suifeng.app.smartmusic.utils.Constant;
import com.suifeng.app.smartmusic.utils.QQMusicParser;
import com.suifeng.app.smartmusic.view.MusicSearchView;
import com.suifeng.lib.playerengine.entity.Music;
import com.suifeng.library.base.net.RxSchedulers;
import com.suifeng.library.base.presenter.BasePresenter;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.List;

public class MusicSearchPresenter extends BasePresenter<MusicSearchView>{

    private LifecycleProvider<ActivityEvent> provider;

    public MusicSearchPresenter(LifecycleProvider<ActivityEvent> provider) {
        super(provider);
        this.provider = provider;
    }

    public void searchMusicByNameOrArtist(String keyWord) {
        SearchMusicService searchMusicService = RetrofitFactory.getSearchMusicService();
        searchMusicService.getSearchMusic(keyWord)
                          .compose(RxSchedulers.compose(provider))
                          .map(this::getMusicList)
                          .subscribe(musicList -> {
                              if(musicList.isEmpty() || musicList.size() < 1) {
                                  getMvpView().searchEmpty();
                              } else {
                                  getMvpView().searMusicList(musicList);
                              }
                          });

    }

    private List<Music> getMusicList(SearchMusicEntity searchMusicEntity) {
        List<Music> musicList = new ArrayList<>();
        for (int i = 0; i < searchMusicEntity.getData().getSong().getList().size(); i++) {
            SearchMusicEntity.DataBean.SongBean.ListBean listBean = searchMusicEntity.getData().getSong().getList().get(i);
            String f = listBean.getF();
            Music music = new Music();
            music.setTitle(listBean.getFsong());
            music.setArtist(listBean.getFsinger());
            music.setUrl(String.format(Constant.BASE_MUSIC_URL, QQMusicParser.getMusicId(f)));
            music.setAlbum(listBean.getAlbumName_hilight());
            music.setAlbumId(QQMusicParser.getMusicAlbumId(f));
            music.setDuration(QQMusicParser.getDuration(f));
            music.setSize(QQMusicParser.getMusicSize(f));
            musicList.add(music);
        }
        return musicList;
    }
}
