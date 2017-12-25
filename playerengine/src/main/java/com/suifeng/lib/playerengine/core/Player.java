package com.suifeng.lib.playerengine.core;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.view.WindowManager;

import com.suifeng.lib.playerengine.api.NotificationAdapter;
import com.suifeng.lib.playerengine.api.PlaybackMode;
import com.suifeng.lib.playerengine.api.PlayerEngine;
import com.suifeng.lib.playerengine.api.PlayerListener;
import com.suifeng.lib.playerengine.command.CommandFactory;
import com.suifeng.lib.playerengine.entity.Music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_FADE_VOLUME;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_FIRST;
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
import static com.suifeng.lib.playerengine.command.CommandFactory.ExtraData.EXTRA_LEFT_VOLUME;
import static com.suifeng.lib.playerengine.command.CommandFactory.ExtraData.EXTRA_RIGHT_VOLUME;
import static com.suifeng.lib.playerengine.command.CommandFactory.ExtraData.EXTRA_SEEK_PERCENT;
import static com.suifeng.lib.playerengine.command.CommandFactory.ExtraData.EXTRA_SKIP_INDEX;

/**
 * main player, user use
 */

public class Player implements PlayerEngine {

    private static final String TAG = "Player";
    private PlayerListener playerListener;
    private static Player instance;
    private Context mContext;
    private PlaybackMode playbackMode;
    private PlayListManager playListManager;    //manager player's music list
    private WifiManager.WifiLock wifiLock;      //wake phone, when play online music
    private PlayerEngineImpl playerEngine;
    private boolean fadeVolume;
    private boolean playNextWhenError;
    private boolean showNotification;
    private CommandFactory commandFactory;  //provide send custom receiver
    private NotificationAdapter notificationAdapter;
    private List<Object> playMusicList;

    public static Player getInstance(Context context) {
        if(instance == null) {
            instance = new Player(context.getApplicationContext());
        }
        return instance;
    }

    private Player(Context context) {
        this.mContext = context;
        playMusicList = new ArrayList<>();
        commandFactory = CommandFactory.getInstance(context);
        sendBaseCommand(ACTION_FIRST);
    }

    /**
     * set play list
     * @param musics need play list
     */
    public void setPlayMusicList(List<? extends Music> musics) {
        if(musics == null || musics.isEmpty()) {
            return;
        }
        if(playListManager == null) {
            playListManager = new PlayListManager();
        }
        playMusicList.addAll(musics);
        for (Music music : musics) {
            playListManager.addTrackUri(music.getUrl());
        }
        if(playerEngine.getPlayListManager() == null) {
            setPlayListManager(playListManager);
        }
    }

    /**
     * set play list and control play list
     * @param musics
     * @param clear
     */
    public void setPlayMusicList(List<? extends Music> musics, boolean clear) {
        if(musics == null || musics.isEmpty()) {
            return;
        }
        if(playListManager == null) {
            playListManager = new PlayListManager();
        }
        if(clear) {
            playMusicList.clear();
            playListManager.clearTracks();
        }
        playMusicList.addAll(musics);
        for (Music music : musics) {
            playListManager.addTrackUri(music.getUrl());
        }
        if(playerEngine.getPlayListManager() == null) {
            setPlayListManager(playListManager);
        }
    }

    public List<Object> getPlayMusicList() {
        return playMusicList;
    }

    public Object getCurrentMusic() {
        return (playListManager != null && !playMusicList.isEmpty()) ? playMusicList.get(playListManager.getSelectedIndex()) : null;
    }

    @Override
    public void setPlayListManager(PlayListManager playListManager) {
        this.playListManager = playListManager;
        sendBaseCommand(ACTION_SET_PLATLIST);
    }

    @Override
    public PlayListManager getPlayListManager() {
        return this.playListManager;
    }

    void setPlayerEngine(PlayerEngineImpl playerEngine) {
        this.playerEngine = playerEngine;
    }

    public int getAudioSessionId() {
        return playerEngine != null ? playerEngine.getAudioSessionId() : -1;
    }

    /**
     * @param notificationAdapter notification interface
     */
    public void setNotificationAdapter(NotificationAdapter notificationAdapter) {
        this.notificationAdapter = notificationAdapter;
    }

    /**
     * player service get notification interface
     * @return notification interface
     */
    public NotificationAdapter getNotificationAdapter() {
        return this.notificationAdapter;
    }

    public boolean isPlaying() {
        return playerEngine != null && playerEngine.isPlaying();
    }

