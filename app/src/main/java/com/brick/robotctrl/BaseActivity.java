package com.brick.robotctrl;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "BaseActivity";
    //    UserTimer userTimer = null;
    private static int timerOutCount = 0;
    private int screenWidth;
    private int screenHeight;
    protected AudioManager mAudioManager;

    private GestureDetector mGestureDetector;
    private View decorView;

    public static void clearTimerCount() {
        timerOutCount = 0;
    }

    public static void addTimerCount() {
        timerOutCount++;
    }

    public static int getTimerCount() {
        return timerOutCount;
    }

    //gr2中的
    public Context mContext;

    public String mSecondaryTouch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        //设置为横屏幕;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();       // 屏幕宽（像素，如：480px）
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();      // 屏幕高（像素，如：800p）

        decorView = getWindow().getDecorView();

        //添加了gr2包下的BaseActivity,
        //设置为横屏幕;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
        initViews(savedInstanceState);//主屏幕
        Log.e("Baseactivity","....1");
        //获取媒体路由器

        mSecondaryTouch = System.getProperty("persist.secondary.touch", "ft5x06");

        //intent中获取数据
        initData();
        // 代码和初始化数据
        initViewData();
        //事件;
        initEvent();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i(TAG, "系统获取了焦点");
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }


    /**
     * 从intent中获取数据
     */
    protected abstract void initData();
    /**
     * 存放从xml中获取ui,例如 findViewById
     */
    protected abstract void initViews(Bundle savedInstanceState);


    /**
     * 初始化页面UI事件,例如 setOnClickListener
     */
    protected abstract void initEvent();

    /**
     * 存放刷新页面的代码和初始化数据
     */
    protected abstract void initViewData();


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();

    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart: ");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        clearTimerCount();
        Log.d(TAG, "onResume: ");
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        ActivityCollerctor.RemoveActivity(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

    /*    if (mGestureDetector.onTouchEvent(event))
            return true;

        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:

                break;
        }*/

        return super.onTouchEvent(event);
    }


    @Override
    public void onClick(View v) {

    }

    public void onShutdown() {
    }
}
