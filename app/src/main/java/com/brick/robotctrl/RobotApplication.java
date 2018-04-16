package com.brick.robotctrl;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.rg2.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by kjnijk on 2016-08-31.
 */
public class RobotApplication extends Application {
    private final static String TAG = "RobotApplication";
  /*  protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Debug.startMethodTracing("zhang_san");
    }*/

    //获取上下文
    private static Context context;
    public static Context getAppContext() {
        return RobotApplication.context;
    }
    //发卡机串口全局引用
    public static SerialCtrl serialCtrlcard ;
    @Override
    public void onCreate() {
        super.onCreate();
        RobotApplication.context = getApplicationContext();
           //初始化服务
          //  Intent stopIntent = new Intent(this, ZIMEAVDemoService.class);
          //   stopService(stopIntent);
              //启动错误捕获日志
      /*  CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);*/

        //获取最大音乐量值
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int  current = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
        Log.e(TAG, "设置前媒体音量 ："+current);

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,7,0);
        int  current2 = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
        Log.e(TAG, "当前媒体音量 ："+current2);

        //初始化发卡机
      // serialCtrlcard = new SerialCtrl(this, new Handler(), "ttyS3", 9600, "robotctrl");
       // serialCtrlcard.sendPortData(serialCtrlcard.ComA,"55AA7E0004020100840D");//开始
//        LogUtil.d(TAG,"serialCtrlcardComA:"+serialCtrlcard.ComA);
//        LogUtil.d(TAG,"serialCtrlcardserialCOM:"+serialCtrlcard.serialCOM);
//        LogUtil.d(TAG,"serialCtrlcardserialBaud:"+serialCtrlcard.serialBaud);
         //初始化程序崩溃调用
        Log.d(TAG, "..........4");
        Thread.currentThread().setUncaughtExceptionHandler(new MyexceptionHandler());
    }

    private class MyexceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override//等发现了为捕获的异常的时候调用的方法；
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e("MyexceptionHandler", "......程序发现了异常，被哥们捕获了"+ex);
            StringBuffer sb = new StringBuffer();
            Date date = new Date();
            //格式化时间
            String time = DateFormat.getInstance().format(date);
            sb.append("Time:");
            sb.append(time+"\n");//当前的系统时间；
            Field[] fields = Build.class.getDeclaredFields();
           /* for(Field field : fields){
                try {
                   String name = field.getName();
                    String value =  (String)field.get(null);
                    sb.append(name+"="+value+"\n"); //追加信息；
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/

            //错误日志, 把异常写到文件中；
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            sb.append(sw.toString());//追加；

            try {
                File file = new File(Environment.getExternalStorageDirectory(),"Robort_error"+time+".txt");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(sb.toString().getBytes());
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
       /* android.os.Process.killProcess(android.os.Process.myPid());
        Log.e("MyexceptionHandler", "......启动自杀方式，再次激活程序");*/
        }
    }
/*
* 程序终止的时候执行*/

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActivityCollerctor.finishAll();
        LogUtil.i(TAG,"关机啦");
    }
  /*
* 程序低内存的时候执行*/

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
    /*
  * 程序清理内存的时候执行*/
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}