    @Override
    public void play() {
        sendBaseCommand(ACTION_PLAY);
    }

    @Override
    public void pause() {
        sendBaseCommand(ACTION_PAUSE);
    }

    @Override
    public void resume() {
        sendBaseCommand(ACTION_RESUME);
    }

    @Override
    public boolean toggle() {
        sendBaseCommand(ACTION_TOGGLE_PLAY);
        return isPlaying();
    }

    @Override
    public void next() {
        sendBaseCommand(ACTION_NEXT);
    }

    @Override
    public void prev() {
        sendBaseCommand(ACTION_PRE);
    }

    @Override
    public void seekTo(int percent) {
        Map map = getExtraDataMap();
        map.put(EXTRA_SEEK_PERCENT, percent);
        commandFactory.sendCommand(ACTION_SEEK_TO, map);
    }

    @Override
    public void skipTo(int position) {
        Map map = getExtraDataMap();
        map.put(EXTRA_SKIP_INDEX, position);
        commandFactory.sendCommand(ACTION_SKIP_TO, map);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        Map map = getExtraDataMap();
        map.put(EXTRA_LEFT_VOLUME, leftVolume);
        map.put(EXTRA_RIGHT_VOLUME, rightVolume);
        commandFactory.sendCommand(ACTION_SET_VOLUME, map);
    }

    @Override
    public void setListener(PlayerListener playerListener) {
        this.playerListener = playerListener;
        sendBaseCommand(ACTION_SET_LISTENER);
    }

    @Override
    public PlayerListener getListener() {
        return this.playerListener;
    }

    @Override
    public void setPlaybackMode(PlaybackMode playbackMode) {
        this.playbackMode = playbackMode;
        sendBaseCommand(ACTION_SET_PLAYMODE);
    }

    /**
     * cycle playback mode
     */
    public void cyclePlayMode() {
        int index = getPlaybackMode().ordinal();
        index = ++ index % 3;
        switch (index) {
            case 0:
                setPlaybackMode(PlaybackMode.ALL);
                break;
            case 1:
                setPlaybackMode(PlaybackMode.SHUFFLE);
                break;
            case 2:
                setPlaybackMode(PlaybackMode.SINGLE_REPEAT);
                break;
        }
    }

    @Override
    public PlaybackMode getPlaybackMode() {
        return this.playbackMode;
    }

    @Override
    public void setWakeMode() {
        sendBaseCommand(ACTION_WAKE_MODE);
    }

    /**
     * keep screen on
     * @param act
     */
    public void setScreenOn(Activity act) {
        act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public String getCurrentTrackUri() {
        return playListManager != null ? playListManager.getSelectedUri() : null;
    }

    public int getCurrentIndex() {
        return playListManager != null ? playListManager.getSelectedIndex() : -1;
    }

    public void acquireWifiLock() {
        wifiLock = ((WifiManager) mContext.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "MyLock");
        wifiLock.acquire();
    }

    public void releaseWifiLock() {
        if(wifiLock != null) {
            wifiLock.release();
        }
    }

    @Override
    public void stop() {
        stop(true);
    }

    private void stop(boolean closeNotification) {
        Map map = getExtraDataMap();
        map.put(EXTRA_CLOSE_NOTIFICATION, closeNotification);
        commandFactory.sendCommand(ACTION_STOP, map);
        instance = null;
    }

    @Override
    public int getCurrentPlayPercent() {
        return playerEngine != null ? playerEngine.getCurrentPlayPercent() : 0;
    }

    @Override
    public void setFadeVolumeWhenStartOrPause(boolean isFadeVolume) {
        this.fadeVolume = isFadeVolume;
        sendBaseCommand(ACTION_FADE_VOLUME);
    }

    @Override
    public boolean isFadeVolumeWhenStartOrPause() {
        return this.fadeVolume;
    }

    @Override
    public void setPlayNextWhenError(boolean playNextWhenError) {
        this.playNextWhenError = playNextWhenError;
        sendBaseCommand(ACTION_PLAY_NEXT_WHEN_ERROR);
    }

    public void setShowNotification(boolean show) {
        this.showNotification = show;

    }

    public boolean isShowNotification() {
        return showNotification;
    }

    @Override
    public boolean isPlayNextWhenError() {
        return this.playNextWhenError;
    }

    private void sendBaseCommand(String command) {
        commandFactory.sendCommand(command, getExtraDataMap());
    }

    private Map<String, Object> getExtraDataMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(CommandFactory.ExtraData.EXTRA_FROM, "Player");
        return map;
    }
}
