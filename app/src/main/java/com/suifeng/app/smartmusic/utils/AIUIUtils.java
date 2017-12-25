package com.suifeng.app.smartmusic.utils;

import android.os.Bundle;

import com.suifeng.app.smartmusic.entity.AIUIResult;
import com.suifeng.library.base.log.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class AIUIUtils {

    public static AIUIResult parseAIUIResult(String resultStr) {
        LogUtil.i("mmp--->" + resultStr);
        try {
            AIUIResult result = new AIUIResult();
            JSONObject jsonObject = new JSONObject(resultStr);
            result.setSourceText(jsonObject.getString("text"));
            if(jsonObject.has("service")) {
                String service = jsonObject.getString("service");
                LogUtil.i("mmp--->" + service);
                if(service.equals("musicPlayer_smartHome")) {
                    // 先判断是数组还是对象
                    Object listArray = new JSONTokener(jsonObject.getString("semantic")).nextValue();
                    if(listArray instanceof JSONObject) {
                        JSONObject semanticObject = (JSONObject)listArray;
                        JSONObject slotsObject = semanticObject.getJSONObject("slots");
                        String attr = slotsObject.getString("attr");
                        String attrValue = slotsObject.getString("attrValue");
                        if(attr.equals("开关")) {
                            result.setCmdName(AIUIResult.INSTRUCTION);
                            if(attrValue.equals("播放")) {
                                result.setCmdValue(AIUIResult.VALUE_PLAY);
                            } else if(attrValue.equals("停止")) {
                                result.setCmdValue(AIUIResult.VALUE_PAUSE);
                            } else if(attrValue.equals("暂停")) {
                                result.setCmdValue(AIUIResult.VALUE_PAUSE);
                            }
                            return result;
                        } else if(attr.equals("歌曲顺序")) {
                            result.setCmdName(AIUIResult.INSTRUCTION);
                            if(attrValue.equals("上一首")) {
                                result.setCmdValue(AIUIResult.VALUE_PRE);
                            } else if(attrValue.equals("下一首")) {
                                result.setCmdValue(AIUIResult.VALUE_NEXT);
                            }
                            return result;
                        } else {
                            if(attrValue.equals("设置")) {
                                //歌曲模式的控制
                                result.setCmdName(AIUIResult.INSTRUCTION);
                                if(attr.equals("列表循环")) {
                                    result.setCmdValue(AIUIResult.VALUE_PLAY_ALL);
                                } else if(attr.equals("随机播放")) {
                                    result.setCmdValue(AIUIResult.VALUE_PLAY_SHUFFLE);
                                } else if(attr.equals("单曲循环")) {
                                    result.setCmdValue(AIUIResult.VALUE_PLAY_SINGLE);
                                }
                                return result;
                            }
                        }
                    }
                } else if(service.equals("musicX")) {
                    Object listArray = new JSONTokener(jsonObject.getString("semantic")).nextValue();
                    if(listArray instanceof JSONArray) {
                        JSONArray semanticArray = (JSONArray) listArray;
                        JSONObject object = semanticArray.getJSONObject(0);
                        String intent = object.getString("intent");
                        if(intent.equals("PLAY")) {
                            //搜索歌曲
                            result.setCmdName(AIUIResult.PLAY);
                            result.setCmdValue(AIUIResult.VALUE_PLAY);
                            JSONArray slots = object.getJSONArray("slots");
                            if(slots.length() == 1) {
                                //返回的数据只有歌手或者只有歌名
                                JSONObject valueObject = slots.getJSONObject(0);
                                String name = valueObject.getString("name");
                                String value = valueObject.getString("value");
                                Bundle data = new Bundle();
                                if(name.equals("artist")) {
                                    data.putInt(AIUIResult.MUSIC_DATA_TYPE, AIUIResult.MUSIC_DATA_TYPE_VALUE_ARTIST);
                                    data.putString(AIUIResult.MUSIC_DATA_ARTIST, value);
                                    result.setData(data);
                                    return result;

                                } else if(name.equals("song")) {
                                    data.putInt(AIUIResult.MUSIC_DATA_TYPE, AIUIResult.MUSIC_DATA_TYPE_VALUE_SONG);
                                    data.putString(AIUIResult.MUSIC_DATA_SONG, value);
                                    result.setData(data);
                                    return result;
                                }
                            }
                            else if(slots.length() == 2) {
                                Bundle data = new Bundle();
                                data.putInt(AIUIResult.MUSIC_DATA_TYPE, AIUIResult.MUSIC_DATA_TYPE_VALUE_ALL);
                                //第一个歌手对象
                                JSONObject valueObject1 = slots.getJSONObject(0);
                                String value1 = valueObject1.getString("value");
                                data.putString(AIUIResult.MUSIC_DATA_ARTIST, value1);
                                //第二个歌名对象
                                JSONObject valueObject2 = slots.getJSONObject(1);
                                String value2 = valueObject2.getString("value");
                                data.putString(AIUIResult.MUSIC_DATA_SONG, value2);
                                result.setData(data);
                                return result;
                            }
                        } else if(intent.equals("INSTRUCTION")) {
                            //上一曲/下一曲 控制
                            JSONArray slots = object.getJSONArray("slots");
                            JSONObject valueObject = slots.getJSONObject(0);
                            String name = valueObject.getString("name");
                            String value = valueObject.getString("value");
                            if(name.equals("insType")) {
                                result.setCmdName(AIUIResult.INSTRUCTION);
                                if(value.equals("past")) {
                                    result.setCmdValue(AIUIResult.VALUE_PRE);
                                } else if(value.equals("next")) {
                                    result.setCmdValue(AIUIResult.VALUE_NEXT);
                                }
                                return result;
                            }
                        }
                    }
                } else if(service.equals("cmd")) {
                    //主要是音量的控制
                    Object listArray = new JSONTokener(jsonObject.getString("semantic")).nextValue();
                    if(listArray instanceof JSONArray) {
                        JSONArray semanticArray = (JSONArray) listArray;
                        JSONObject object = semanticArray.getJSONObject(0);
                        String intent = object.getString("intent");
                        if(intent.equals("INSTRUCTION")) {
                            JSONArray slots = object.getJSONArray("slots");
                            JSONObject valueObject = slots.getJSONObject(0);
                            String name = valueObject.getString("name");
                            String value = valueObject.getString("value");
                            if(name.equals("insType")) {
                                result.setCmdName(AIUIResult.INSTRUCTION);
                                if(value.equals("volume_plus")) {
                                    result.setCmdValue(AIUIResult.VALUE_VOLUME_ADD);
                                } else if(value.equals("volume_max")) {
                                    result.setCmdValue(AIUIResult.VALUE_VOLUME_MAX);
                                } else if(value.equals("volume_minus")) {
                                    result.setCmdValue(AIUIResult.VALUE_VOLUME_SUB);
                                } else if(value.equals("volume_min")) {
                                    result.setCmdValue(AIUIResult.VALUE_VOLUME_MIN);
                                } else if(value.equals("pause")) {
                                    result.setCmdValue(AIUIResult.VALUE_PAUSE);
                                }
                                return result;
                            }
                        }
                    }
                } else if(service.equals("telephone")) {
                    JSONArray semanticArray = jsonObject.getJSONArray("semantic");
                    JSONObject object = semanticArray.getJSONObject(0);
                    String intent = object.getString("intent");
                    if(intent.equals("DIAL")) {
                        JSONArray slots = object.getJSONArray("slots");
                        result.setCmdName(AIUIResult.DIAL);
                        result.setCmdValue(AIUIResult.VALUE_CALL_PHONE);
                        if(slots.length() == 1) {
                            JSONObject valueObject = slots.getJSONObject(0);
                            String name = valueObject.getString("name");
                            String value = valueObject.getString("value");
                            Bundle data = new Bundle();
                            if (name.equals("name")) {
                                data.putInt(AIUIResult.CALL_PHONE_TYPE, AIUIResult.CALL_PHONE_NAME);
                                data.putString(AIUIResult.CONTACT_NAME, value);
                            } else if(name.equals("code")){
                                data.putInt(AIUIResult.CALL_PHONE_TYPE, AIUIResult.CALL_PHONE_CODE);
                                data.putString(AIUIResult.CONTACT_CODE, value);
                                result.setAnswerText(jsonObject.getJSONObject("answer").getString("text"));
                            }
                            return result;
                        } else if(slots.length() == 2) {
                            Bundle data = new Bundle();
                            data.putInt(AIUIResult.CALL_PHONE_TYPE, AIUIResult.CALL_PHONE_ALL);
                            //联系人号码
                            JSONObject valueObject1 = slots.getJSONObject(0);
                            String value1 = valueObject1.getString("value");
                            data.putString(AIUIResult.CONTACT_CODE, value1);
                            //联系人名字
                            JSONObject valueObject2 = slots.getJSONObject(1);
                            String value2 = valueObject2.getString("value");
                            data.putString(AIUIResult.CONTACT_NAME, value2);
                            result.setAnswerText(jsonObject.getJSONObject("answer").getString("text"));
                            return result;
                        }
                    }
                }
            } else {
                //做讯飞没有识别出来的语意分析
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

}
