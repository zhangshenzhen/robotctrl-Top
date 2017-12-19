package com.brick.robotctrl;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ant.liao.GifView;
import com.jly.batteryView.BatteryView;
import com.kjn.videoview.ADVideo;
import com.presentation.MainPresentation;
import com.rg2.activity.ShellUtils;
import com.rg2.utils.LogUtil;
import com.rg2.utils.StringUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.sauronsoftware.base64.Base64;
import zime.ui.ZIMEAVDemoService;
///*https://zhangshenzhen@bitbucket.org/pumpkine/robotctrl.git*/
public class MainActivity extends com.brick.robotctrl.BaseActivity{
    private static final String TAG = "MainActivity";
    SharedPreferences.OnSharedPreferenceChangeListener presChangeListener = null;

    SSDBTask   ssdbTask          = null;
    SerialCtrl serialCtrl        = null;
    SerialCtrl serialCtrlPrinter = null;
    Button IDButton;

    DispQueueThread DispQueue = null;//刷新电压显示线程
    public BatteryView mBatteryView = null;

    private boolean serverChanged        = false;
    private boolean serialChanged        = false;
    private boolean robotLocationChanged = false;

    private String mp3Url = Environment.getExternalStorageDirectory().getPath() + "/Movies/qianqian.mp3";

    Calendar currentTime = null;

    ADVideo adVideo1 = null;
    private final int videoInfo = 9999;
    private final int ssdbConn  = 888;

    private IntentFilter          intentFilter;
    private netWorkChangeReceiver netWorkChangeReceiver;
    private Button                printButton, mSettingBtn;

    private Dialog mNoticeDialog;
    private Timer timer = new Timer(true);
    TimerTask mSettingTask = new TimerTask(){
        @Override
        public void run()
        {
            String fingerprint = "cat /sys/class/gpio/gpio34/value";
            List<String> commands = new ArrayList<String>();
            commands.add(fingerprint);
            ShellUtils.CommandResult result = ShellUtils.execCommand(commands, false, true);
            LogUtil.e("TAG", "result-->" + result.result);
        }
    };

    private ImageView mivglobal;

private MainPresentation  mMainPresentation;
    private TextView mtvBback;
    private Button btnMoney;
    private Button btntest;
    private ProgressDialog pd;
    private GifView gif;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        // updatePresentation();//在BaseActivity中调用
       // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // remove text in toolbar
       // toolbar.setTitle("");
       // setSupportActionBar(toolbar);

        gif = (GifView) findViewById(R.id.gif);

        gif.setGifImage(R.drawable.weixiao);

        ssdbTask = new SSDBTask(MainActivity.this, handler);  //ttymxc0
       // serialCtrl = new SerialCtrl(MainActivity.this, handler, "ttymxc0", 9600, "robotctrl");
        serialCtrl = new SerialCtrl(MainActivity.this, handler, "ttyS3", 9600, "robotctrl");

         //打印机              ttyUSB1
        serialCtrlPrinter = new SerialCtrl(MainActivity.this, handler, "ttyS1", 9600, "printer");
        // serialCtrlPrinter.setSerialCOM("/dev/ttyUSB0");
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        //创建NetWorkChangeReceiver的实例，并调用registerReceiver()方法进行注册
        netWorkChangeReceiver = new netWorkChangeReceiver();
        registerReceiver(netWorkChangeReceiver, intentFilter);
        DispQueue = new DispQueueThread();      //获取电压显示线程
       // DispQueue.start();  //暂时关闭此线程,

          initData();
          initChangeListener();
//        PlayerService.startAction(this, mp3Url);
//        relative timer

         Timer timer = new Timer(true);
         //改指令执行后延时1000ms后执行run，之后每1000ms执行�?次run
         timer.schedule(queryTask, 200, 200);
        //   timer.cancel(); //结束Timer所有的计时器;
        initHandler();
        AboutActivity about = new AboutActivity();
        AboutActivity.MyThread tt = about.new MyThread(ssdbTask.robotName);
        tt.start();
        //被移动到SplashActivity界面中进行开启服务;

//         Intent startIntent = new Intent(MainActivity.this, ZIMEAVDemoService.class);
//         startService(startIntent); // 启动服务

