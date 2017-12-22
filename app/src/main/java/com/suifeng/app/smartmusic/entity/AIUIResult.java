package com.suifeng.app.smartmusic.entity;

import android.os.Bundle;

public class AIUIResult {

    public static final int PLAY = 0;
    public static final int INSTRUCTION = 1;
    public static final int DIAL = 2;

    public static final int VALUE_PLAY = 0;
    public static final int VALUE_PAUSE = 1;
    public static final int VALUE_NEXT = 2;
    public static final int VALUE_PRE = 3;
    public static final int VALUE_PLAY_ALL = 4;
    public static final int VALUE_PLAY_SHUFFLE = 5;
    public static final int VALUE_PLAY_SINGLE = 6;
    public static final int VALUE_VOLUME_ADD = 7;
    public static final int VALUE_VOLUME_MAX = 8;
    public static final int VALUE_VOLUME_SUB = 9;
    public static final int VALUE_VOLUME_MIN = 10;
    public static final int VALUE_CALL_PHONE = 11;

    public static final String MUSIC_DATA_TYPE = "music_data_type";
    public static final int MUSIC_DATA_TYPE_VALUE_ARTIST = 0;
    public static final int MUSIC_DATA_TYPE_VALUE_SONG = 1;
    public static final int MUSIC_DATA_TYPE_VALUE_ALL = 2;

    public static final String MUSIC_DATA_SONG = "music_data_song";
    public static final String MUSIC_DATA_ARTIST = "music_data_artist";

    public static final String CALL_PHONE_TYPE = "call_phone_type";
    public static final int CALL_PHONE_NAME = 0;
    public static final int CALL_PHONE_CODE = 1;
    public static final int CALL_PHONE_ALL = 2;

    public static final String CONTACT_NAME ="contact_name";
    public static final String CONTACT_CODE ="contact_code";

    private int cmdName;
    private int cmdValue;

    private String sourceText;
    private String answerText;
    private Bundle data;

    public int getCmdName() {
        return cmdName;
    }

    public void setCmdName(int cmdName) {
        this.cmdName = cmdName;
    }

    public int getCmdValue() {
        return cmdValue;
    }

    public void setCmdValue(int cmdValue) {
        this.cmdValue = cmdValue;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public Bundle getData() {
        return data;
    }

    public void setData(Bundle data) {
        this.data = data;
    }
}
