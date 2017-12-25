package com.suifeng.app.smartmusic.net.api;

import com.suifeng.app.smartmusic.entity.SearchQQMusicEntity;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchQQMusicService {

    @GET("music_search_new_platform?t=0&n=30&aggr=1&cr=1&loginUin=0&format=json&inCharset=GB2312&outCharset=utf-8&notice=0&platform=jqminiframe.json&needNewCode=0&p=1&catZhida=0&remoteplace=sizer.newclient.next_song")
    Observable<SearchQQMusicEntity> getSearchQQMusic(@Query("w") String name);

}
