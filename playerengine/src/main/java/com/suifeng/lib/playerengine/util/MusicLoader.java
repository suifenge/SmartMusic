package com.suifeng.lib.playerengine.util;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

import com.suifeng.lib.playerengine.api.LoadMusicListener;
import com.suifeng.lib.playerengine.entity.Music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * load local external storage audios.
 */

public class MusicLoader {

    private static final String TAG = "MusicLoader";
    private static final long MIN_MUSIC_DURATION = 20000;    //the music duration must greater than 20 seconds

    private long minMusicDuration = MIN_MUSIC_DURATION;
    private static MusicLoader musicLoader;
    private ContentResolver contentResolver;
    private Uri contentUri;
    private Cursor cursor;
    private LoadMusicListener loadMusicListener;
    private String[] musicColumns;
    private Executor executor;
    private LoadMusicTask loadMusicTask;

    public static MusicLoader instance(ContentResolver contentResolver) {
        if(musicLoader == null) {
            musicLoader = new MusicLoader(contentResolver);
        }
        return musicLoader;
    }

    private MusicLoader(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        this.contentUri = Media.EXTERNAL_CONTENT_URI;
        this.musicColumns = new String[]{Media._ID, Media.TITLE, Media.ARTIST, Media.DATA, Media.DURATION, Media.SIZE, Media.ALBUM, Media.ALBUM_ID};
        this.executor = Executors.newFixedThreadPool(1);
    }

    public void setMinMusicDuration(long minMusicDuration) {
        this.minMusicDuration = minMusicDuration;
    }

    public void loadMusic(LoadMusicListener loadMusicListener) {
        this.loadMusicListener = loadMusicListener;
        if(this.loadMusicTask != null && !this.loadMusicTask.isCancelled()) {
            this.loadMusicTask.cancel(true);
            this.loadMusicTask = null;
        }
        this.loadMusicTask = new LoadMusicTask();
        if(Build.VERSION.SDK_INT >= 11) {
            this.forAPI11();
        } else {
            this.loadMusicTask.execute(new String[]{""});
        }
    }

    @TargetApi(11)
    private void forAPI11() {
        this.loadMusicTask.executeOnExecutor(this.executor, new String[]{""});
    }

    private class LoadMusicTask extends AsyncTask<String, Music, List<Music>> {

        private LoadMusicTask() {}

        @Override
        protected List<Music> doInBackground(String... strings) {
            return getMusicList();
        }

        @Override
        protected void onPostExecute(List<Music> musics) {
            super.onPostExecute(musics);
            if(MusicLoader.this.loadMusicListener != null) {
                MusicLoader.this.loadMusicListener.onLoadMusic(musics);
            }
        }

        @Override
        protected void onProgressUpdate(Music... music) {
            if(MusicLoader.this.loadMusicListener != null) {
                MusicLoader.this.loadMusicListener.onLoadingMusic(music[0]);
            }
        }

        private List<Music> getMusicList() {
            List<Music> musicList = new ArrayList();
            MusicLoader.this.cursor = contentResolver.query(MusicLoader.this.contentUri, MusicLoader.this.musicColumns, null, null, null);
            if(cursor == null) {
                Log.w(TAG, "Music Loader cursor is null (on line 62)");
            } else if (!MusicLoader.this.cursor.moveToFirst()) {
                Log.w(TAG, "Music Loader cursor moveToFirst return false (on line 65)");
            } else {
                int idCol = MusicLoader.this.cursor.getColumnIndex(Media._ID);
                int displayNameCol = MusicLoader.this.cursor.getColumnIndex(Media.TITLE);
                int artistCol = MusicLoader.this.cursor.getColumnIndex(Media.ARTIST);
                int urlCol = MusicLoader.this.cursor.getColumnIndex(Media.DATA);
                int durationCol = MusicLoader.this.cursor.getColumnIndex(Media.DURATION);
                int sizeCol = MusicLoader.this.cursor.getColumnIndex(Media.SIZE);
                int albumCol = MusicLoader.this.cursor.getColumnIndex(Media.ALBUM);
                int albumIdCol = MusicLoader.this.cursor.getColumnIndex(Media.ALBUM_ID);

                do {
                    String title = MusicLoader.this.cursor.getString(displayNameCol);
                    long id = MusicLoader.this.cursor.getLong(idCol);
                    long duration = MusicLoader.this.cursor.getLong(durationCol);
                    if(MusicLoader.this.minMusicDuration <= duration) {
                        long size =  MusicLoader.this.cursor.getLong(sizeCol);
                        String artist = MusicLoader.this.cursor.getString(artistCol);
                        String url = MusicLoader.this.cursor.getString(urlCol);
                        String album = MusicLoader.this.cursor.getString(albumCol);
                        long albumId = MusicLoader.this.cursor.getLong(albumIdCol);
                        Music music = new Music(id, title);
                        music.setSong(music.getTitle());
                        music.setArtist(artist);
                        music.setUrl(url);
                        music.setAlbum(album);
                        music.setAlbumId(albumId);
                        music.setDuration(duration);
                        music.setSize(size);
                        musicList.add(music);
                        onProgressUpdate(music);
                    }
                } while(MusicLoader.this.cursor.moveToNext());
            }

            MusicLoader.this.close();
            return musicList;
        }
    }

    private void close() {
        if(this.cursor != null) {
            this.cursor.close();
            this.cursor = null;
        }

    }

}
