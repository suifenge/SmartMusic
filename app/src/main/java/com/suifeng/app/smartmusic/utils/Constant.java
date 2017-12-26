package com.suifeng.app.smartmusic.utils;

public interface Constant {

    /**
     * http://s.music.qq.com/fcgi-bin/music_search_new_platform?t=0&n=${num}&aggr=1&cr=1&loginUin=0&format=json&inCharset=GB2312&outCharset=utf-8&notice=0&platform=jqminiframe.json&needNewCode=0&p=1&catZhida=0&remoteplace=sizer.newclient.next_song&w=${name}
     */

    String BASE_MUSIC_SEARCH_URL = "http://s.music.qq.com/fcgi-bin/";
    String BASE_MUSIC_URL = "http://ws.stream.qqmusic.qq.com/%s.m4a?fromtag=46";
    String BASE_ALBUM_PICTURE_URL = "http://imgcache.qq.com/music/photo/album_500/%s/500_albumpic_%s_0.jpg";
    String ENSURE_DIAL_NUMBER = "确认拨打%s的号码吗？";

}
