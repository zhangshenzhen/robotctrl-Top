package com.brick.robotctrl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kjn.askquestion.AccountInfo;
import com.kjn.askquestion.Jason;
import com.kjn.askquestion.JsonBean;
import com.sinovoice.hcicloudsdk.android.asr.recorder.ASRRecorder;
import com.sinovoice.hcicloudsdk.api.HciCloudSys;
import com.sinovoice.hcicloudsdk.common.AuthExpireTime;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.InitParam;
import com.sinovoice.hcicloudsdk.common.asr.AsrConfig;
import com.sinovoice.hcicloudsdk.common.asr.AsrGrammarId;
import com.sinovoice.hcicloudsdk.common.asr.AsrInitParam;
import com.sinovoice.hcicloudsdk.common.asr.AsrRecogResult;
import com.sinovoice.hcicloudsdk.recorder.ASRCommonRecorder;
import com.sinovoice.hcicloudsdk.recorder.ASRRecorderListener;
import com.sinovoice.hcicloudsdk.recorder.RecorderEvent;

import org.apache.commons.httpclient.HttpException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by ${kang} on 2016/6/20.
 */

public class QuestActivity extends Activity{
    private static final String TAG = "MainActivity";

    /**
     * 加载用户信息工具类
     */
    private AccountInfo mAccountInfo;

    private TextView mResult;
    private TextView mState;
    private TextView mError;
    private ListView mGrammarLv;
    private Button mBtnRecogRealTimeMode;
    public String query;
    public String result;
    public String data;

    public String resultShow;

    private ASRRecorder mAsrRecorder;

    private String grammar = null;

    private static class WeakRefHandler extends Handler {          //可以避免内存泄漏的Handler库
        private WeakReference<QuestActivity> ref = null;

