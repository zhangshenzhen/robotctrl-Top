package com.rg2.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;


/**
 * 作者：王先云 on 2016/7/1 10:23
 * 邮箱：wangxianyun1@163.com
 * 描述：一句话简单描述
 */
public abstract class BaseActivity extends Activity implements View.OnClickListener {
    private static final String TAG ="BaseActivity" ;
    private static int timerOutCount = 0;


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
    public Context mContext;
    public  static Context Context;

    public String mSecondaryTouch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        Context = this;
        //单例
       // instance = UserInfo.getInstance();
        super.onCreate(savedInstanceState);
        //启动时隐藏软键盘,但EditText的光标还在，点击编辑框才弹出软键盘；
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        decorView = getWindow().getDecorView();


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
        Log.i(TAG,"。。系统获取了焦点");
        if (hasFocus){
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
    public void onClick(View v){
    }

    @Override
    protected void onPause() {
        super.onPause();
    }




    @Override
    protected void onResume() {

        super.onResume();
        // Listen for changes to media routes.

    }

}
