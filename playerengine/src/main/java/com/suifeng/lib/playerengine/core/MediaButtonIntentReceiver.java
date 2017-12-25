package com.suifeng.lib.playerengine.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.suifeng.lib.playerengine.command.CommandFactory;

import java.util.HashMap;

import static android.view.KeyEvent.KEYCODE_HEADSETHOOK;
import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;
import static android.view.KeyEvent.KEYCODE_MEDIA_STOP;
import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_NEXT;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_PAUSE;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_PLAY;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_PRE;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_STOP;
import static com.suifeng.lib.playerengine.command.CommandFactory.Command.ACTION_TOGGLE_PLAY;
import static com.suifeng.lib.playerengine.command.CommandFactory.ExtraData.EXTRA_FROM;

/**
 * media button receiver
 */

public class MediaButtonIntentReceiver extends BroadcastReceiver{

    private long mLastClickTime = 0;
    private boolean mDown = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if("android.media.AUDIO_BECOMING_NOISY".equals(intentAction)) {
            sendCommand(context, ACTION_PAUSE);
        } else if(Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if(keyEvent == null) {
                return;
            }
            int keyCode = keyEvent.getKeyCode();
            int action = keyEvent.getAction();
            long eventTime = keyEvent.getEventTime();
            String command = null;
            switch (keyCode) {
                case KEYCODE_VOLUME_UP:
                    break;
                case KEYCODE_VOLUME_DOWN:
                    break;
                case KEYCODE_HEADSETHOOK:
                case KEYCODE_MEDIA_PLAY_PAUSE:
                    command = ACTION_TOGGLE_PLAY;
                    break;
                case KEYCODE_MEDIA_STOP:
                    command = ACTION_STOP;
                    break;
                case KEYCODE_MEDIA_NEXT:
                    command = ACTION_NEXT;
                    break;
                case KEYCODE_MEDIA_PREVIOUS:
                    command = ACTION_PRE;
                    break;
                case KEYCODE_MEDIA_PLAY:
                    command = ACTION_PLAY;
                    break;
                case KEYCODE_MEDIA_PAUSE:
                    command = ACTION_PAUSE;
                    break;
            }

            if(command != null) {
                if(action != 0) {
                    mDown = false;
                } else {
                    if(mDown) {
                        if(ACTION_TOGGLE_PLAY.equals(command) || ACTION_PLAY.equals(command) && mLastClickTime != 0) {
                            //do nothing
                        }
                    } else if(keyEvent.getRepeatCount() == 0) {
                        String comm = command;
                        if(keyCode == KEYCODE_HEADSETHOOK && eventTime - mLastClickTime < 300L) {
                            comm = ACTION_NEXT;
                            mLastClickTime = 0L;
                        } else {
                            mLastClickTime = eventTime;
                        }

                        sendCommand(context, comm);
                        mDown = true;
                    }
                }

                if(isOrderedBroadcast()) {
                    abortBroadcast();
                }
            }
        }
    }

    private void sendCommand(Context context, String command) {
        CommandFactory commandFactory = CommandFactory.getInstance(context);
        HashMap map = new HashMap();
        map.put(EXTRA_FROM, "MediaButtonIntentReceiver");
        commandFactory.sendCommand(command, map);
    }
}
