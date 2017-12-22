package com.suifeng.app.smartmusic;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

public class CustomApplication extends Application{

    private static CustomApplication instance;

    public static CustomApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=5a1fd846");
    }
}
