package com.brick.robotctrl;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

import com.kjn.videoview.ADVideo;


public class ADActivity extends BaseActivity {
    private final String TAG = "ADActivity";
   public static Context Adthis;
    private VideoView videoView;
    ADVideo adVideo = null;
    private   String videoPath;
    private boolean flag = true;
    private boolean isOver = false;
    public static String fileName = null;
    private String mode = null;
    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;
    /* 最大声音 */
    private int mMaxVolume;
    /* 当前声音 */
    private int mVolume = -1;
    private final int singleOver = 101;
    private final int videoInfo = 9999;
    private final int PROGRESS = 102;
    private static Handler contextHandler2 = null;
    private String path;
//    private View mVolumeBrightnessLayout;
    /**副屏
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        super.onCreate(savedInstanceState);
        Adthis = this;//在副屏中使用当前Activity的this
        setContentView(R.layout.activity_ad);
         Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        mode = intent.getStringExtra("mode");
        Log.d(TAG, "onCreate: filename : " + fileName +" mode : "+mode);
        // videoview 实现
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setZOrderOnTop(true);
//      mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mGestureDetector = new GestureDetector(this, new MyGestureListener());
       // videoView.setVisibility(View.GONE);
//        videoView.setOnTouchListener(this);
//        videoView.setMediaController(new MediaController(this));  //不需要注释掉即可

    adVideo = new ADVideo(videoView, handler);
        switch (mode){
            case "SingleCycle":
                videoPlayTargetCycle();
                break;
            case "ContinuePlay":
                videoPlay();
                break;
            case "Single":
                Log.d(TAG, "onCreate: filename 主" + fileName);
                videoPlayTargetSingle();
                break;
            case "Cycle":
                videoCycleFrom(fileName);
                break;
        }

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void initViewData() {

    }

  Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case singleOver:
                    String percentString = "100%";
                    Log.d(TAG, "进度singleOver: " + percentString);
                 /*  Message message = new Message();
                    message.what = videoInfo;
                    message.obj = percentString;
                    contextHandler2.sendMessage(message);*/
                    ExpressionActivity.startAction(ADActivity.this, 11);
                    break;
                case PROGRESS:
                    int currentPosition,duration;
                    currentPosition = videoView.getCurrentPosition();
                    duration = videoView.getDuration();
                    int percent = ((currentPosition * 100) / duration);
                    String percentprocessString = String.valueOf(percent)  + "%";
                    Log.d(TAG, "进度PROGRESS: " + percentprocessString);
                  /* Message message1 = new Message();
                    message1.what = videoInfo;
                    message1.obj = fileName+" "+percentprocessString;
                    contextHandler2.sendMessage(message1);*/
                    if(videoView.isPlaying()){
                        handler.sendEmptyMessageDelayed(PROGRESS,1000);
                  }
            }
        }
    };

    private void showVideoDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ADActivity.this);
        builder.setTitle("提示");
        builder.setMessage("路径中无视频文件");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    public void videoPlay(){
        videoPath = Environment.getExternalStorageDirectory()
                .getPath()+"/Movies";
        flag = adVideo.getFiles(videoPath);
        if (flag) {
            new Thread() {
                @Override
                public void run() {
                    adVideo.play();
                }
            }.start();
        } else {
//            showVideoDialog();
        }
    }
    public void videoPlayTargetCycle(){
        videoPath = Environment.getExternalStorageDirectory().getPath()+"/Movies";
        new Thread() {
            @Override
            public void run() {
                try {
//                    File file = new File(videoPath);
//                    File[] files = file.listFiles();
//                    Log.d(TAG, "filename: " + fileName);
//                    for (int i = 0; i < files.length; i++) {
//                        if (files[i].getAbsolutePath().endsWith(fileName)) {
//                            videoPath = files[i].toString();
//                            break;
////                        System.out.println(files[i].toString());
//                        }else{
//                            videoPath = files[0].toString();
//                        }
//                    }
//                    videoView.setVideoPath(videoPath);
//                    Log.d(TAG, "play: starting play: " + videoPath);
//                    videoView.start();
                    if(adVideo.getFiles(videoPath)){
                        adVideo.playSingleCycleWhat(fileName);
                    }
                } catch (Exception e) {
                    Log.d("getfile", "查找异常!");
                    System.out.println(e.toString());
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public void videoPlayTargetSingle(){
        videoPath = Environment.getExternalStorageDirectory().getPath()+"/Movies";
        new Thread() {
            @Override
            public void run() {
                try {
                    if (adVideo.getFiles(videoPath)) {
                        adVideo.playSingleWhat(fileName);
                        Log.d(TAG, "videoPlayTargetSingle: " + isOver);
                    }
                } catch (Exception e) {
                    Log.d("getfile", "查找异常!");
                    System.out.println(e.toString());
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void videoCycleFrom(String str)
    {
        videoPath = Environment.getExternalStorageDirectory()
                .getPath()+"/Movies";
        Log.d(TAG,"name"+ADActivity.fileName);
        flag = adVideo.getFiles(videoPath);
        if (flag) {
            adVideo.playCycleWhat(str);
        }
    }

    public void videoStop(){
        adVideo.stopPlayBack();
    }

    public void videopause(){
        adVideo.pause();
        Log.d(TAG, "videopause: ");
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "生命------onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "生命------onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "生命------onPause");
         finish();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "生命------Stop");
        super.onStop();
         finish();
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "生命------onRestart");
        View decorView = getWindow().getDecorView();
//        Hide both the navigation bar and the status bar.
//        SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
//        a general rule, you should design your app to hide the status bar whenever you
//        hide the navigation bar.
      /*  int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);*/
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "生命------onDestroy");
        super.onDestroy();
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return true;
        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }

        return super.onTouchEvent(event);
    }

    /** 手势结束 */
    private void endGesture() {
        mVolume = -1;
    }
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed (MotionEvent e){
            Log.d(TAG, "onTouch: to MainActivity");
//            startActivity(new Intent().setClass(ADActivity.this, ExpressionActivity.class));
            return true;
        }

        /** 滑动 */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            Display disp = getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();

//            if (mOldX > windowWidth * 4.0 / 5)// 右边滑动
            onVolumeSlide((mOldY - y) / windowHeight);
//            else if (mOldX < windowWidth / 5.0)// 左边滑动
//                onBrightnessSlide((mOldY - y) / windowHeight);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }
    private void onVolumeSlide(float percent) {
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0) {
                mVolume = 0;
            }

            // 显示
//            mOperationBg.setImageResource(R.drawable.video_volumn_bg);
//            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume) {
            index = mMaxVolume;
        } else if (index < 0) {
            index = 0;
        }

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
//        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
//        lp.width = findViewById(R.id.operation_full).getLayoutParams().width
//                * index / mMaxVolume;
//        mOperationPercent.setLayoutParams(lp);
    }

    public static void startAction(Context context, String mode, String str) {
        Intent startIntent = new Intent();
        startIntent.setClass(context, ADActivity.class);
        startIntent.putExtra("mode", mode);
        startIntent.putExtra("fileName", str);
        context.startActivity(startIntent);
    }

    public static void setHandler(Handler handler){
        ADActivity.contextHandler2 = handler;
    }
}
