package com.suifeng.lib.playerengine.core;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.suifeng.lib.playerengine.util.ReflectUtil;

import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_BRING_TO_FRONT;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_NEXT;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_PRE;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_STOP;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_TOGGLE_PLAY;

/**
 * player notification
 */

public class PlayerNotification {

    public static final int ID = 111;
    private static final String NOTIFICATION_LAYOUT_NAME = "sf_layout_player_notification";
    private static final String BUTTON_NEXT_ID_NAME = "btn_next_music";
    private static final String BUTTON_PRE_ID_NAME = "btn_pre_music";
    private static final String BUTTON_TOGGLE_ID_NAME = "btn_play_pause";
    private static final String BUTTON_DESTROY = "btn_destroy";
    private static final String TV_MUSIC_NAME = "tv_music_name";
    private static final String TV_ARTIST = "tv_artist";
    private static final String IV_MUSIC_COVER = "iv_music_cover";
    private static final String ICON_DRAWABLE = "icon_notification_player";
    private static final String DRAWABLE_PLAY_STATE = "btn_notification_player_pause";
    private static final String DRAWABLE_PAUSE_STATE = "btn_notification_player_play";
    private static final String DRAWABLE_MUSIC_COVER = "notification_default_cover";
    private static final int FLAG = PendingIntent.FLAG_UPDATE_CURRENT;

    private Notification notification;
    private Context mContext;
    private String mPackageName;
    private Class<?> idClass;
    private Class<?> drawableClass;
    private int[] requestCodes = new int[]{1, 2, 3, 4};
    private int[] resIds;
    private String[] actions = new String[]{ACTION_TOGGLE_PLAY, ACTION_NEXT, ACTION_PRE, ACTION_STOP};
    private int playDrawableId;
    private int pauseDrawableId;
    private int musicCoverDrawableId;
    private int musicNameTvId;
    private int artistNameTvId;
    private int musicCoverIv;
    private PlayerService mService;
    private NotificationManager mManager;
    private RemoteViews views;
    private int layoutId = -1;
    private String currentImageUri;

    public PlayerNotification(PlayerService service) {
        this.mService = service;
        mContext = service.getApplicationContext();
        mPackageName = service.getPackageName();
        mManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        initRes();
    }

    private void initRes() {
        Class<?> layoutClass = ReflectUtil.findClassInR(mPackageName, "layout");
        idClass = ReflectUtil.findClassInR(mPackageName, "id");
        drawableClass = ReflectUtil.findClassInR(mPackageName, "drawable");
        layoutId = ReflectUtil.getIntegerInR(mContext, layoutClass, NOTIFICATION_LAYOUT_NAME);
        musicCoverIv = findViewIdByIdName(IV_MUSIC_COVER);
        musicCoverDrawableId = findDrawableIdByIdName(DRAWABLE_MUSIC_COVER);
        playDrawableId = findDrawableIdByIdName(DRAWABLE_PLAY_STATE);
        pauseDrawableId = findDrawableIdByIdName(DRAWABLE_PAUSE_STATE);
        musicNameTvId = findViewIdByIdName(TV_MUSIC_NAME);
        artistNameTvId = findViewIdByIdName(TV_ARTIST);
        resIds = new int[4];
        resIds[0] = findViewIdByIdName(BUTTON_TOGGLE_ID_NAME);
        resIds[1] = findViewIdByIdName(BUTTON_NEXT_ID_NAME);
        resIds[2] = findViewIdByIdName(BUTTON_PRE_ID_NAME);
        resIds[3] = findViewIdByIdName(BUTTON_DESTROY);
    }

    private int findIdByIdName(Class<?> clz, String idName) {
        return ReflectUtil.getIntegerInR(mContext, clz, idName);
    }

    private int findViewIdByIdName(String idNmae) {
        return findIdByIdName(idClass, idNmae);
    }

    private int findDrawableIdByIdName(String drawableName) {
        return findIdByIdName(drawableClass, drawableName);
    }

    public Notification init(boolean playing, String musicName, String artist) {
        if(layoutId < 0) {
            return null;
        } else {
            views = initContentView(layoutId);
            int iconId = findDrawableIdByIdName(ICON_DRAWABLE);
            notification = buildNotification(musicName, iconId);
            mService.startForeground(ID, notification);
            mManager.notify(ID, notification);
            return notification;
        }
    }

    private Notification buildNotification(String musicName, int iconId) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        Notification notification = mBuilder.setContentTitle(musicName).setSmallIcon(iconId).setContent(views).setContentIntent(buildContentIntent()).build();
        setMaxPriority();
        return notification;
    }

    private PendingIntent buildContentIntent() {
        Intent intent = new Intent(mContext, PlayerService.class);
        intent.setAction(ACTION_BRING_TO_FRONT);
        return PendingIntent.getService(mContext.getApplicationContext(), 0, intent, FLAG);
    }

    public void update(boolean playing, String musicName, String artist) {
        if(notification != null) {
            setMaxPriority();
            update(views, playing, musicName, artist);
        }
    }

    private void update(RemoteViews views, boolean playing, String musicName, String artist) {
        if(notification != null && views != null) {
            views.setImageViewResource(resIds[0], playing ? playDrawableId : pauseDrawableId);
            views.setImageViewResource(musicCoverIv, musicCoverDrawableId);
            updateText(views, musicName, artist);
            mManager.notify(ID, notification);
        }
    }

    public void update(boolean playing) {
        if(notification != null) {
            setMaxPriority();
            views.setImageViewResource(resIds[0], playing ? playDrawableId : pauseDrawableId);
            mManager.notify(ID, notification);
        }
    }

    public void updateText(String musicName, String artist) {
        if(notification != null) {
            setMaxPriority();
            updateText(views, musicName, artist);
        }
    }

    private void updateText(RemoteViews views, String musicName, String artist) {
        if(notification != null && views != null) {
            views.setTextViewText(musicNameTvId, musicName);
            views.setTextViewText(artistNameTvId, artist);
            mManager.notify(ID, notification);
        }

    }

    @SuppressLint({"NewApi"})
    private void setMaxPriority() {
        if(Build.VERSION.SDK_INT >= 16 && this.notification != null) {
            this.notification.priority = Notification.PRIORITY_MAX;
        }

    }

    private RemoteViews initContentView(int layoutId) {
        RemoteViews views = new RemoteViews(mPackageName, layoutId);
        initPendingIntents(views);
        return views;
    }

    private void initPendingIntents(RemoteViews views) {
        for(int i = 0; i < resIds.length; ++i) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCodes[i], new Intent(actions[i]), FLAG);
            views.setOnClickPendingIntent(this.resIds[i], pendingIntent);
        }

    }

    private void updateImage() {
        if(notification != null) {
            views.setImageViewResource(musicCoverIv, musicCoverDrawableId);
            this.mManager.notify(ID, this.notification);
        }
    }

    public String getCurrentImageUri() {
        return currentImageUri;
    }

    public void updateImage(String imageUri, Bitmap bitmap) {
        this.currentImageUri = imageUri;
        if(notification != null) {
            if(bitmap != null) {
                views.setImageViewBitmap(musicCoverIv, bitmap);
            }
            else {
                updateImage();
            }
            mManager.notify(ID, notification);
        }
    }

    public void close() {
        mManager.cancel(ID);
    }

}