        public WeakRefHandler(QuestActivity activity) {
            ref = new WeakReference<QuestActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (ref.get() != null) {
                switch (msg.arg1) {
                    case 1:
                        if (!msg.obj.toString().equalsIgnoreCase(""))
                            ref.get().mState.setText(msg.obj.toString());
                        break;
                    case 2:
                        if (!msg.obj.toString().equalsIgnoreCase(""))
                            ref.get().mResult.setText(msg.obj.toString());

                        break;
                    case 3:
                        if (!msg.obj.toString().equalsIgnoreCase(""))
                            ref.get().mError.setText(msg.obj.toString());
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static Handler mUIHandle = null;               //主要接受子线程发送的数据， 并用此数据配合主线程更新UI。

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResult = (TextView) findViewById(R.id.resultview);
        mState = (TextView) findViewById(R.id.stateview);
        mError = (TextView) findViewById(R.id.errorview);
        mGrammarLv = (ListView) findViewById(R.id.grammar_list);
        mBtnRecogRealTimeMode = (Button) findViewById(R.id.begin_recog_real_time_mode);


        mUIHandle = new WeakRefHandler(this);

        mAccountInfo = AccountInfo.getInstance();
        boolean loadResult = mAccountInfo.loadAccountInfo(this);
        if (loadResult) {
            // 加载信息成功进入主界面
            Toast.makeText(getApplicationContext(), "加载灵云账号成功",
                    Toast.LENGTH_SHORT).show();
        } else {
            // 加载信息失败，显示失败界面
            Toast.makeText(getApplicationContext(), "加载灵云账号失败！请在assets/AccountInfo.txt文件中填写正确的灵云账户信息，账户需要从www.hcicloud.com开发者社区上注册申请。",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 加载信息,返回InitParam, 获得配置参数的字符串
        InitParam initParam = getInitParam();
        String strConfig = initParam.getStringConfig();
        Log.i(TAG,"\nhciInit config:" + strConfig);

        // 初始化
        int errCode = HciCloudSys.hciInit(strConfig, this);
        if (errCode != HciErrorCode.HCI_ERR_NONE && errCode != HciErrorCode.HCI_ERR_SYS_ALREADY_INIT) {
            Toast.makeText(getApplicationContext(), "hciInit error: " + HciCloudSys.hciGetErrorInfo(errCode),Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取授权/更新授权文件 :
        errCode = checkAuthAndUpdateAuth();
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            // 由于系统已经初始化成功,在结束前需要调用方法hciRelease()进行系统的反初始化
            Toast.makeText(getApplicationContext(), "CheckAuthAndUpdateAuth error: " + HciCloudSys.hciGetErrorInfo(errCode),Toast.LENGTH_SHORT).show();
            HciCloudSys.hciRelease();
            return;
        }



        // 读取用户的调用的能力
        String capKey = mAccountInfo.getCapKey();

        if (!capKey.equals("asr.cloud.grammar"))
        {
            mBtnRecogRealTimeMode.setEnabled(true);
        }

        // 初始化录音机
        mAsrRecorder = new ASRRecorder();

        // 配置初始化参数
        AsrInitParam asrInitParam = new AsrInitParam();
        String dataPath = getFilesDir().getPath().replace("files", "lib");
        Log.i(TAG,"dataPath" + dataPath);
        asrInitParam.addParam(AsrInitParam.PARAM_KEY_INIT_CAP_KEYS, capKey);
        asrInitParam.addParam(AsrInitParam.PARAM_KEY_DATA_PATH, dataPath);
        asrInitParam.addParam(AsrInitParam.PARAM_KEY_FILE_FLAG, AsrInitParam.VALUE_OF_PARAM_FILE_FLAG_ANDROID_SO);
        Log.v(TAG, "init parameters:" + asrInitParam.getStringConfig());

        // 设置初始化参数
        mAsrRecorder.init(asrInitParam.getStringConfig(),
                new ASRResultProcess());

        // 配置识别参数
        final AsrConfig asrConfig = new AsrConfig();
        // PARAM_KEY_CAP_KEY 设置使用的能力
        asrConfig.addParam(AsrConfig.SessionConfig.PARAM_KEY_CAP_KEY, capKey);
        // PARAM_KEY_AUDIO_FORMAT 音频格式根据不同的能力使用不用的音频格式
        asrConfig.addParam(AsrConfig.AudioConfig.PARAM_KEY_AUDIO_FORMAT,
                AsrConfig.AudioConfig.VALUE_OF_PARAM_AUDIO_FORMAT_PCM_16K16BIT);
        // PARAM_KEY_ENCODE 音频编码压缩格式，使用OPUS可以有效减小数据流量
        asrConfig.addParam(AsrConfig.AudioConfig.PARAM_KEY_ENCODE, AsrConfig.AudioConfig.VALUE_OF_PARAM_ENCODE_SPEEX);
        // 其他配置，此处可以全部选取缺省值

        // 语法相关的配置,若使用自由说能力可以不必配置该项
        if (capKey.contains("local.grammar")) {
            grammar = loadGrammar("stock_10001.gram");
            // 加载本地语法获取语法ID
            AsrGrammarId id = new AsrGrammarId();
            ASRCommonRecorder.loadGrammar("capkey=" + capKey +",grammarType=jsgf", grammar, id);
            Log.d(TAG, "grammarid="+id);
            // PARAM_KEY_GRAMMAR_TYPE 语法类型，使用自由说能力时，忽略以下此参数
            asrConfig.addParam(AsrConfig.GrammarConfig.PARAM_KEY_GRAMMAR_TYPE,
                    AsrConfig.GrammarConfig.VALUE_OF_PARAM_GRAMMAR_TYPE_ID);
            asrConfig.addParam(AsrConfig.GrammarConfig.PARAM_KEY_GRAMMAR_ID,
                    "" + id.getGrammarId());

            List<String> grammarList = loadGrammarList(grammar);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, grammarList);
            mGrammarLv.setAdapter(adapter);
        }
        else if(capKey.contains("cloud.grammar")) {
            grammar = loadGrammar("stock_10001.gram");
            // PARAM_KEY_GRAMMAR_TYPE 语法类型，使用自由说能力时，忽略以下此参数
            asrConfig.addParam(AsrConfig.GrammarConfig.PARAM_KEY_GRAMMAR_TYPE,
                    AsrConfig.GrammarConfig.VALUE_OF_PARAM_GRAMMAR_TYPE_JSGF);

            List<String> grammarList = loadGrammarList(grammar);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, grammarList);
            mGrammarLv.setAdapter(adapter);
        }

        Log.v(TAG, "asr config:" + asrConfig.getStringConfig());

        mBtnRecogRealTimeMode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mAsrRecorder.getRecorderState() == ASRRecorder.RECORDER_STATE_IDLE) {
                    asrConfig.addParam(AsrConfig.SessionConfig.PARAM_KEY_REALTIME, "yes");
                    mAsrRecorder.start(asrConfig.getStringConfig(), grammar);

                } else {
                    Log.e("recorder", "录音机未处于空闲状态，请稍等");
                }
            }
        });
    }
    // /////////////////////////////////////////////////////////////////////////////////////////