                Log.d(TAG, "ZIMEService");

//        //ExpressionActivity.startAction(MainActivity.this, 12);
    }
    /*监听
    * */
     private void initChangeListener() {
        //NOTE OnSharedPreferenceChangeListener: listen settings changed
        presChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            private final String robotName = getString(R.string.robotName);
            private final String robotLocation = getString(R.string.robotLocation);
            private final String serverIp = getString(R.string.serverIp);
            private final String serverPort = getString(R.string.serverPort);
            private final String controlType = getString(R.string.controlType);

            private final String serialBaud = getString(R.string.serialBaud);
            private final String serialCom = getString(R.string.serialCOM);

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
            {
                if (key.equals(controlType)){
                    boolean val = sharedPreferences.getBoolean(key, false);
                    Log.i(TAG, "onSharedPreferenceChanged: " + key + " " + val);
                } else{
                    String val = null;
                    try {
                        val = sharedPreferences.getString(key, "");
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    if (key.equals(robotName) && val != null) {
                        ssdbTask.setRobotName(val);     // deal it if val = null设置表名
                    } else if (key.equals(robotLocation) && val != null) {
                        robotLocationChanged = true;
                        ssdbTask.setRobotLocation(val);
                    } else if (key.equals(serverIp) && val != null){
                        ssdbTask.setServerIP(val);
                        serverChanged = true;
                    } else if (key.equals(serverPort)) {
                        int serverPort = Integer.parseInt(val);
                        ssdbTask.setServerPort(serverPort);
                        serverChanged = true;
                    }
                    else if (key.equals(serialCom) && val != null) {
                        // do some thing
                        serialCtrl.setSerialCOM(val);
                        serialChanged = true;
                    }  else if (key.equals(serialBaud) && val != null){
                        serialCtrl.setSerialBaud(val);
                        // do some thing
                        serialChanged = true;
                    }
                    //                    Log.i(TAG, "onSharedPreferenceChanged: " + key + " " + val);
                }
            }
        };
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(presChangeListener);
     }

    //在oncreate方法中;
    private void initHandler() {
         ADActivity.setHandler(handler);
        AboutActivity.setHandler(handler);
    }

    @Override
    protected void updatePresentation() {
            //得到当前route and its presentation display
            MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(
                    MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
            Display presentationDisplay =  route  !=  null ? route.getPresentationDisplay() : null;
            if (mMainPresentation != null && mMainPresentation.getDisplay() !=  presentationDisplay) {
                mMainPresentation.dismiss();
                mMainPresentation = null;
            }
            if (mMainPresentation == null &&  presentationDisplay != null) {
                // Initialise a new Presentation for the Display
                Log.d(TAG, "MainPresentation............main ..2");
                mMainPresentation = new MainPresentation(this,  presentationDisplay);
                //把当前的对象引用赋值给BaseActivity中的引用;
                mPresentation  =  mMainPresentation  ;
                // Log.d(TAG, "updatePresentation: this: "+ this.toString());
                mMainPresentation.setOnDismissListener(mOnDismissListener);

                // Try to show the presentation, this might fail if the display has
                // gone away in the mean time
                try {
                    mMainPresentation.show();
                } catch (WindowManager.InvalidDisplayException ex) {
                    // Couldn't show presentation - display was already removed
                    // Log.d(TAG, "updatePresentation: failed");
                    mMainPresentation = null;
                }
            }
    }





    //初始化控件;
    public void initData() {

       /* mSettingBtn = (Button) findViewById(R.id.btn_setting);
        printButton = (Button) findViewById(R.id.Printer);
        IDButton = (Button) findViewById(R.id.IDButtonTest);
        btnMoney = (Button) findViewById(R.id.btn_money);
         btntest  =    (Button) findViewById(R.id.btn_test);
        mtvBback = (TextView) findViewById(R.id.tv_back);
          mSettingBtn.setOnClickListener(this);
//        mbtnmenue.setOnClickListener(this);
//        mquestion.setOnClickListener(this);
         printButton.setOnClickListener(this);
         btnMoney.setOnClickListener(this);
         btntest.setOnClickListener(this);
         IDButton.setOnClickListener(this);
          mtvBback.setOnClickListener(this);*/
          gif.setOnClickListener(this);

    }

    //点击事件
    @Override
    public void onClick(View view) {
        LogUtil.e("MainActivity", "..System.currentTimeMillis()"+System.currentTimeMillis());
       switch (view.getId()){
           case R.id.gif:
               Log.i(TAG, "onClick: 点击了界面");
               //暂时不用，先屏蔽掉
               startActivity(new Intent(MainActivity.this,FunctionSelectActivity.class));
               break;


            /*
            case R.id.btn_setting:
                startActivity(new Intent(MainActivity.this, FingerInputActivity.class));
                break;
            case R.id.IDButtonTest:
                startActivity(new Intent(MainActivity.this, CardActivity.class));
                LogUtil.e("MainActivity", ".........................23");
                break;
            case R.id.Printer:
                startActivity(new Intent(MainActivity.this, PrintActivity.class));
                break;
            case R.id.btn_money:
                startActivity(new Intent(MainActivity.this, FinancialMangerActivity.class));
                break;
            case R.id.tv_back:
                finish();  //结束掉当前的 Activity, 返回到上一层;
                break;
            case R.id.btn_test:
                startActivity(new Intent(MainActivity.this, MenuActivity.class));
                break;*/
        }
    }

    private void initMCU(){
    }
    private void threadToUiToast(final String message, final int toastLength){
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(getApplicationContext(), message, toastLength).show();
            }
        });
    }
    private int    countForPlayer        = 0;//播放计数�?
    private int    countForReconnectSSDB = 0;
    private int    countForAlive         = 0;//复活计数�?
    private String strTimeFormat         = null;
    private String disableAudio          = "No";

    TimerTask queryTask = new TimerTask()
    {
        @Override
        public void run()
        {
//            Log.d(TAG, "run: stop: " + ssdbTask.stop);
            if (!ssdbTask.stop)
            {                  // 发起读请�?
                ssdbTask.SSDBQuery(SSDBTask.ACTION_HGET);////////////////////////////!!!!!!!!!!!!!!!!!!!!!!!!!!
            } else {
                countForReconnectSSDB++;
                if (countForReconnectSSDB % (1000 / 200) == 0)
                {
                    Log.d(TAG, "run: " + countForReconnectSSDB);
                    threadToUiToast("ssdb reconnect after " + (5 - countForReconnectSSDB / (1000 / 200)) + "s", Toast.LENGTH_SHORT);
                    if (countForReconnectSSDB == 5 * 1000 / 200)
                    {
                        threadToUiToast("ssdb reconnecting...", Toast.LENGTH_SHORT);
                        ssdbTask.connect();
                        countForReconnectSSDB = 0;
                    }
                }
            }

            if (countForAlive++ > 5 * 1000 / 200)
            {//显示时间
                currentTime = Calendar.getInstance();
                strTimeFormat = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.TIME_12_24);
                if ((strTimeFormat == null) || (strTimeFormat.equals("")) || strTimeFormat.equals("12"))
                {     // 12HOUR
                    if (Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM) {      // AM
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_CurrentTime], String.valueOf(currentTime.get(Calendar.HOUR)) +
                                ":" + String.valueOf(currentTime.get(Calendar.MINUTE)) + ":" + String.valueOf(currentTime.get(Calendar.SECOND)));
                    } else {    // PM
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_CurrentTime], String.valueOf(currentTime.get(Calendar.HOUR) + 12) +
                                ":" + String.valueOf(currentTime.get(Calendar.MINUTE)) + ":" + String.valueOf(currentTime.get(Calendar.SECOND)));
                    }
                } else {        // 24HOUR
                    ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_CurrentTime], String.valueOf(currentTime.get(Calendar.HOUR)) +
                            ":" + String.valueOf(currentTime.get(Calendar.MINUTE)) + ":" + String.valueOf(currentTime.get(Calendar.SECOND)));
                }
                countForAlive = 0;
            }

            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);//获得运行activity
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;//得到某一活动
            //            Log.d(TAG, "pkg:"+cn.getPackageName());
            //            Log.d(TAG, "cls:"+cn.getClassName());

            //            if ( cn.getClassName().equals("com.brick.robotctrl.MainActivity") ) {
            //                countForPlayer++;
            ////                Log.d(TAG, "run: countForPlayer:" + countForPlayer);
            //                if ( countForPlayer == 30*1000/200 ) {
            //                    PlayerService.startAction(MainActivity.this, mp3Url);
            //                    countForPlayer = 0;
            //                }
            //            }
            if (cn.getClassName().equals("com.brick.robotctrl.ADActivity")) {
                if (disableAudio.equals("No")){
                    disableAudio = "Yes";
                    ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_DisableAudio], disableAudio);
                }
             } else {
                if (disableAudio.equals("Yes"))
                {
                    disableAudio = "No";
                    ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_DisableAudio], disableAudio);
                }
            }
            addTimerCount();
        }
    };

    public static String Base64Decode(String base64EncodedData) {
        String base64EncodedBytes = Base64.decode(base64EncodedData);
        Log.d(TAG, "Base64Decode: base64EncodedBytes " + base64EncodedBytes);
        return base64EncodedBytes;
    }

    // receive ssdb server info
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what){
                case videoInfo:
                    ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_VideoInfo], (String) msg.obj);
                    Log.d(TAG, "handleMessage: ----------8------- ");
                    break;
                case ssdbConn:
                    Log.d(TAG, "handleMessage: ----------9------- ");
                    ssdbTask.connect();
                    break;
                case SSDBTask.Key_Event:
                    /**
                     * 处理event方法
                     * 1. 设置event事件使能，以获取事件内容�?
                     * 2. 清除服务器中该event事件�?
                     */
                    String rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ----------10--------Key:Location \tvalue:" + rlt);
                    if (rlt.equals("DirCtl"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-1-------Key:Event \tvalue:" + rlt);
                        SSDBTask.enableDirCtl = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);//获得运行activity
                        ComponentName an = am.getRunningTasks(1).get(0).topActivity;//得到某一活动
                        Log.d(TAG, "handleMessage: clear Event"+an.getClassName());
                        if (!an.getClassName().equals("com.brick.robotctrl.ExpressionActivity"))
                        {
                          ExpressionActivity.startAction(MainActivity.this, 9 );
                        }
                    }
                    if (rlt.equals("EndDirCtl")) {
                        Log.d(TAG, "handleMessage: ----------10-2------- Key:Event \tvalue:" + rlt);
                        SSDBTask.enableDirCtl = false;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("Charge")) {
                        Log.d(TAG, "handleMessage: ----------10-3------- Key:Event \tvalue:" + rlt);
                        //SSDBTask.enableCharge = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        //充电
                        serialCtrl.robotCharge();
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("setparam"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-4------- Key:Event \tvalue:" + rlt);
                        SSDBTask.enableSetParameter = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("Brow"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-5-------Key:Event \tvalue:" + rlt);
                        SSDBTask.enableChangeBrow = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("SetVolume"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-6------- Key:Event \tvalue:" + rlt);
                        SSDBTask.enableSetVolume = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                    }
                    if (rlt.equals("EndVideo"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-7------- Key:Event \tvalue:" + rlt);
                        SSDBTask.enableVideoPlay = false;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    // by gaowei start
                    if (rlt.equals("VideoPlay"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-8------- Key:Event \tvalue:" + rlt);
                        SSDBTask.enableVideoPlay = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("VideoPlayList"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-9------- Key:Event \tvalue:" + rlt);
                        SSDBTask.enableVideoPlayList = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("RobotMsg"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-10------- Key:Event \tvalue:" + rlt);
                        SSDBTask.enableRobotMsg = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("BatteryVolt"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-11------- Key:Event \tvalue:" + rlt);
                        SSDBTask.enableBatteryVolt = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("NetworkDelay"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-12------- Key:Event \tvalue:" + rlt);
                        SSDBTask.enableNetworkDelay = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("Location"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-13------- Key:Event \tvalue:" + rlt);
                        SSDBTask.enableLocation = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("CurrentTime"))
                    {
                        Log.d(TAG, "handleMessage:  ----------10-14-------Key:Event \tvalue:" + rlt);
                        SSDBTask.enableCurrentTime = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("DisableAudio"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-15------- Key:Event \tvalue:" + rlt);
                        SSDBTask.enableForbidAudio = true;//使能静音
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    if (rlt.equals("reboot"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-16------- Key:Event \tvalue:" + rlt);
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                        Log.d(TAG, "restart: " + "重启机器人" );
                        Intent iReboot = new Intent(Intent.ACTION_REBOOT);
                        iReboot.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MainActivity.this.startActivity(iReboot);
                    }
                    if (rlt.equals("shutdown"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-17------- Key:Event \tvalue:" + rlt);
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                        MainActivity.super.onShutdown();
                    }
                    if (rlt.equals("message"))
                    {
                        Log.d(TAG, "handleMessage: ----------10-18------- Key:Event \tvalue:" + rlt);
                        ssdbTask.enableGetMessage = true;
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Event], "");
                        Log.d(TAG, "handleMessage: clear Event");
                    }
                    break;
                /**
                 * 处理具体的event事件
                 * 1. 根据event事件的持续�?�修改event事件的使能开关；
                 * 2. 对获取的数据做一定的断言处理�?
                 * 3. 根据事件类型及事件内容改变robot行为�?
                 */
                case SSDBTask.Key_Location:
                    rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ----------11--------Key:Location \tvalue:" + rlt);
                    if (!rlt.equals(""))
                    {
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Location], ssdbTask.robotLocation);
                        SSDBTask.enableLocation = false;
                    }
                    break;
                case SSDBTask.Key_VideoPlay:
                    rlt = (String) msg.obj;
                    if (!rlt.equals(""))
                    {
                        Log.d(TAG, "handleMessage: ---------12---------Key:VideoPlay \tvalue:" + rlt);
                        String[] strArray = rlt.split(" ");
                        ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_VideoPlay], "");
                        switch (strArray[0])
                        {
                            case "Play":
                                startActivity(new Intent().setClass(MainActivity.this, ADActivity.class));
                                //singleTask 此Activity实例之上的其他Activity实例统统出栈，使此Activity实例成为栈顶对象，显示到幕前�?   break;
                                break;
                            case "ContinuePlay":
                                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);//获得运行activity
                                ComponentName an = am.getRunningTasks(1).get(0).topActivity;//得到某一活动
                                if (!an.getClassName().equals("com.brick.robotctrl.ADActivity"))
                                {
                                    ADActivity.startAction(MainActivity.this, strArray[0], null);
                                } else{
                                    Log.e("test111111111111", "");
                                    ADVideo.resume();
                                }
                                //                                else{
                                //                                    ADVideo2.start();
                                //                                }
                                break;
                            case "Pause":
                                ActivityManager em = (ActivityManager) getSystemService(ACTIVITY_SERVICE);//获得运行activity
                                ComponentName en = em.getRunningTasks(1).get(0).topActivity;//得到某一活动
                                if (en.getClassName().equals("com.brick.robotctrl.ADActivity"))
                                {
                                    ADVideo.pause();
                                }

                                break;
                            case "Stop":
                                ActivityManager bm = (ActivityManager) getSystemService(ACTIVITY_SERVICE);//获得运行activity
                                ComponentName bn = bm.getRunningTasks(1).get(0).topActivity;//得到某一活动
                                Log.d(TAG, "handleMessage: ----------12-2--------Key:Stop \tvalue:" + bn.getClassName());

                                if (bn.getClassName().equals("com.brick.robotctrl.ADActivity"))
                                {
                                    ADVideo.stopPlayBack();
                                    ExpressionActivity.startAction(MainActivity.this, 1);
                                }
                                else if (bn.getClassName().equals("com.brick.robotctrl.ImageActivity"))
                                {
                                    ExpressionActivity.startAction(MainActivity.this, 1);
                                }
                                break;
                            case "Single":
                                ActivityManager dm = (ActivityManager) getSystemService(ACTIVITY_SERVICE);//获得运行activity
                                ComponentName dn = dm.getRunningTasks(1).get(0).topActivity;//得到某一活动
                                if (!dn.getClassName().equals("com.brick.robotctrl.ADActivity") ||
                                        !dn.getClassName().equals("com.brick.robotctrl.ImageActivity"))
                                {
                                    if (strArray[1].endsWith(".jpg"))
                                    {
                                        ImageActivity.startAction(MainActivity.this, strArray[0], strArray[1]);
                                    }
                                    else
                                    {
                                        ADActivity.startAction(MainActivity.this, strArray[0], strArray[1]);
                                    }
                                }
                            case "Cycle":
                                ActivityManager cm = (ActivityManager) getSystemService(ACTIVITY_SERVICE);//获得运行activity
                                ComponentName cn = cm.getRunningTasks(1).get(0).topActivity;//得到某一活动
                                if (!cn.getClassName().equals("com.brick.robotctrl.ADActivity") ||
                                        !cn.getClassName().equals("com.brick.robotctrl.ImageActivity"))
                                {
                                    if (strArray[1].endsWith(".jpg"))
                                    {
                                        ImageActivity.startAction(MainActivity.this, strArray[0], strArray[1]);
                                    }
                                    else
                                    {
                                        ADActivity.startAction(MainActivity.this, strArray[0], strArray[1]);
                                    }
                                }
                                break;
                            case "SingleCycle":
                                ActivityManager fm = (ActivityManager) getSystemService(ACTIVITY_SERVICE);//获得运行activity
                                ComponentName fn = fm.getRunningTasks(1).get(0).topActivity;//得到某一活动
                                if (!fn.getClassName().equals("com.brick.robotctrl.ADActivity"))
                                {
                                    ADActivity.startAction(MainActivity.this, strArray[0], strArray[1]);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                case SSDBTask.Key_VideoPlayList:
                    rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ----------13--------Key:SetParam \tvalue:" + rlt);
                    ssdbTask.pushFileList();
                       if(!rlt.equals(""))   {
                       Log.d(TAG, "videoplaylist: hehe");
                       SSDBTask.enableVideoPlayList=false;
                        }
                    break;
                case SSDBTask.Key_RobotMsg:
                    rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ---------14---------Key:SetParam \tvalue:" + rlt);
                    if (!rlt.equals(""))
                    {
                        //!!!!!!!!!!!执行videorobotmsg操作
                        SSDBTask.enableRobotMsg = false;
                    }
                    break;
                case SSDBTask.Key_BatteryVolt:
                    rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ---------15---------Key:SetParam \tvalue:" + rlt);
                    if (!rlt.equals(""))
                    {
                        //!!!!!!!!!!!执行BatteryVolt操作
                        SSDBTask.enableBatteryVolt = false;
                    }
                    break;
                case SSDBTask.Key_NetworkDelay:
                    rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ---------16---------Key:SetParam \tvalue:" + rlt);
                    if (!rlt.equals("")) {
                        //!!!!!!!!!!!执行NetworkDelay操作
                        SSDBTask.enableNetworkDelay = false;
                    }
                    break;
                case SSDBTask.Key_CurrentTime:
                    rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ---------17---------Key:SetParam \tvalue:" + rlt);
                    if (!rlt.equals("")){
                        //!!!!!!!!!!!执行CurrentTime操作
                        SSDBTask.enableCurrentTime = false;
                    }
                    break;
                case SSDBTask.Key_DisableAudio:
                    rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ---------18---------Key:SetParam \tvalue:" + rlt);
                    if (!rlt.equals("")){
                        //!!!!!!!!!!!执行ForbidAudio操作
                        SSDBTask.enableForbidAudio = false;
                    }
                    break;
                // by gaowie end
                case SSDBTask.Key_DirCtrl:
                    rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ---------19---------Key:DirCtrl \tvalue:" + rlt);
                    if (rlt.equals("EndDirCtl")) {
                        SSDBTask.enableDirCtl = false;
                        Log.d(TAG, "handleMessage: ---------19-2--------Key:DirCtrl \tvalue:" + rlt);
                    }
                    else if (!rlt.equals("")){
                        Log.d(TAG, "handleMessage: ---------19-3--------Key:DirCtrl \tvalue:" + rlt);

                        serialCtrl.robotMove(rlt);
                         //修改的代码
                      /*  if(rlt.equals("stop")||rlt.equals("headmid")) {
                            SSDBTask.enableDirCtl = false;
                        }*/
                    }
                    break;
                case SSDBTask.Key_SetParam:
                    rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ---------20---------Key:SetParam \tvalue:" + rlt);
                    if (!rlt.equals("")) {
                        serialCtrl.setRobotRate(rlt);
                        SSDBTask.enableSetParameter = false;
                    }
                    break;
                case SSDBTask.Key_ChangeBrow:
                    rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ---------21---------Key:ChangeBrow \tvalue:" + rlt);
                    if (!rlt.equals("")){
                        SSDBTask.enableChangeBrow = false;
                        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                        Log.d(TAG, "handleMessage: ---------21-2--------Key:ChangeBrow \tvalue:"+rlt +" : "+ cn.getClassName());
                        if (cn.getClassName().equals("com.brick.robotctrl.ExpressionActivity")){
                            ExpressionActivity.changeExpression(Integer.parseInt(rlt));
                            Log.d(TAG, "handleMessage: changebrowed");
                        } else { //添加的代码,修改实现初次成功切换表情;
                           ExpressionActivity.startAction(MainActivity.this, Integer.parseInt(rlt));
                            Log.d(TAG, "handleMessage: change brow failure because of current activity is not ExpressionActivity");
                        }
                    }
                    break;
                case SSDBTask.Key_SetVolume:
                    rlt = (String) msg.obj;
                    Log.d(TAG, "handleMessage: ---------22---------Key:SetVolume \tvalue:" + rlt);
                    if (!rlt.equals(""))
                    {
                        int volume = Integer.parseInt(rlt);
                        if (volume > 100){
                            volume = 100;
                      }
                        else if (volume < 0){
                            volume = 0;
                        }
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume / 5, 0);
                        SSDBTask.enableSetVolume = false;
                    }
                    break;
                case SSDBTask.Key_Message:
                    rlt = (String) msg.obj;
                  /*  ActivityManager fm = (ActivityManager) getSystemService(ACTIVITY_SERVICE);//获得运行activity
                    ComponentName fn = fm.getRunningTasks(1).get(0).topActivity;//得到某一活动*/
                    Log.d(TAG, rlt+"handleMessage: --------23----------Key:Message \tvalue:" + rlt);
                    if (!rlt.equals(""))
                    {  //在这里测试其他的功能暂时注释掉，
                      //  SpeechService.startAction(MainActivity.this, Base64Decode(rlt));
                       switch (rlt){
                           case "a":
                               print3();
                               break;
                           case "A" :
                               print3();
                               break;
                           case "b":
                               print3();
                               break;
                           case "B" :
                               print3();
                               break;
                           case "c":
                               print3();
                               break;
                           case "C" :
                               print3();
                               break;

                       }



                        // 设置音量max大小为  15;
                        //获取最大音乐量值
                     /*   int voice = Integer.parseInt(rlt);
                           if(voice>=15){
                            voice =15;
                           }

                        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,voice,0);
                        int  current2 = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
                        Log.e(TAG, "当前媒体音量 ："+current2);*/
                        SSDBTask.enableGetMessage = false;
                    }
                    break;
                case SSDBTask.key_ApkUpdate:
                    //                    final String archiveFilePath = getInstallApkFullPath();
                    //                    if ( !(archiveFilePath == null) ) {                 // 判断字符串是否为空要�?==�? 不要用equals方法
                    //                        Log.d(TAG, "run: start to install apk: " + archiveFilePath);
                    //                        Intent intentA = new Intent(Intent.ACTION_VIEW);
                    //                        intentA.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //                        intentA.setDataAndType(Uri.fromFile(new File(archiveFilePath)), "application/vnd.android.package-archive");
                    //                        startActivity(intentA);
                    ////                        android.os.Process.killProcess(android.os.Process.myPid());
                    //                    } else {
                    //                        Log.d(TAG, "no apk need to install");
                    //                    }
                default:
                    break;
            }
        }
    };
    private String getInstallApkFullPath()
    {
        String apkDirPath = Environment.getExternalStorageDirectory().getPath() + "/Download";
        File apkFile = new File(apkDirPath);
        File[] apkFiles = apkFile.listFiles();//把该文件夹下的文件封装成文件的集合

        for (int i = 0; i < apkFiles.length; i++){
            if (needUpdate(apkFiles[i].getAbsolutePath())){
                Log.d(TAG, "run: start to install apk: " + apkFiles[i].getAbsolutePath());
                return apkFiles[i].getAbsolutePath();
            }
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, "onActivityResult: resultCode:" + resultCode);
        if (requestCode == 0)
       {
            //            if (resultCode == RESULT_OK) {        // left top back resultCode = 0
            if (serverChanged)
            {
                serverChanged = false;
                ssdbTask.connect();
            }
            if (serialChanged)
            {
                serialChanged = false;
                serialCtrl.openSerialCOM();
            }
            if (robotLocationChanged)
            {
                robotLocationChanged = false;
                ssdbTask.SSDBQuery(SSDBTask.ACTION_HSET, SSDBTask.event[SSDBTask.Key_Location], ssdbTask. robotLocation);
            }
        }
    }

   // DDMS
  /*  public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Debug.stopMethodTracing();
    }*/

    @Override //当重新获取焦点是 开启副屏的方法;
    protected void onResume() {
        super.onResume();
        setResult(Activity.RESULT_OK);//开启新的ActivityForResult();
        LogUtil.e(TAG, "..System.currentTimeMillis()"+System.currentTimeMillis());
       // updatePresentation();//在父类中已经被调用了，
        timer.cancel();//取消任务
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        timer.cancel(); //结束Timer所有的计时器;
        if ( mMainPresentation!= null) {
            mMainPresentation.dismiss();
            mMainPresentation = null;
        }
        Log.i(TAG, "onPuase在这里停止掉");
    }

    @Override
    protected  void onStart() {

        Log.i(TAG, "onStart");
        super.onStart();
    }

        @Override
        protected void onStop() {
           super.onStop();
            if ( mMainPresentation!= null) {
                mMainPresentation.dismiss();
                mMainPresentation = null;
            }
            Log.i(TAG, "onStop: MainActivity停止了么？");
       }

    @Override
    protected void onRestart()
    {
        Log.i(TAG, "onRestart");
        countForPlayer = 0;
        //        PlayerService.startAction(MainActivity.this, mp3Url);
        super.onRestart();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        Intent stopZIMEServiceIntent = new Intent(this,ZIMEAVDemoService.class);
        stopService(stopZIMEServiceIntent);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(presChangeListener);
        ssdbTask.disConnect();
        serialCtrl.closeSerialCOM();

        unregisterReceiver(netWorkChangeReceiver);
        Intent stopSpeechServiceIntent = new Intent(this, SpeechService.class);
        stopService(stopSpeechServiceIntent);
        //穿网自启动相关的
       // CommandExecution.execCommand("busybox killall edge",true);
    }

    //----------------------------------------------------电池电压刷新显示线程
    private int batteryVoltVal = 0;



    public class DispQueueThread extends Thread
    {
        @Override
        public void run()
        {
            super.run();
            while (!isInterrupted())
            {
                try
                {
                    while (true)
                    {//暂时屏蔽
                       batteryVoltVal = serialCtrl.getBattery();
                         Log.d("abc", "run: batteryVoltVal = " + batteryVoltVal);
                        if (batteryVoltVal != 0)
                        {
                            runOnUiThread(new Runnable()
                            {
                                public void run()
                                {
   //                          mBatteryView.setPower(batteryVoltVal);
                                }
                            });
                        }
                        Thread.sleep(500);//显示性能高的话，可以把此数�?�调小�??
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            }
            Log.d(TAG, "run: while over");
        }
    }
    /*
    * 网络连接的广播接收者*/
    class netWorkChangeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            //通过getSystemService()方法得到connectionManager这个系统服务类，专门用于管理网络连接
            ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
            if ((networkInfo != null && networkInfo.isAvailable()) && ssdbTask.stop)
            {
                Toast.makeText(context, "network is available, ssdb server haven't started, starting connect ssdb server", Toast.LENGTH_SHORT).show();
         //     handler.sendEmptyMessage(ssdbConn);
            }
            else
            {
                Toast.makeText(context, "network is unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }
                //判断是否需要更新的方法;
    private boolean needUpdate(String archiveFilePath)
    {                       //包管理器;
        PackageManager pm = getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
        if (info != null)
        {
            try
            {

                ApplicationInfo apkInfo = info.applicationInfo;
                //   String appName = pm.getApplicationLabel(apkInfo).toString();
                String packageName = apkInfo.packageName;   //得到安装包名�?
                String versionName = info.versionName;      //得到版本信息
                int versionCode = info.versionCode;        //得到版本信息

                //Drawable icon = pm.getApplicationIcon(appInfo);//得到图标信息
                //appName:RobotCtrl packagename: com.brick.robotctrl version: v1.27.31
                Log.d(TAG, "apkInfo:packagename: " + packageName + " versionName: " + versionName + " versionCode: " + versionCode);

          String appVersionName = getVersion(packageName);

                if (versionName.equals(null))
                {
                    threadToUiToast("尚未安装播放器，接下来安�?", Toast.LENGTH_SHORT);
                    return true;
                }
                else if (!versionName.equals(appVersionName))
                {
                    threadToUiToast("播放器需要更新，接下来更�?", Toast.LENGTH_SHORT);
                    return true;
                }
                else
                {
                    return false;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取版本�?
     *
     * @return 当前应用的版本号
     */
    private String getVersion(String packageName)
    {
        //        try {
        //            PackageInfo info = packagemanager.getPackageInfo(this.getPackageName(), 0);
        //            String version = info.versionName;
        //            return version;
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //            return null;
        //        }

         //获取包管理器;
        PackageManager pManager = MainActivity.this.getPackageManager();
         //获取手机内所有应;
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++)
        {
            PackageInfo appInfo = paklist.get(i); //第i+1个包信息;
            if (appInfo.packageName.equals(packageName))
            {
              Log.d(TAG, "getVersion: ~~~~~~~~~~" + packageName + " version:~~~~~~~~~~~~~~~~ " + appInfo.versionName);
                return appInfo.versionName;
            }
        }
        return null;
    }

      // 客服叫号
    private void print3()
    {
        //  String str ="1234567890ABCDEFGHIJ中华人民共和";
        String time = StringUtils.getDateToString(new Date());

        String str0 = "                                ";
        String str1 = "                      2016-12-13";
        String str2 = "                                ";
        String str3 = "                                ";
        String str4 = "               16号             ";
        String str5 = "                                ";
        String str6 = "                                ";
        String str7 = "理财业务    柜台032             ";
        String str8 = "                                ";
        String str9 = "                                ";
        sendPortText(str9);
        sendPortText(str9);
        sendPortText(str9);
        sendPortText(str9);
        sendPortText(str9);
        sendPortText(str8);
        sendPortText(str7);
        sendPortText(str6);
        sendPortText(str5);
        sendPortText(str4);
        sendPortText(str3);
        sendPortText(str2);
        sendPortText(str1);
        sendPortText(str0);
        sendPortText(str0);
        sendPortText(str0);
        sendPortText(str0);
        sendPortText(str0);
    }

    private void sendPortText(String content)
    {
        byte[] temp = null;
        try
        {
            temp = content.getBytes("gbk");
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        serialCtrlPrinter.sendPortText(serialCtrlPrinter.ComA, temp);
    }

}
