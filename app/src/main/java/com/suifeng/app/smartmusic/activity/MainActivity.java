package com.suifeng.app.smartmusic.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.suifeng.app.smartmusic.R;
import com.suifeng.app.smartmusic.entity.AIUIResult;
import com.suifeng.app.smartmusic.entity.Contact;
import com.suifeng.app.smartmusic.manager.AIUIManager;
import com.suifeng.app.smartmusic.presenter.MusicSearchPresenter;
import com.suifeng.app.smartmusic.utils.AIUIUtils;
import com.suifeng.app.smartmusic.utils.ContactUtil;
import com.suifeng.app.smartmusic.utils.HanziToPinyin;
import com.suifeng.app.smartmusic.utils.ToastUtils;
import com.suifeng.app.smartmusic.view.MusicSearchView;
import com.suifeng.lib.playerengine.api.LoadMusicListener;
import com.suifeng.lib.playerengine.api.PlaybackMode;
import com.suifeng.lib.playerengine.api.PlayerListener;
import com.suifeng.lib.playerengine.core.PlayListManager;
import com.suifeng.lib.playerengine.core.Player;
import com.suifeng.lib.playerengine.entity.Music;
import com.suifeng.lib.playerengine.util.AudioHelper;
import com.suifeng.lib.playerengine.util.MusicLoader;
import com.suifeng.library.base.eventbus.EventCenter;
import com.suifeng.library.base.log.LogUtil;
import com.suifeng.library.base.net.RxSchedulers;
import com.suifeng.library.base.netstatus.NetUtils;
import com.suifeng.library.base.ui.BaseAppManager;
import com.suifeng.library.base.ui.activity.BaseAppCompatActivity;
import com.suifeng.library.base.widget.voiceline.VoiceLineView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class MainActivity extends BaseAppCompatActivity implements AIUIManager.ResultListener, MusicSearchView, PlayerListener {

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
    private Music currentMusic;

    private TelephonyManager telephonyManager;
    private MyPhoneStateListener phoneStateListener;
    private String phoneNumber;

    private ArrayList<Contact> contacts;

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
        player.setFadeVolumeWhenStartOrPause(false);
        player.setShowNotification(false);
        player.setListener(this);
        player.acquireWifiLock();
        MusicLoader musicLoader = MusicLoader.instance(getContentResolver());
        musicLoader.loadMusic(new LoadMusicListener() {
            @Override
            public void onLoadMusic(List<Music> musics) {
                player.setPlayMusicList(musics);
            }

            @Override
            public void onLoadingMusic(final Music music) {

            }
        });
    }

    private void startAIUI() {
        permissions.request(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECORD_AUDIO)
                .subscribe(grant -> {
                    if (grant) {
                        if (aiuiManager == null) {
                            return;
                        }
                        playingBefore = player.isPlaying();
                        if (player.isPlaying()) {
                            player.pause();
                        }
                        aiuiManager.startListen();
                    } else {
                        showToast("please grant permissions");
                    }
                });
    }

    private void voiceViewState(boolean visible) {
        if (visible) {
            startVoiceIv.setVisibility(View.VISIBLE);
            voiceLine.setVisibility(View.GONE);
        } else {
            startVoiceIv.setVisibility(View.GONE);
            voiceLine.setVisibility(View.VISIBLE);
        }
    }

    private void grantAppStartPermission() {
        permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE)
                .subscribe(grant -> {
                    if (grant) {
                        initAiUI();
                    } else {
                        showToast("permission is not granted");
                        finish();
                    }
                });
    }

    private void initAiUI() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new MyPhoneStateListener();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        aiuiManager = AIUIManager.getInstance();
        aiuiManager.setResultListener(this);
    }

    @Override
    public void onAIUIResult(String result) {
        AIUIResult aiuiResult = AIUIUtils.parseAIUIResult(result);
        if (aiuiResult == null) {
            aiuiManager.stopListen();
            Observable.timer(500, TimeUnit.MILLISECONDS)
                    .compose(bindToLifecycle())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        infoTv.setText(R.string.not_understand);
                        aiuiManager.readText(getString(R.string.not_understand), this::startAIUI);
                    });
            return;
        }
        aiuiManager.stopListen();
        int cmdName = aiuiResult.getCmdName();
        if (cmdName == AIUIResult.PLAY) {
            infoTv.setText(aiuiResult.getSourceText());
            Bundle data = aiuiResult.getData();
            int musicDataType = data.getInt(AIUIResult.MUSIC_DATA_TYPE);
            if (musicDataType == AIUIResult.MUSIC_DATA_TYPE_VALUE_SONG) {
                String song = data.getString(AIUIResult.MUSIC_DATA_SONG);
                presenter.searchQQMusicByNameOrArtist(song);
            } else if (musicDataType == AIUIResult.MUSIC_DATA_TYPE_VALUE_ARTIST) {
                String artist = data.getString(AIUIResult.MUSIC_DATA_ARTIST);
                presenter.searchQQMusicByNameOrArtist(artist);

            } else if (musicDataType == AIUIResult.MUSIC_DATA_TYPE_VALUE_ALL) {
                String song = data.getString(AIUIResult.MUSIC_DATA_SONG);
                String artist = data.getString(AIUIResult.MUSIC_DATA_ARTIST);
                presenter.searchQQMusicByNameOrArtist(artist + "-" + song);
            }
        } else if (cmdName == AIUIResult.INSTRUCTION) {
            doInstruction(aiuiResult.getCmdValue());
        } else if (cmdName == AIUIResult.DIAL) {
            int cmdValue = aiuiResult.getCmdValue();
            if(cmdValue == AIUIResult.VALUE_CALL_PHONE) {
                permissions.request(Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE)
                        .subscribe(grant -> {
                            if (grant) {
                                doDialAction(aiuiResult);
                            } else {
                                showToast("permissions is not granted");
                            }
                        });
            }
            else if(cmdValue == AIUIResult.VALUE_DIAL_ANSWER) {
                Bundle data = aiuiResult.getData();
                int answer = data.getInt(AIUIResult.DIAL_ANSWER_DATA);
                if(answer == AIUIResult.DIAL_ANSWER_CONFIRM) {
                    dialNumber(phoneNumber);
                } else if(answer == AIUIResult.DIAL_ANSWER_CANCEL) {
                    aiuiManager.readText(aiuiResult.getAnswerText(), () -> aiuiManager.startListen());
                } else if(answer == AIUIResult.DIAL_ANSWER_QUIT) {
                    aiuiManager.stopListen();
                    infoTv.setText(R.string.greetings_msg);
                } else if(answer == AIUIResult.DIAL_ANSWER_SEQUENCE) {
                    int sequence = data.getInt(AIUIResult.ANSWER_SEQUENCE);
                    if(sequence > 0 && sequence <= contactsSize) {
                        Contact contact = contactList.get(sequence - 1);
                        dialNumber(contact.getNumber());
                    } else {
                        aiuiManager.readText(String.format(getString(R.string.sequence_not_found), sequence+""), () -> aiuiManager.startListen());
                    }
                }
            }
        } else if (cmdName == AIUIResult.USER_ANSWER) {

        }
    }

    private void dialNumber(String number) {
        if (!TextUtils.isEmpty(number)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                showToast("call phone permission is not granted");
                phoneNumber = "";
                return;
            }
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            startActivity(intent);
            phoneNumber = "";
        }
    }

    private void resumePlay() {
        if(playingBefore) {
            player.resume();
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
                resumePlay();
                player.setPlaybackMode(PlaybackMode.ALL);
                break;
            case AIUIResult.VALUE_PLAY_SHUFFLE:
                resumePlay();
                player.setPlaybackMode(PlaybackMode.SHUFFLE);
                break;
            case AIUIResult.VALUE_PLAY_SINGLE:
                resumePlay();
                player.setPlaybackMode(PlaybackMode.SINGLE_REPEAT);
                break;
            case AIUIResult.VALUE_VOLUME_ADD:
                resumePlay();
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,
                        AudioManager.FX_FOCUS_NAVIGATION_UP);
                break;
            case AIUIResult.VALUE_VOLUME_MAX:
                resumePlay();
                int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
                break;
            case AIUIResult.VALUE_VOLUME_SUB:
                resumePlay();
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,
                        AudioManager.FX_FOCUS_NAVIGATION_UP);
                break;
            case AIUIResult.VALUE_VOLUME_MIN:
                resumePlay();
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                break;
        }
    }

    private void doDialAction(AIUIResult result) {
        Bundle data = result.getData();
        int callPhoneType = data.getInt(AIUIResult.CALL_PHONE_TYPE);
        switch (callPhoneType) {
            case AIUIResult.CALL_PHONE_NAME:
                String name = data.getString(AIUIResult.CONTACT_NAME);
                searchContactByName(name);
                break;
            case AIUIResult.CALL_PHONE_CODE:
            case AIUIResult.CALL_PHONE_ALL:
                phoneNumber = data.getString(AIUIResult.CONTACT_CODE);
                aiuiManager.readText(result.getAnswerText(), () -> {
                    aiuiManager.startListen();
                });
                break;
        }
        infoTv.setText(result.getAnswerText());
    }

    private int contactsSize = 0;
    private List<Contact> contactList = new ArrayList<>();
    private void searchContactByName(String name) {
        Observable.create((ObservableOnSubscribe<List<Contact>>) e -> {
            if(contacts == null) {
                contacts = ContactUtil.getAllContacts(getContentResolver());
            }
            List<Contact> searchContacts = ContactUtil.search(name, contacts);
            e.onNext(searchContacts);
            e.onComplete();
        })
        .compose(RxSchedulers.compose(this))
        .subscribe(searchContacts -> {
            contactList.clear();
            contactList.addAll(searchContacts);
            contactsSize = searchContacts.size();
            if (searchContacts.isEmpty() || searchContacts.size() == 0) {
                String text = String.format(getString(R.string.not_found_contact), name);
                infoTv.setText(text);
                aiuiManager.readText(text, null);
            } else if(searchContacts.size() == 1){
                Contact contact = searchContacts.get(0);
                phoneNumber = contact.getNumber();
                String text = String.format(getString(R.string.ensure_dial_contact), contact.getName() + contact.getNumber());
                infoTv.setText(text);
                aiuiManager.readText(text, () -> aiuiManager.startListen());
            } else if(searchContacts.size() > 1) {
                String text = String.format(getString(R.string.found_multiple_contacts), searchContacts.size()+"", name);
                aiuiManager.readText(text, () -> aiuiManager.startListen());
                StringBuilder contactString = new StringBuilder();
                for (Contact contact : searchContacts) {
                    contactString.append(contact.getNumber() + "\n");
                }
                infoTv.setText(text + "\n" + contactString);
            }
        });
    }

    @Override
    public void searchEmpty() {
        infoTv.setText(R.string.not_search_music);
        aiuiManager.readText(getString(R.string.not_search_music), null);
    }

    @Override
    public void searMusicList(List<Music> musicList) {
        if(player.isPlaying()) {
            player.pause();
        }
        PlayListManager playListManager = player.getPlayListManager();
        int lastIndex = -1;
        if(playListManager != null) {
            lastIndex = playListManager.getAllTracks().length;
        }
        player.setPlayMusicList(musicList);
        if(lastIndex != -1) {
            playListManager.select(lastIndex);
        }
        aiuiManager.readText(String.format(getString(R.string.play_music_answer), musicList.get(0).getArtist(), musicList.get(0).getSong()), () -> {
            player.play();
            currentMusic = musicList.get(0);
            infoTv.setText(currentMusic.getTitle());
        });
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
        if(aiuiManager != null) {
            aiuiManager.destroyAll();
        }
        aiuiManager = null;
        if(player != null) {
            player.stop();
        }
        player = null;
        if(telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    @Override
    public void onTrackBuffering(String uri, int percent) {

    }

    @Override
    public void onTrackStart(String uri) {
        currentMusic = (Music) player.getCurrentMusic();
        infoTv.setText(currentMusic.getTitle());
    }

    @Override
    public void onTrackChange(String uri) {
        currentMusic = (Music) player.getCurrentMusic();
        infoTv.setText(currentMusic.getTitle());
    }

    @Override
    public void onTrackProgress(String uri, int percent, int currentDuration, int duration) {

    }

    @Override
    public void onTrackPause(String uri) {

    }

    @Override
    public void onTrackStop(String uri) {

    }

    @Override
    public void onTrackStreamError(String uri, int what, int extra) {

    }

    private class MyPhoneStateListener extends PhoneStateListener {
        private MyPhoneStateListener(){}

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if(playingBefore) {
                        player.resume();
                    }
                    infoTv.setText(R.string.greetings_msg);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
            }
        }
    }
}
