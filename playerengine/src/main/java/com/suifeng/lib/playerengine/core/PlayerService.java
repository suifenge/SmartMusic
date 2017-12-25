package com.suifeng.lib.playerengine.core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.suifeng.lib.playerengine.api.NotificationAdapter;
import com.suifeng.lib.playerengine.api.PlaybackMode;
import com.suifeng.lib.playerengine.util.AudioHelper;

import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_BRING_TO_FRONT;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_FADE_VOLUME;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_NEXT;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_PAUSE;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_PLAY;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_PLAY_NEXT_WHEN_ERROR;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_PRE;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_RESUME;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_SEEK_TO;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_SET_LISTENER;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_SET_PLATLIST;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_SET_PLAYMODE;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_SET_VOLUME;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_SKIP_TO;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_STOP;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_TOGGLE_PLAY;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_WAKE_MODE;
import static com.suifeng.lib.playerengine.command.CommandFactory.ExtraData.EXTRA_CLOSE_NOTIFICATION;
import static com.suifeng.lib.playerengine.command.CommandFactory.ExtraData.EXTRA_FROM;
import static com.suifeng.lib.playerengine.command.CommandFactory.ExtraData.EXTRA_LEFT_VOLUME;
import static com.suifeng.lib.playerengine.command.CommandFactory.ExtraData.EXTRA_RIGHT_VOLUME;
import static com.suifeng.lib.playerengine.command.CommandFactory.ExtraData.EXTRA_SEEK_PERCENT;
import static com.suifeng.lib.playerengine.command.CommandFactory.ExtraData.EXTRA_SKIP_INDEX;

/**
 * player background service
 */

public class PlayerService extends Service implements PlayerEngineImpl.OnPlayStateChangeListener{

    private static final String TAG = "PlayerService";

