package com.suifeng.app.smartmusic.activity;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iflytek.cloud.util.ContactManager;
import com.jakewharton.rxbinding2.view.RxView;
import com.suifeng.app.smartmusic.R;
import com.suifeng.app.smartmusic.entity.AIUIResult;
import com.suifeng.app.smartmusic.manager.AIUIManager;
import com.suifeng.app.smartmusic.presenter.MusicSearchPresenter;
import com.suifeng.app.smartmusic.utils.AIUIUtils;
import com.suifeng.app.smartmusic.utils.ToastUtils;
import com.suifeng.app.smartmusic.view.MusicSearchView;
import com.suifeng.lib.playerengine.api.PlaybackMode;
import com.suifeng.lib.playerengine.api.PlayerListener;
import com.suifeng.lib.playerengine.core.Player;
import com.suifeng.lib.playerengine.entity.Music;
import com.suifeng.library.base.eventbus.EventCenter;
import com.suifeng.library.base.log.LogUtil;
import com.suifeng.library.base.netstatus.NetUtils;
import com.suifeng.library.base.ui.BaseAppManager;
import com.suifeng.library.base.ui.activity.BaseAppCompatActivity;
import com.suifeng.library.base.widget.voiceline.VoiceLineView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;

public class MainActivity extends BaseAppCompatActivity implements AIUIManager.ResultListener, MusicSearchView {

    private AIUIManager aiuiManager;
    @BindView(R.id.tv_info)
    TextView infoTv;
    @BindView(R.id.iv_start_voice)
    ImageView startVoiceIv;
    @BindView(R.id.voice_line_view)
    VoiceLineView voiceLine;
    private RxPermissions permissions;

    private Player player;

    private MusicSearchPresenter presenter;
    private AudioManager mAudioManager;

    private boolean playingBefore;

    @Override
    protected void getBundleExtras(Bundle extras) {

    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViewsAndEvents() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        grantAppStartPermission();
        presenter = new MusicSearchPresenter(this);
        presenter.attachView(this);
        infoTv.setText(R.string.greetings_msg);
        initPlayer();
        RxView.clicks(startVoiceIv)
              .throttleFirst(2, TimeUnit.SECONDS)
              .subscribe(Void -> startAIUI());

    }

    private void initPlayer() {
        player = Player.getInstance(this);
        player.setPlayNextWhenError(true);
        player.setPlaybackMode(PlaybackMode.ALL);
        player.setFadeVolumeWhenStartOrPause(false);    //
        player.setShowNotification(false);
        player.acquireWifiLock();
    }

    private void startAIUI() {
        permissions.request(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECORD_AUDIO)
                .subscribe(grant -> {
                    if(grant) {
                        if(aiuiManager == null) {
                            return;
                        }
                        playingBefore = player.isPlaying();
                        if(player.isPlaying()) {
                            player.pause();
                        }
                        aiuiManager.startListen();
                    }
                    else {
                        showToast("please grant permissions");
                    }
                });
    }

    private void voiceViewState(boolean visible) {
        if(visible) {
            startVoiceIv.setVisibility(View.VISIBLE);
            voiceLine.setVisibility(View.GONE);
        }
        else {
            startVoiceIv.setVisibility(View.GONE);
            voiceLine.setVisibility(View.VISIBLE);
        }
    }

