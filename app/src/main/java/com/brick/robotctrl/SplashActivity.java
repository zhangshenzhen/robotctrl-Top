package com.brick.robotctrl;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.VideoView;

import com.rg2.utils.WifiAdmin;

/**
 * Created by shenzhen on 2017/1/7.
 */

public class SplashActivity extends BaseActivity {

    private static final String TAG ="SplashActivity" ;
    public  String OPPOR9sk = '"'+"OPPOR 9sk"+'"';

    private VideoView vv;
    private  Uri mUri;
    //无线网工具类
    public WifiAdmin wifiAdmin;

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setContentView(R.layout.activity_splash);//主屏幕;
        vv = (VideoView) findViewById(R.id.vv);

    }

    @Override
    protected void initData() {
         mUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.reds);
         vv.setVideoURI(Uri.parse(String.valueOf(mUri)));

         vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                  @Override
               public void onPrepared(MediaPlayer mediaPlayer) {
                   if(vv != null) {
                    Log.i(TAG, "initData:VideoView "+vv);
                      vv.start();
                    }
                  }
              });
    }
    @Override
    protected void initViewData() {

    }

    @Override
    protected void initEvent() {
        //视频播放的监听事件;
        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
            startActivityForResult( new Intent(SplashActivity.this, MainActivity.class),1);
            }
        });
    }

    @Override
    protected void onResume() {
        // Log.d(TAG, "onResume: ");
        super.onResume();
      //  autoConnectWifi();
        // Register a callback for all events related to live video devices
       // mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
        // Update the displays based on the currently active routes
       // updatePresentation();//在父类中调用
    }

    @Override
    protected void onPause() {
        // Log.d(TAG, "onPause: ");
        super.onPause();

    }

    @Override
    protected void onStop() {
         super.onStop();
         if(vv != null){
             vv=null;
         }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    //代码连接无线网
    public void autoConnectWifi(){
        wifiAdmin = new WifiAdmin(SplashActivity.this);
        if(!OPPOR9sk.equals(wifiAdmin.getSSID())) {//如果已经里连接默认网络不要重复连接；
            wifiAdmin.openWifi();
            wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo("OPPO R9sk", "12345678", 3));
            Toast.makeText(SplashActivity.this, "准备连接网络", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "默认网络:");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 &&resultCode== Activity.RESULT_OK)
        {
            finish();
        }
    }


}