    private PlayerEngineImpl playerEngine;
    private Player player;
    private AudioHelper audioHelper;
    private boolean isUserPause = true;
    private PlayerNotification notification;
    private boolean hasShowNotification;
    private boolean register;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(ACTION_NEXT.equals(action)) {
                playerEngine.next();
            } else if(ACTION_PRE.equals(action)) {
                playerEngine.prev();
            } else if(ACTION_TOGGLE_PLAY.equals(action)) {
                playerEngine.toggle();
            } else if(ACTION_STOP.equals(action)) {
                stopSelf();
            }
            updateNotification(action);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initPlayer();
    }

    private void initPlayer() {
        player = Player.getInstance(this);
        if(player != null) {
            playerEngine = new PlayerEngineImpl(this);
            playerEngine.setPlayStateListener(this);
            setListener();
            setPlayListManager();
            player.setPlayerEngine(playerEngine);
            initAudioHelper();
            if(Build.VERSION.SDK_INT >= 8) {
                audioHelper.requestFocus();
            }
        }
    }

    private void initAudioHelper() {
        if(audioHelper != null) {
            audioHelper.destroy();
        }
        audioHelper = new AudioHelper(getApplicationContext(), playerEngine, MediaButtonIntentReceiver.class);
        audioHelper.registerMediaButtonEventReceiver();
    }

    private void setListener() {
        playerEngine.setListener(player.getListener());
    }

    private void setPlayListManager() {
        PlaybackMode playbackMode = player.getPlaybackMode();
        playerEngine.setPlayListManager(player.getPlayListManager());
        if(playbackMode != null) {
            playerEngine.setPlaybackMode(playbackMode);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null) {
            return super.onStartCommand(intent, flags, startId);
        } else {
            String action = intent.getAction();
            String from = intent.getStringExtra(EXTRA_FROM);
            if(player == null && "MediaButtonIntentReceiver".equals(from)) {
                return super.onStartCommand(intent, flags, startId);
            } else {
                if(action != null && player == null) {
                    initPlayer();
                }
                if(playerEngine != null && player != null && action != null) {
                    if(ACTION_STOP.equals(action)) {
                        boolean closeNotification = intent.getBooleanExtra(EXTRA_CLOSE_NOTIFICATION, false);
                        if(closeNotification) {
                            stopSelf();
                        } else {
                            playerEngine.stop();
                        }
                        return super.onStartCommand(intent, flags, startId);
                    } else {
                        if(ACTION_SET_LISTENER.equals(action)) {
                            setListener();
                        } else if(ACTION_SET_PLATLIST.equals(action)) {
                            setPlayListManager();
                        } else if(ACTION_PLAY.equals(action)) {
                            playerEngine.play();
                            audioHelper.requestFocus();
                        } else if(ACTION_PAUSE.equals(action)) {
                            isUserPause = true;
                            playerEngine.pause();
                        } else if(ACTION_RESUME.equals(action)) {
                            isUserPause = false;
                            playerEngine.resume();
                            audioHelper.requestFocus();
                        } else {
                            if(ACTION_SEEK_TO.equals(action)) {
                                playerEngine.seekTo(intent.getIntExtra(EXTRA_SEEK_PERCENT, 0));
                            } else if(ACTION_NEXT.equals(action)) {
                                playerEngine.next();
                                audioHelper.requestFocus();
                                isUserPause = false;
                            } else if(ACTION_PRE.equals(action)) {
                                playerEngine.prev();
                                audioHelper.requestFocus();
                                isUserPause = false;
                            } else if(ACTION_SKIP_TO.equals(action)) {
                                playerEngine.skipTo(intent.getIntExtra(EXTRA_SKIP_INDEX, 0));
                                audioHelper.requestFocus();
                                isUserPause = false;
                            } else if(ACTION_SET_PLAYMODE.equals(action)) {
                                playerEngine.setPlaybackMode(player.getPlaybackMode());
                            } else if(ACTION_SET_VOLUME.equals(action)) {
                                float leftVolume = intent.getFloatExtra(EXTRA_LEFT_VOLUME, 1.0F);
                                float rightVolume = intent.getFloatExtra(EXTRA_RIGHT_VOLUME, 1.0F);
                                playerEngine.setVolume(leftVolume, rightVolume);
                            } else if(ACTION_TOGGLE_PLAY.equals(action)) {
                                isUserPause = !playerEngine.toggle();
                                audioHelper.requestFocus();
                            } else if(ACTION_WAKE_MODE.equals(action)) {
                                playerEngine.setWakeMode();
                            } else if(ACTION_FADE_VOLUME.equals(action)) {
                                playerEngine.setFadeVolumeWhenStartOrPause(player.isFadeVolumeWhenStartOrPause());
                            } else if(ACTION_PLAY_NEXT_WHEN_ERROR.equals(action)) {
                                playerEngine.setPlayNextWhenError(player.isPlayNextWhenError());
                            } else if (ACTION_BRING_TO_FRONT.equals(action)) {
                                NotificationAdapter notificationAdapter = getNotificationAdapter();
                                if(notificationAdapter != null) {
                                    notificationAdapter.onNotificationClick();
                                }
                            }
                        }

                        audioHelper.setIsUserPause(isUserPause);
                        boolean isShowNotification = isShowNotificationAction(action);
                        if(!hasShowNotification && isShowNotification) {
                            initNotification();
                        }
                        if(isShowNotification) {
                            updateNotification(action);
                        }
                        return START_NOT_STICKY;
                    }
                } else {
                    return super.onStartCommand(intent, flags, startId);
                }
            }
        }
    }

    private boolean isShowNotificationAction(String action) {
        return player.isShowNotification() && ACTION_NEXT.equals(action) || ACTION_PRE.equals(action) || ACTION_PLAY.equals(action) || ACTION_RESUME.equals(action) || ACTION_TOGGLE_PLAY.equals(action) || ACTION_SKIP_TO.equals(action);
    }

    private void initNotification() {
        NotificationAdapter adapter = getNotificationAdapter();
        if(adapter != null) {
            hasShowNotification = true;
            notification = new PlayerNotification(this);
            registerNotificationBroadcastReceiver();
            String musicName = adapter.getMusicName();
            String artistName = adapter.getArtistName();
            notification.init(adapter.isPlaying(), musicName, artistName);
        }
    }

    private void registerNotificationBroadcastReceiver() {
        register = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PRE);
        intentFilter.addAction(ACTION_TOGGLE_PLAY);
        intentFilter.addAction(ACTION_STOP);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void updateNotification(String action) {
        NotificationAdapter adapter = getNotificationAdapter();
        if(adapter != null && notification != null) {
            String musicName = adapter.getMusicName();
            String artistName = adapter.getArtistName();
            notification.updateText(musicName, artistName);
            notification.update(adapter.isPlaying());
            if(!TextUtils.equals(action, ACTION_TOGGLE_PLAY) || TextUtils.isEmpty(notification.getCurrentImageUri())) {
                adapter.loadMusicImage(new NotificationAdapter.MusicImageLoadListener() {
                    @Override
                    public void onMusicImageLoaded(String imgUrl, Bitmap bitmap) {
                        notification.updateImage(imgUrl, bitmap);
                    }
                });
            }
        }
    }

    private void dismissNotification() {
        hasShowNotification = false;
        if(notification != null) {
            notification.close();
            notification = null;
        }

        this.stopForeground(true);
    }

    protected NotificationAdapter getNotificationAdapter() {
        return player.getNotificationAdapter();
    }

    @Override
    public void onPlayStateChange(boolean play) {
        updateNotification(ACTION_TOGGLE_PLAY);
    }

    @Override
    public void onDestroy() {
        if(register) {
            unregisterReceiver(mBroadcastReceiver);
        }
        if(audioHelper != null) {
            audioHelper.destroy();
        }
        dismissNotification();
        player = null;
        playerEngine.stop();
        playerEngine = null;
        super.onDestroy();
    }
}
