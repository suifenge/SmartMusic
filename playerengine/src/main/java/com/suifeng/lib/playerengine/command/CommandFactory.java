package com.suifeng.lib.playerengine.command;

import android.content.Context;
import android.content.Intent;

import com.suifeng.lib.playerengine.core.PlayerService;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * build receiver cmd
 */

public class CommandFactory {

    private static CommandFactory instance;
    private Context mContext;

    public static CommandFactory getInstance(Context context) {
        if(instance == null) {
            instance = new CommandFactory(context.getApplicationContext());
        }
        return instance;
    }

    private CommandFactory(Context context) {
        this.mContext = context;
    }

    public void sendCommand(String command) {
        Intent intent = getIntent(mContext, command);
        mContext.startService(intent);
    }

    /**
     * sends custom receiver with specified data
     * @param command
     * @param extraMap
     */
    public void sendCommand(String command, Map<String, Object> extraMap) {
        Intent intent = getIntent(mContext, command);
        if(extraMap != null) {
            Set set = extraMap.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String key = (String) entry.getKey();
                Object obj = entry.getValue();
                if(obj instanceof Integer) {
                    intent.putExtra(key, (Integer) obj);
                } else if(obj instanceof String) {
                    intent.putExtra(key, (String) obj);
                } else if(obj instanceof Boolean) {
                    intent.putExtra(key, (Boolean) obj);
                } else if(obj instanceof Float) {
                    intent.putExtra(key, (Float) obj);
                }
            }
        }
        mContext.startService(intent);
    }

    private Intent getIntent(Context context, String action) {
        Intent intent = new Intent(action);
        intent.setClass(context, PlayerService.class);
        return intent;
    }

    public static class Command {
        public static final String ACTION_FIRST = "com.suifeng.first";
        public static final String ACTION_PLAY = "com.suifeng.play";
        public static final String ACTION_STOP = "com.suifeng.stop";
        public static final String ACTION_PAUSE = "com.suifeng.pause";
        public static final String ACTION_RESUME = "com.suifeng.resume";
        public static final String ACTION_SEEK_TO = "com.suifeng.seek_to";
        public static final String ACTION_NEXT = "com.suifeng.next";
        public static final String ACTION_PRE = "com.suifeng.pre";
        public static final String ACTION_SKIP_TO = "com.suifeng.skip_to";
        public static final String ACTION_SET_PLAYMODE = "com.suifeng.set_playmode";
        public static final String ACTION_SET_VOLUME = "com.suifeng.set_volume";
        public static final String ACTION_TOGGLE_PLAY = "com.suifeng.toggle_play";
        public static final String ACTION_WAKE_MODE = "com.suifeng.wake_mode";
        public static final String ACTION_FADE_VOLUME = "com.suifeng.fade_volume";
        public static final String ACTION_PLAY_NEXT_WHEN_ERROR = "com.suifeng.play_next_when_error";
        public static final String ACTION_SET_PLATLIST = "com.suifeng.set_playlist";
        public static final String ACTION_SET_LISTENER = "com.suifeng.set_listener";
        public static final String ACTION_BRING_TO_FRONT = "com.suifeng.bing_to_front";
        private Command(){}
    }

    public static class ExtraData {
        public static final String EXTRA_SEEK_PERCENT = "seek_percent";
        public static final String EXTRA_SKIP_INDEX = "skip_index";
        public static final String EXTRA_LEFT_VOLUME = "left_volume";
        public static final String EXTRA_RIGHT_VOLUME = "right_volume";
        public static final String EXTRA_CLOSE_NOTIFICATION = "close_notification";
        public static final String EXTRA_FROM = "from";
        private ExtraData(){}
    }
}
