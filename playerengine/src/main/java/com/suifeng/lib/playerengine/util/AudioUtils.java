package com.suifeng.lib.playerengine.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.math.BigDecimal;

public class AudioUtils {
    /**
     * use album id cursor album bitmap
     * @param album_art string
     * @return bitmap
     */
    public static Bitmap getLocalAudioAlbumPicture(String album_art) {
        if(album_art == null) {
            return null;
        } else {
            return BitmapFactory.decodeFile(album_art);
        }
    }

    /**
     * use album id cursor album picture uri
     * @param context context
     * @param album_id album id
     * @return string
     */
    public static String getLocalAudioAlbumPictureUri(Context context, long album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[] { "album_art" };
        Cursor cur = context.getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + Long.toString(album_id)),
                projection, null, null, null);
        if(cur == null) {
            return null;
        }
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        if(album_art != null) {
            return album_art;
        }
        return null;
    }

    /**
     * format music duration
     * @param duration ms
     * @return string time like 03:20
     */
    public static String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minute = seconds / 60;
        long second = seconds % 60;
        return (minute >= 10 ? minute : "0" + minute) + ":" +
                (second >= 10 ? second : "0" +second);
    }

    /**
     * format file size
     * @param size long
     * @return
     */
    public static String getFormatSize(long size) {
        double kiloByte = size / 1024.0;
        if (kiloByte < 1) {
            return size + "B";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }
}