    private void grantAppStartPermission() {
        permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS)
                .subscribe(grant ->{
                    if(grant) {
                        initAiUI();
                    }
                    else {
                        showToast("permission is not granted");
                        finish();
                    }
                });
    }

    private void initAiUI() {
        aiuiManager = AIUIManager.getInstance();
        aiuiManager.setResultListener(this);
    }

    @Override
    public void onAIUIResult(String result) {
        if(playingBefore) {
            player.resume();
        }
        AIUIResult aiuiResult = AIUIUtils.parseAIUIResult(result);
        if(aiuiResult == null) {
            aiuiManager.stopListen();
            infoTv.setText(R.string.not_understand);
            aiuiManager.readText(getString(R.string.not_understand), this::startAIUI);
            return;
        }
        int cmdName = aiuiResult.getCmdName();
        if(cmdName == AIUIResult.PLAY) {
            infoTv.setText(aiuiResult.getSourceText());
            Bundle data = aiuiResult.getData();
            int musicDataType = data.getInt(AIUIResult.MUSIC_DATA_TYPE);
            if(musicDataType == AIUIResult.MUSIC_DATA_TYPE_VALUE_SONG) {
                String song = data.getString(AIUIResult.MUSIC_DATA_SONG);
                presenter.searchMusicByNameOrArtist(song);
            } else if(musicDataType == AIUIResult.MUSIC_DATA_TYPE_VALUE_ARTIST) {
                String artist = data.getString(AIUIResult.MUSIC_DATA_ARTIST);
                presenter.searchMusicByNameOrArtist(artist);

            } else if(musicDataType == AIUIResult.MUSIC_DATA_TYPE_VALUE_ALL) {
                String song = data.getString(AIUIResult.MUSIC_DATA_SONG);
                String artist = data.getString(AIUIResult.MUSIC_DATA_ARTIST);
                presenter.searchMusicByNameOrArtist(artist + "-" + song);
            }
        } else if(cmdName == AIUIResult.INSTRUCTION) {
            doInstruction(aiuiResult.getCmdValue());
        } else if(cmdName == AIUIResult.DIAL) {

        }
    }

    private void doInstruction(int cmdValue) {
        switch (cmdValue) {
            case AIUIResult.VALUE_PLAY:
                if(player.isPlaying()) {
                    return;
                }
                if(player.getCurrentIndex() == -1) {
                    aiuiManager.readText("请填充播放列表", null);
                    return;
                }
                player.play();
                break;
            case AIUIResult.VALUE_PAUSE:
                if(player.isPlaying()) {
                    player.pause();
                }
                break;
            case AIUIResult.VALUE_NEXT:
                if(player.getCurrentIndex() == -1) {
                    aiuiManager.readText("请填充播放列表", null);
                    return;
                }
                player.next();
                break;
            case AIUIResult.VALUE_PRE:
                if(player.getCurrentIndex() == -1) {
                    aiuiManager.readText("请填充播放列表", null);
                    return;
                }
                player.prev();
                break;
            case AIUIResult.VALUE_PLAY_ALL:
                player.setPlaybackMode(PlaybackMode.ALL);
                break;
            case AIUIResult.VALUE_PLAY_SHUFFLE:
                player.setPlaybackMode(PlaybackMode.SHUFFLE);
                break;
            case AIUIResult.VALUE_PLAY_SINGLE:
                player.setPlaybackMode(PlaybackMode.SINGLE_REPEAT);
                break;
            case AIUIResult.VALUE_VOLUME_ADD:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,
                        AudioManager.FX_FOCUS_NAVIGATION_UP);
                break;
            case AIUIResult.VALUE_VOLUME_MAX:
                int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
                break;
            case AIUIResult.VALUE_VOLUME_SUB:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,
                        AudioManager.FX_FOCUS_NAVIGATION_UP);
                break;
            case AIUIResult.VALUE_VOLUME_MIN:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                break;
            case AIUIResult.VALUE_CALL_PHONE:
                break;
        }
    }

    @Override
    public void searchEmpty() {
        infoTv.setText(R.string.not_search_music);
        aiuiManager.readText(getString(R.string.not_search_music), null);
    }

    @Override
    public void searMusicList(List<Music> musicList) {
        player.setPlayMusicList(musicList);
        aiuiManager.readText(String.format(getString(R.string.play_music_answer), musicList.get(0).getArtist(), musicList.get(0).getTitle()), () -> player.play());
    }

    @Override
    public void onAIUIRecordState(boolean start) {
        voiceViewState(!start);
    }

    @Override
    public void onAIUISoundChange(int volume) {
        voiceLine.setVolume(volume);
    }

    @Override
    protected void onEventComming(EventCenter eventCenter) {

    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected void onNetworkConnected(NetUtils.NetType type) {

    }

    @Override
    protected void onNetworkDisConnected() {

    }

    @Override
    protected boolean isApplyStatusBarTranslucency() {
        return true;
    }

    @Override
    protected boolean isApplyStatusBarDarkFont() {
        return true;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    protected boolean toggleOverridePendingTransition() {
        return false;
    }

    @Override
    protected TransitionMode getOverridePendingTransitionMode() {
        return null;
    }

    private long exitTime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ToastUtils.showShortToast(R.string.press_again_to_exit);
                exitTime = System.currentTimeMillis();
            } else {
                if(aiuiManager != null) {
                    aiuiManager.destroyAll();
                }
                BaseAppManager.getInstance().exit(false);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
        player.releaseWifiLock();
    }
}
