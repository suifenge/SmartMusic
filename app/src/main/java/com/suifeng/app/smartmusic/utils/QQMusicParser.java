package com.suifeng.app.smartmusic.utils;


/**
 * 解析QQ音乐的数据
 */
public class QQMusicParser {

    public static String getMusicId(String f) {
        String[] dataArray = getArrayData(f);
        if(dataArray == null) {
            return null;
        }
        return dataArray[0];
    }

    public static long getMusicAlbumId(String f) {
        String[] dataArray = getArrayData(f);
        if(dataArray == null) {
            return -1;
        }
        return Long.parseLong(dataArray[4]);
    }

    public static long getDuration(String f) {
        String[] dataArray = getArrayData(f);
        if(dataArray == null) {
            return -1;
        }
        return Long.parseLong(dataArray[7]) * 1000;
    }

    //暂不确定是第几个参数
    public static long getMusicSize(String f) {
        String[] dataArray = getArrayData(f);
        if(dataArray == null) {
            return -1;
        }
        return Long.parseLong(dataArray[6]);
    }

    private static String[] getArrayData(String f) {
        String[] dataArray = f.split("\\|");
        if(dataArray.length < 2) {
            return null;
        }
        return dataArray;
    }

}
