package com.suifeng.lib.playerengine.core;

import android.util.Log;

import com.suifeng.lib.playerengine.api.PlaybackMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * manager play list
 */

public class PlayListManager {

    private static final String TAG = "PlayListManager";

    private ArrayList<Integer> playOrder = null;    // play list order
    private PlaybackMode playListPlaybackMode;      // play mode
    private ArrayList<String> playList;             // music uri list
    private int selected;

    public PlayListManager() {
        this.playListPlaybackMode = PlaybackMode.ALL;
        this.playList = null;
        this.selected = -1;
        this.playOrder = new ArrayList<>();
        this.playList = new ArrayList<>();
        calculateOrder(true);
    }

    public PlaybackMode getPlaylistPlaybackMode() {
        return this.playListPlaybackMode;
    }

    public void setPlayListPlaybackMode(PlaybackMode playbackMode) {
        if(playbackMode == null) {
            Log.w(TAG, "Playbackmode is null !");
        } else if(this.playListPlaybackMode != playbackMode) {
            boolean force = false;
            switch (playbackMode.ordinal()) {
                case 0:
                case 2:
                    if(playListPlaybackMode != PlaybackMode.SHUFFLE) {
                        force = true;
                    }
                    break;
                case 1:
                    force = true;
            }
            this.playListPlaybackMode = playbackMode;
            calculateOrder(force);
        }
    }

    public void addTrackUri(String uri) {
        playList.add(uri);
        this.playOrder.add(size() - 1);
    }

    public void removeTrackUri(String uri) {
        int position = playList.indexOf(uri);
        playList.remove(uri);
        playOrder.remove(position);
    }

    public void addTrackUris(List<String> uris) {
        int size = uris.size();
        for (int i = 0; i < size; i++) {
            addTrackUri(uris.get(i));
        }
    }

    public void clearTracks() {
        playList.clear();
        playOrder.clear();
        this.selected = -1;
        calculateOrder(true);
    }

    public boolean isEmpty() {
        return this.playList.size() == 0;
    }

    public void selectNext() {
        if(!isEmpty()) {
            ++selected;
            selected %= playList.size();    //do loop
        }
    }

    public void selectPrev() {
        if(!isEmpty()) {
            --selected;
            if(selected < 0) {
                selected = playList.size() - 1; // do loop
            }
        }
    }

    public void select(int index) {
        if(!isEmpty() && index >= 0 && index < playList.size()) {
            selected = playOrder.indexOf(index);
        }
    }

    public void selectOrAdd(String uri) {
        for (int i = 0; i < playList.size(); i++) {
            if(playList.get(i).equals(uri)) {
                select(i);
                return;
            }
        }
        addTrackUri(uri);
        select(playList.size() - 1);
    }

    private int getIndex() {
        if(isEmpty()) {
            this.selected = -1;
        }
        if(this.selected == -1 && !isEmpty()) {
            this.selected = 0;
        }
        return this.selected;
    }

    public int getSelectedIndex() {
        int index = getIndex();
        if(index != -1) {
            index = playOrder.get(index);
        }
        return index;
    }

    public String getSelectedUri() {
        int index = getIndex();
        if(index == -1) {
            return null;
        } else {
            index = playOrder.get(index);
            if(index  == -1) {
                return null;
            } else {
                return playList.get(index);
            }
        }
    }

    public int size() {
        return playList == null ? 0 : playList.size();
    }

    public String getTrackUri(int index) {
        if(index >= 0 && index < playList.size()) {
            return playList.get(index);
        } else {
            return null;
        }
    }

    public String[] getAllTracks() {
        return playList.toArray(new String[playList.size()]);
    }

    public void remove(int position) {
        if(playList != null && position < playList.size() && position >= 0) {
            if(selected >= position) {
                --selected;
            }
            playList.remove(position);
            playOrder.remove(position);
        }
    }

    /**
     * calculate play order
     * @param force
     */
    private void calculateOrder(boolean force) {
        if(playOrder.isEmpty() || force) {
            int oldSelected = 0;
            if(!playOrder.isEmpty()) {
                oldSelected = playOrder.get(selected);
                playOrder.clear();
            }
            for (int i = 0; i < size(); i++) {
                playOrder.add(i, i);
            }

            if(playListPlaybackMode == null) {
                playListPlaybackMode = PlaybackMode.ALL;
            }

            switch (playListPlaybackMode.ordinal()) {
                case 0:
                case 2:
                    this.selected = oldSelected;
                    break;
                case 1:
                    Collections.shuffle(this.playOrder);
                    this.selected = playOrder.indexOf(selected);
                    break;
            }
        }
    }

    public boolean isLastTrackOnList() {
        return selected == (size() - 1);
    }
}
