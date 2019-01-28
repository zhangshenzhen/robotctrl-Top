package com.brick.robotctrl;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.udpwork.ssdb.SSDB;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SSDBTask {
    private static final String TAG = "SSDBTask";
    public static final int ENABLECTRL = 0x0001;            // enable control
    public static final int DIRCTRLWARNING = 0x0002;
    private static final int ACTION_CONNECT = 0x0001;
    private static final int ACTION_DISCONNECT = 0x0002;
    public static final int ACTION_HSET = 0x0004;
    public static final int ACTION_HGET = 0x0008;
    private Handler contextHandler = null;
    private Context context = null;
    private SSDB ssdbClient = null;
    // public String serverIp = "120.25.66.79";
    public String serverIp = "222.190.128.98";
    public int serverPort = 20177;
    public String robotName = "hs23";
    public String robotLocation = "江苏红石信息集成服务有限公司";
    public String videoPlayList = null;
    private final int serverSite = 222;
    private String serverSiteString = null;

    public void setRobotName(@NonNull String robotName) {
        if (!TextUtils.isEmpty(robotName))
            this.robotName = robotName;
    }
    public void setRobotLocation(@NonNull String robotLocation) {
        if (!TextUtils.isEmpty(robotName))
            this.robotLocation = robotLocation;
    }
    public void setServerPort(int serverPort) {
        if (serverPort > 0 && serverPort < 65536) {
            this.serverPort = serverPort;
        }
    }

    public void setServerIP(@NonNull String serverIp) {
        if (!TextUtils.isEmpty(serverIp)) {
            this.serverIp = serverIp;
        }
    }

   // Timer timer = new Timer();

    private static class CmdEntry<T, K, V> {
        public final T cmdType;
        public final K key;
        public final V val;

        public CmdEntry(T type, K key, V val) {
            this.cmdType = type;
            this.key = key;
            this.val = val;
        }

        public static <T, K, V> CmdEntry<T, K, V> create(T type, K key, V val) {
            return new CmdEntry<>(type, key, val);
        }

        @Override
        public int hashCode() {
            int hash = cmdType.hashCode();
            hash = hash * 31 + key.hashCode();
            hash = hash * 31 + val.hashCode();
            return hash;
        }

        @Override
        public String toString() {
            return String.format("type:%s\tkey:%s\tval:%s", cmdType, key, val);
        }
    }

    //保存命令列表 NOTE Queue用法
    Queue<CmdEntry<Integer, String, String>>  cmdList = new LinkedList<>();
    ScheduledThreadPoolExecutor mexecutor = new ScheduledThreadPoolExecutor(3);
    public SSDBTask(Context context, Handler handler) {
        assert context != null;
        assert handler != null;
        this.context = context;
        this.contextHandler = handler;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        setRobotName(sp.getString(context.getString(R.string.robotName), robotName));
        setServerIP(sp.getString(context.getString(R.string.serverIp), serverIp));
        String port = sp.getString(context.getString(R.string.serverPort), String.valueOf(serverPort));
        try {
            setServerPort(Integer.parseInt(port));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        //this 代表当前类，
       // timer.schedule(this, 100, 100);
       // mexecutor
        mexecutor.scheduleWithFixedDelay(runtask, 100, 80, TimeUnit.MILLISECONDS);
        connect();
//     pushFileList();
    }

    public void pushFileList() {
        try {
            File file = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/Movies");
            File[] files = file.listFiles();
            if (files.length == 0) {
                Log.d(TAG, "pushFileList: 为空");
            }
            if (files.length > 1) {
                videoPlayList = null;
                for (int i = 0; i < files.length - 1; i++) {
                    if (
                            files[i].getAbsolutePath().endsWith(".avi") ||
                            files[i].getAbsolutePath().endsWith(".mp4") ||
                            files[i].getAbsolutePath().endsWith(".3gp") ||
                            files[i].getAbsolutePath().endsWith(".jpg")

                            ) {
                        Log.d(TAG, "getFiles: " + files[i].getAbsolutePath().substring(files[i].getAbsolutePath().lastIndexOf("/") + 1));
                        if (videoPlayList != null) {
                            videoPlayList = videoPlayList + files[i].getAbsolutePath().substring(files[i].getAbsolutePath().lastIndexOf("/") + 1) + " ";
                        } else {
                            videoPlayList = files[i].getAbsolutePath().substring(files[i].getAbsolutePath().lastIndexOf("/") + 1) + " ";
                        }
                    }
                }
                videoPlayList = videoPlayList + files[files.length - 1].getAbsolutePath().substring(files[files.length - 1].getAbsolutePath().lastIndexOf("/") + 1);
            }
            if (files.length == 1) {
                videoPlayList = files[0].getAbsolutePath().substring(files[0].getAbsolutePath().lastIndexOf("/") + 1);
            }
            Log.d(TAG, "SSDBTask: " + videoPlayList);
            SSDBQuery(ACTION_HSET, event[Key_VideoPlayList], videoPlayList);
        } catch (Exception e) {
            Log.d("getfile", "查找异常!");
            System.out.println(e.toString());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void connect() {
        if (ssdbClient != null) {
            ssdbClient.close();
            ssdbClient = null;
        }
        stop = false;
        SSDBQuery(ACTION_CONNECT);
        contextHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: stop 4: " + stop);
                Toast.makeText(context, "Connecting to " + serverIp + ":" + serverPort, Toast.LENGTH_SHORT).show();
            }
        });
        pushFileList();
        SSDBQuery(ACTION_HSET, "appVersion", context.getString(R.string.appVersion));
        SSDBQuery(ACTION_HSET, event[Key_Location], robotLocation);
    }

    public void disConnect() {
        if (ssdbClient != null) {
            SSDBQuery(ACTION_DISCONNECT);
            stop = true;
        }
    }

    public boolean stop = true;
    public static final int Key_Event = 0;
    public static final int Key_DirCtrl = 1;
    public static final int Key_SetParam = 2;
    public static final int Key_VideoPlay = 3;
    public static final int Key_VideoInfo = 4;        // 视频播放时1s上传一次info，不需要接收该关键字
    public static final int Key_VideoPlayList = 5;
    public static final int Key_RobotMsg = 6;
    public static final int Key_BatteryVolt = 7;
    public static final int Key_NetworkDelay = 8;
    public static final int Key_Location = 9;
    public static final int Key_ChangeBrow = 10;
    public static final int Key_CurrentTime = 11;
    public static final int Key_DisableAudio = 12;
    public static final int Key_SetVolume = 13;
    public static final int Key_EndVideo = 14;
    public static final int key_ApkUpdate = 15;               //用来更新apk的，跟ssdb没关系，放在这里只是为了统一,15被谁用了
    public static final int Key_Message = 16;
    public static final int key_IDCard = 17;

    public static final String[] event = new String[]{
            "event", "DirCtl", "param", "VideoPlay", "VideoInfo", "VideoPlayList", "RobotMsg", "BatteryVolt", "NetworkDelay", "Location",
            "Brow", "CurrentTime", "DisableAudio", "SetVolume", "EndVideo", "", "Message"};
    ////////////////////////gaowei/////////////////////////////
    public static boolean enableForbidAudio = false;
    public static boolean enableCurrentTime = false;
    public static boolean enableLocation = false;
    public static boolean enableNetworkDelay = false;
    public static boolean enableBatteryVolt = false;
    public static boolean enableRobotMsg = false;             //
    public static boolean enableVideoPlayList = false;        //
    public static boolean enableVideoPlay = false;            //
    //public static boolean enableVideoInfo=false;            //
    ////////////////////////gaowei////////////////////////////
    public static boolean enableDirCtl = false;
    public static boolean enableChangeBrow = false;
    public static boolean enableSetParameter = false;
    public static boolean enableSetVolume = false;
    // public static boolean enableEndVideo = false;
    public static boolean enableGetMessage = false;
    public static boolean  enableSerial = false;
    private int iCount = 0;

    void sendMessageToMain(int Key_Type)     //ssdbtask对象从数据库取key_type所指定的键的值给mainactivity by gaowei                                               //
    {
        try {
            byte[] rlt = ssdbClient.hget(robotName, event[Key_Type]); // check event
            if (rlt != null) {
                Message message = new Message();
                message.what = Key_Type;
                message.obj = new String(rlt, "GBK");
                Log.d(TAG, "what :" + message.what + "----------SSDB-0------ Key:"+(String) message.obj);
                contextHandler.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
 //           SSDBQuery(ACTION_CONNECT);
        }
    }

  Runnable runtask = new Runnable() {
    @Override
    public  synchronized void run() {
        {
            try {
                Log.d(TAG, "---------run-SSDB------- Key:"+cmdList.size());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"查找异常--cmdList-"+e.toString()+" , "+e.getMessage());
                e.printStackTrace();
            }

            if (stop) {
                return;
            }

            while (cmdList.size() > 0) {
             CmdEntry<Integer, String, String> cmd = null;
             try {
                 cmd = cmdList.poll();
                 Log.d(TAG, "----------cmd.cmdType----cmd-1-- Key:"+cmd.cmdType);
                } catch (Exception e) {
                  e.printStackTrace();
                  cmd = cmdList.peek(); //尝试修复cmdList.poll() 出现的异常
                  cmdList.clear();
              Log.d(TAG,"查找异常--cmdList-"+e.toString()+" , "+e.getMessage());
             }
             if (cmd == null) {
               Log.d(TAG, "----------cmd.cmdType----cmd-2-- Key:");
                return;
                }
             Log.d(TAG, "----------cmd.cmdType----cmd-3-- Key:");

                switch (cmd.cmdType) {
                    case ACTION_CONNECT://1
                        try {
                            Log.d(TAG, "run: ACTION_CONNECT");
                            ssdbClient = new SSDB(serverIp, serverPort);
                            stop = false;
                        } catch (Exception e) {
                            Log.d(TAG, "run: ACTION_CONNECT_FAILED");
                            stop = true;
                            e.printStackTrace();
                        }
                        break;
                    case ACTION_DISCONNECT://2
                        if (ssdbClient != null) {
                            ssdbClient.close();
                            ssdbClient = null;
                        }
                        break;
                    case ACTION_HSET://4
                        try {
//                       ssdbClient = new SSDB(serverIp, serverPort);
                         ssdbClient.hset(robotName, cmd.key, cmd.val);
                        } catch (Exception e) {
                            e.printStackTrace();
                            SSDBQuery(ACTION_CONNECT);
                        }
                        break;
                    case ACTION_HGET://8
                        if (++iCount >= 5) {        // 1s check
                            iCount = 0;
                            try {                                       //event 数组内的关键字当做参数
                                byte[] rlt = ssdbClient.hget(robotName, event[Key_Event]); // check event
                                if (rlt != null) {
                                    Message message = new Message();
                                    message.what = Key_Event;
                                    message.obj = new String(rlt, "GBK");
                                    Log.d(TAG, "what :" + message.what + "----------SSDB-1------ Key:"+(String) message.obj);
                                    contextHandler.sendMessage(message);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                iCount = 5;                     // 快速重新获取
                                stop = true;
//                            SSDBQuery(ACTION_CONNECT);    // 异常处理不应该是重新连接
                            }
                        }
                        // by gaowei start//enableVideoPlay
                        if (enableVideoPlay) {
                            sendMessageToMain(Key_VideoPlay);
                        } else {
//                        SSDBQuery(ACTION_HSET, event[Key_VideoPlay], "");
                        }
                        if (enableVideoPlayList) {
                            sendMessageToMain(Key_VideoPlayList);
                        } else {
//                        SSDBQuery(ACTION_HSET, event[Key_VideoPlayList], "");
                        }
                        if (enableRobotMsg) {
                            sendMessageToMain(Key_RobotMsg);
                        } else {
//                        SSDBQuery(ACTION_HSET, event[Key_RobotMsg], "");
                        }
                        if (enableBatteryVolt) {
                            sendMessageToMain(Key_BatteryVolt);
                        } else {
//                        SSDBQuery(ACTION_HSET, event[Key_BatteryVolt], "");
                        }
                        if (enableNetworkDelay) {
                            sendMessageToMain(Key_NetworkDelay);
                        } else {
//                      SSDBQuery(ACTION_HSET, event[Key_NetworkDelay], "");
                        }
                        if (enableLocation) {
                            sendMessageToMain(Key_Location);
                        } else {
//                        SSDBQuery(ACTION_HSET, event[Key_Location]);
                        }
                        if (enableCurrentTime) {
                            sendMessageToMain(Key_CurrentTime);
                        } else {
//                        SSDBQuery(ACTION_HSET, event[Key_CurrentTime]);
                        }
                        if (enableForbidAudio) {
                            sendMessageToMain(Key_DisableAudio);
                        } else {
//                        SSDBQuery(ACTION_HSET, event[Key_DisableAudio]);
                        }
                        if (enableDirCtl) {             // check control move
                            try {
                                byte[] rlt = ssdbClient.hget(robotName, event[Key_DirCtrl]);          // check move control
                                if (rlt != null) {
                                    Message message = new Message();
                                    message.what = Key_DirCtrl;
                                    message.obj = new String(rlt, "GBK");
                                    Log.d(TAG, "what :" + message.what + "----------SSDB-2------ Key:" + (String) message.obj);
                                    contextHandler.sendMessage(message);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
//                            SSDBQuery(ACTION_CONNECT);
                            }
                        } else {
//                        SSDBQuery(ACTION_HSET, event[Key_DirCtrl], "");
                        }
                        if (enableSetParameter) {     // check rate parameter
                            try {
                                byte[] rlt = ssdbClient.hget(robotName, event[Key_SetParam]);
                                if (rlt != null) {
                                    Message message = new Message();
                                    message.what = Key_SetParam;
                                    message.obj = new String(rlt, "GBK");
                                    Log.d(TAG, "what :" + message.what + "----------SSDB-3------ Key:" + (String) message.obj);
                                    contextHandler.sendMessage(message);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
//                            SSDBQuery(ACTION_CONNECT);
                            }
                        } else {
//                        SSDBQuery(ACTION_HSET, event[Key_SetParam], "");
                        }
                        if (enableChangeBrow) {       // check emotion change
                            try {
                                byte[] rlt = ssdbClient.hget(robotName, event[Key_ChangeBrow]);
                                if (rlt != null) {
                                    Message message = new Message();
                                    message.what = Key_ChangeBrow;
                                    message.obj = new String(rlt, "GBK");
                                    Log.d(TAG, "what :" + message.what + "----------SSDB-4------ Key:" + (String) message.obj);
                                    contextHandler.sendMessage(message);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
//                            SSDBQuery(ACTION_CONNECT);
                            }
                        } else {
//                        SSDBQuery(ACTION_HSET, event[Key_ChangeBrow], "");
                        }
                        if (enableSetVolume) {       // check volume change
                            try {
                                byte[] rlt = ssdbClient.hget(robotName, event[Key_SetVolume]);
                                if (rlt != null) {
                                    Message message = new Message();
                                    message.what = Key_SetVolume;
                                    message.obj = new String(rlt, "GBK");
                                    Log.d(TAG, "what :" + message.what + "----------SSDB-5------ Key:" + (String) message.obj);
                                    contextHandler.sendMessage(message);
                                }
                                enableSetVolume = false;
                            } catch (Exception e) {
                                e.printStackTrace();
//                            SSDBQuery(ACTION_CONNECT);
                            }
                        }
//                    else {
//                        SSDBQuery(ACTION_HSET, event[Key_ChangeBrow], "");      // 后面不能清
//                    }
                        if (enableGetMessage) {
                            try {
                                byte[] rlt = ssdbClient.hget(robotName, event[Key_Message]);
                                if (rlt != null) {
                                    Message message = new Message();
                                    message.what = Key_Message;
                                    message.obj = new String(rlt, "GBK");
                                    Log.d(TAG, "what :" + message.what + "----------SSDB-6------ Key:" + (String) message.obj);
                                    contextHandler.sendMessage(message);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    default:
                        break;
                }
            }
        }
    }
};
    public void SSDBQuery(int codeType) {
        SSDBQuery(codeType, null, null);
    }

    public void SSDBQuery(int codeType, String key) {
        SSDBQuery(codeType, key, null);
    }

    public synchronized void SSDBQuery(int codeType, String key, String val) {
        try {
            cmdList.add(CmdEntry.create(codeType, key, val));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"查找异常--SSDBQuery-"+e.toString()+" , "+e.getMessage());
        }
    }
}
