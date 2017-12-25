package com.suifeng.app.smartmusic.presenter;

import com.suifeng.app.smartmusic.entity.SearchQQMusicEntity;
import com.suifeng.app.smartmusic.net.RetrofitFactory;
import com.suifeng.app.smartmusic.net.api.SearchQQMusicService;
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

    public void searchQQMusicByNameOrArtist(String keyWord) {
        SearchQQMusicService searchQQMusicService = RetrofitFactory.getSearchQQMusicService();
        searchQQMusicService.getSearchQQMusic(keyWord)
                          .compose(RxSchedulers.compose(provider))
                          .map(this::getQQMusicList)
                          .subscribe(musicList -> {
                              if(musicList.isEmpty() || musicList.size() < 1) {
                                  getMvpView().searchEmpty();
                              } else {
                                  getMvpView().searMusicList(musicList);
                              }
                          });

    }

    private List<Music> getQQMusicList(SearchQQMusicEntity searchQQMusicEntity) {
        List<Music> musicList = new ArrayList<>();
        for (int i = 0; i < searchQQMusicEntity.getData().getSong().getList().size(); i++) {
            SearchQQMusicEntity.DataBean.SongBean.ListBean listBean = searchQQMusicEntity.getData().getSong().getList().get(i);
            String f = listBean.getF();
            Music music = new Music();
            music.setSong(listBean.getFsong());
            music.setArtist(listBean.getFsinger());
            music.setTitle(music.getSong() + "-" + music.getArtist());
            music.setUrl(String.format(Constant.BASE_MUSIC_URL, QQMusicParser.getMusicId(f)));
            music.setAlbum(listBean.getAlbumName_hilight());
            music.setAlbumId(QQMusicParser.getMusicAlbumId(f));
            music.setDuration(QQMusicParser.getDuration(f));
            music.setSize(QQMusicParser.getMusicSize(f));
            music.setCover(getCoverUrl(music.getAlbumId()));
            musicList.add(music);
        }
        return musicList;
    }

    private String getCoverUrl(long albumId) {
        String encry = String.valueOf(albumId % 100);
        return String.format(Constant.BASE_ALBUM_PIRTURE_URL, encry, String.valueOf(albumId));
    }
}
