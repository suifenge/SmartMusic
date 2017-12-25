package com.suifeng.lib.playerengine.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * music entity
 */
public class Music implements Parcelable{

    private long id;
    private String title;
    private String song;
    private String artist;
    private String url;
    private String album;
    private long albumId;
    private long duration;
    private long size;
    private String cover;
    private String lyrics;

    public Music() {}

    public Music(long id, String title) {
        this.id = id;
        this.title = title;
    }

    protected Music(Parcel in) {
        id = in.readLong();
        title = in.readString();
        song = in.readString();
        artist = in.readString();
        url = in.readString();
        album = in.readString();
        albumId = in.readLong();
        duration = in.readLong();
        size = in.readLong();
        cover = in.readString();
        lyrics = in.readString();
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeString(song);
        parcel.writeString(artist);
        parcel.writeString(url);
        parcel.writeString(album);
        parcel.writeLong(albumId);
        parcel.writeLong(duration);
        parcel.writeLong(size);
        parcel.writeString(cover);
        parcel.writeString(lyrics);
    }
}