    private String loadGrammar(String fileName) {
        String grammar = "";
        try {
            InputStream is = null;
            try {
                is = getAssets().open(fileName);
                byte[] data = new byte[is.available()];
                is.read(data);
                grammar = new String(data);
            } finally {
                is.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return grammar;
    }

    private List<String> loadGrammarList(String WordlistGrammar) {

        List<String> strList = new ArrayList<String>();

        for (String msg : WordlistGrammar.split("\n")) {
            strList.add(msg.trim());
        }

        return strList;
    }

    private class ASRResultProcess implements ASRRecorderListener {
        @Override
        public void onRecorderEventError(RecorderEvent arg0, int arg1) {
            String sError = "错误码为：" + arg1;
            Message m = mUIHandle.obtainMessage(1, 3, 1, sError);
            mUIHandle.sendMessage(m);
        }

        @Override
        public void onRecorderEventRecogFinsh(RecorderEvent recorderEvent,               //识别的API
                                              AsrRecogResult arg1) {
            if (recorderEvent == RecorderEvent.RECORDER_EVENT_RECOGNIZE_COMPLETE) {
                String sState = "状态为：识别结束";
                Message m = mUIHandle.obtainMessage(1, 1, 1, sState);
                mUIHandle.sendMessage(m);
            }
            if (arg1 != null) {
                String sResult;
                if (arg1.getRecogItemList().size() > 0) {
                    sResult = "识别结果为："
                            + arg1.getRecogItemList().get(0).getRecogResult();         //识别的文本信息
                    query = arg1.getRecogItemList().get(0).getRecogResult();
//////////////////////////////////////////////
                    new Thread(){
                        @Override
                        public void run() {
                            Jason jts = new Jason();
                            Log.i(TAG,"进入新线程");
                            try {
                                result = jts.ask(query);                               //把网络访问的代码放在这里

                                if (result != null) {
                                    Log.i(TAG, "进入解析");
                                    Gson gson = new Gson();
                                    java.lang.reflect.Type type = new TypeToken<JsonBean>() {
                                    }.getType();
                                    JsonBean jsonBean = gson.fromJson(result, type);
                                    System.out.println(jsonBean.getResult());
                                    resultShow = jsonBean.getSingleNode().getAnswerMsg();
                                    if (jsonBean.getVagueNode() != null) {
                                        for (int i = 0;i < jsonBean.getVagueNode().getItemList().size(); i++){
                                            resultShow += jsonBean.getVagueNode().getItemList().get(i).getNum() + jsonBean.getVagueNode().getItemList().get(i).getQuestion();
                                        }

                                    }else{
                                        resultShow = jsonBean.getSingleNode().getAnswerMsg();
                                    }
                                    System.out.println(resultShow);
                                    if(resultShow != null) {
                                        Intent intent = new Intent(QuestActivity.this, ShowQueryActivity.class);
                                        intent.putExtra("extra_showResult",resultShow);
                                        startActivity(intent);
                                    }
                                }

                            } catch (HttpException e) {
                                System.out.println("heheda" + e);
                            }
                        }
                    }.start();

                } else {
                    sResult = "未能正确识别,请重新输入";
                }
                Message m = mUIHandle.obtainMessage(1, 2, 1, sResult);
                mUIHandle.sendMessage(m);
            }
        }

        @Override
        public void onRecorderEventStateChange(RecorderEvent recorderEvent) {
            String sState = "状态为：初始状态";
            if (recorderEvent == RecorderEvent.RECORDER_EVENT_BEGIN_RECORD) {
                sState = "状态为：开始录音";
            } else if (recorderEvent == RecorderEvent.RECORDER_EVENT_BEGIN_RECOGNIZE) {
                sState = "状态为：开始识别";
            } else if (recorderEvent == RecorderEvent.RECORDER_EVENT_NO_VOICE_INPUT) {
                sState = "状态为：无音频输入";
            }
            Message m = mUIHandle.obtainMessage(1, 1, 1, sState);
            mUIHandle.sendMessage(m);
        }

        @Override
        public void onRecorderRecording(byte[] volumedata, int volume) {
        }

        @Override
        public void onRecorderEventRecogProcess(RecorderEvent recorderEvent,
                                                AsrRecogResult arg1) {
            // TODO Auto-generated method stub
            if (recorderEvent == RecorderEvent.RECORDER_EVENT_RECOGNIZE_PROCESS) {
                String sState = "状态为：识别中间反馈";
                Message m = mUIHandle.obtainMessage(1, 1, 1, sState);
                mUIHandle.sendMessage(m);
            }
            if (arg1 != null) {
                String sResult;
                if (arg1.getRecogItemList().size() > 0) {
                    sResult = "识别中间结果结果为："
                            + arg1.getRecogItemList().get(0).getRecogResult();
                } else {
                    sResult = "未能正确识别,请重新输入";
                }
                Message m = mUIHandle.obtainMessage(1, 2, 1, sResult);
                mUIHandle.sendMessage(m);
            }
        }
    }

//	@Override
//	public void onStart() {
//		super.onStart();
//	}
//
//	@Override
//	public void onStop() {
//		super.onStop();
//	}
//

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mAsrRecorder.release();
        HciCloudSys.hciRelease();
        Log.i(TAG, "onDestroy()");
    }


    /**
     * 获取授权
     *
     * @return true 成功
     */
    private int checkAuthAndUpdateAuth() {

        // 获取系统授权到期时间
        int initResult;
        AuthExpireTime objExpireTime = new AuthExpireTime();
        initResult = HciCloudSys.hciGetAuthExpireTime(objExpireTime);
        if (initResult == HciErrorCode.HCI_ERR_NONE) {
            // 显示授权日期,如用户不需要关注该值,此处代码可忽略
            Date date = new Date(objExpireTime.getExpireTime() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
                    Locale.CHINA);
            Log.i(TAG, "expire time: " + sdf.format(date));

            if (objExpireTime.getExpireTime() * 1000 > System
                    .currentTimeMillis()) {
                // 已经成功获取了授权,并且距离授权到期有充足的时间(>7天)
                Log.i(TAG, "checkAuth success");
                return initResult;
            }

        }

        // 获取过期时间失败或者已经过期
        initResult = HciCloudSys.hciCheckAuth();
        if (initResult == HciErrorCode.HCI_ERR_NONE) {
            Log.i(TAG, "checkAuth success");
            return initResult;
        } else {
            Log.e(TAG, "checkAuth failed: " + initResult);
            return initResult;
        }
    }

    /**
     * 加载初始化信息
     *
     * @param context
     *            上下文语境
     * @return 系统初始化参数
     */
    private InitParam getInitParam() {
        String authDirPath = this.getFilesDir().getAbsolutePath();

        // 前置条件：无
        InitParam initparam = new InitParam();

        // 授权文件所在路径，此项必填
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_AUTH_PATH, authDirPath);

        // 是否自动访问云授权,详见 获取授权/更新授权文件处注释
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_AUTO_CLOUD_AUTH, "no");

        // 灵云云服务的接口地址，此项必填
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_CLOUD_URL, AccountInfo
                .getInstance().getCloudUrl());

        // 开发者Key，此项必填，由捷通华声提供
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_DEVELOPER_KEY, AccountInfo
                .getInstance().getDeveloperKey());

        // 应用Key，此项必填，由捷通华声提供
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_APP_KEY, AccountInfo
                .getInstance().getAppKey());

        // 配置日志参数
        String sdcardState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
            String sdPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            String packageName = this.getPackageName();

            String logPath = sdPath + File.separator + "sinovoice"
                    + File.separator + packageName + File.separator + "log"
                    + File.separator;

            // 日志文件地址
            File fileDir = new File(logPath);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            // 日志的路径，可选，如果不传或者为空则不生成日志
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_PATH, logPath);

            // 日志数目，默认保留多少个日志文件，超过则覆盖最旧的日志
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_COUNT, "5");

            // 日志大小，默认一个日志文件写多大，单位为K
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_SIZE, "1024");

            // 日志等级，0=无，1=错误，2=警告，3=信息，4=细节，5=调试，SDK将输出小于等于logLevel的日志信息
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_LEVEL, "5");
        }

        return initparam;
    }
}
