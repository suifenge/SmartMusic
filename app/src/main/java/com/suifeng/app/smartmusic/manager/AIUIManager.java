package com.suifeng.app.smartmusic.manager;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.suifeng.app.smartmusic.CustomApplication;
import com.suifeng.library.base.log.LogUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class AIUIManager {

    private static final String TAG = "AIUIManager";

    private static AIUIManager instance;
    private int mAIUIState;
    private AIUIAgent mAIUIAgent;
    private SpeechSynthesizer mTts;
    private CustomApplication ca;
    private ResultListener resultListener;
    private TTSCompleteListener ttsCompleteListener;

    private AIUIManager() {
        //创建AIUIAgent
        ca = CustomApplication.getInstance();
        mAIUIAgent = AIUIAgent.createAgent(ca.getApplicationContext(), getAIUIParams(), mAIUIListener);
        mTts = SpeechSynthesizer.createSynthesizer(ca.getApplicationContext(), initListener);
        setTtsParam();

        //发送`CMD_START`消息，使AIUI处于工作状态
        AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null, null);
        mAIUIAgent.sendMessage(startMsg);
    }

    public static AIUIManager getInstance() {
        if(instance == null) {
            instance = new AIUIManager();
        }
        return instance;
    }

    public void startListen() {
        // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
        if(AIUIConstant.STATE_WORKING !=   this.mAIUIState){
            AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(wakeupMsg);
        }

        // 打开AIUI内部录音机，开始录音
        String params = "sample_rate=16000,data_type=audio";
        AIUIMessage writeMsg = new AIUIMessage( AIUIConstant.CMD_START_RECORD, 0, 0, params, null );
        mAIUIAgent.sendMessage(writeMsg);
    }

    /**
     * 开始语音合成
     * @param text  需要合成的文字
     */
    public void readText(String text, TTSCompleteListener ttsCompleteListener) {
        this.ttsCompleteListener = ttsCompleteListener;
        int code = mTts.startSpeaking(text, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            Log.i(TAG, "语音合成失败,错误码: " + code);
        }
    }

    public void setResultListener(ResultListener resultListener) {
        this.resultListener = resultListener;
    }

    public void stopListen() {
        AIUIMessage writeMsg = new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, null, null );
        mAIUIAgent.sendMessage(writeMsg);
    }

    private AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {
            switch (event.eventType) {
                case AIUIConstant.EVENT_WAKEUP:
                    Log.d( TAG,  "进入识别状态---on event: "+ event.eventType );
                    break;

                case AIUIConstant.EVENT_RESULT: {
                    //解析结果
                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));

                            String sub = params.optString("sub");
                            if ("nlp".equals(sub)) {
                                // 解析得到语义结果
                                String resultStr = cntJson.optString("intent");
                                if(TextUtils.isEmpty(resultStr) || resultStr.equals("{}")) {
                                    return;
                                }
                                if(resultListener != null) {
                                    resultListener.onAIUIResult(resultStr);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Log.d( TAG, "解析出错---"+ e.getLocalizedMessage() );
                    }
                } break;

                case AIUIConstant.EVENT_ERROR: {
                    Log.d( TAG,  "on event: "+ event.eventType );
                    Log.d( TAG, "错误: "+event.arg1+"\n"+event.info);
                } break;

                case AIUIConstant.EVENT_VAD: {
                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        Log.d("wtf", "找到vad_bos");
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        Log.d("wtf", "找到vad_eos");
                    } else {
                        Log.d("wtf", event.arg2+"");
                        if(resultListener != null) {
                            resultListener.onAIUISoundChange(event.arg2);
                        }
                    }
                } break;

                case AIUIConstant.EVENT_START_RECORD: {
                    Log.d( TAG,  "开始录音---on event: "+ event.eventType );
                    if(resultListener != null) {
                        resultListener.onAIUIRecordState(true);
                    }
                } break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                    Log.d( TAG,  "停止录音---on event: "+ event.eventType );
                    if(resultListener != null) {
                        resultListener.onAIUIRecordState(false);
                    }
                } break;

                case AIUIConstant.EVENT_STATE: {    // 状态事件
                    mAIUIState = event.arg1;

                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                        Log.d(TAG, "STATE_IDLE");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                        Log.d(TAG, "STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                        Log.d(TAG, "STATE_WORKING");
                    }
                } break;

                case AIUIConstant.EVENT_CMD_RETURN:{
                    if( AIUIConstant.CMD_UPLOAD_LEXICON == event.arg1 ){
                        Log.d(TAG, "上传"+ (0==event.arg2?"成功":"失败"));
                    }
                } break;

                default:
                    break;
            }
        }
    };

    public void destroyAll() {
        if(mAIUIAgent != null) {
            mAIUIAgent.destroy();
            mAIUIAgent = null;
        }
        if(mTts != null) {
            mTts.destroy();
            mTts = null;
        }
        instance = null;
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
        }

        @Override
        public void onSpeakPaused() {
        }

        @Override
        public void onSpeakResumed() {
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
        }

        @Override
        public void onCompleted(SpeechError error) {
            if(ttsCompleteListener != null) {
                ttsCompleteListener.onTTSComplete();
                ttsCompleteListener = null;
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };

    private void setTtsParam(){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "30");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "100");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, ca.getApplicationContext().getCacheDir()+"/msc/tts.wav");
    }

    /**
     * 初始化监听
     */
    private InitListener initListener = code -> {
        if (code != ErrorCode.SUCCESS) {
            Log.d(TAG, "初始化TTS失败,错误码："+code);
        }
    };

    private String getAIUIParams() {
        String params = "";
        AssetManager assetManager = ca.getApplicationContext().getResources().getAssets();
        try {
            InputStream ins = assetManager.open( "cfg/aiui_phone.cfg" );
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return params;
    }

    public interface ResultListener {
        void onAIUIResult(String result);
        void onAIUIRecordState(boolean start);
        void onAIUISoundChange(int volume);
    }

    public interface TTSCompleteListener {
        void onTTSComplete();
    }
}
