package com.suifeng.lib.playerengine.api;

import android.graphics.Bitmap;

/**
 * notification interface, user implements
 */

public interface NotificationAdapter {
    boolean isPlaying();

    String getMusicName();

    String getArtistName();

    void loadMusicImage(MusicImageLoadListener musicImageLoadListener);

    void onNotificationClick();

    interface MusicImageLoadListener {
        void onMusicImageLoaded(String imgUrl, Bitmap bitmap);
    }
}
