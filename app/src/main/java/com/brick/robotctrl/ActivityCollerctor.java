package com.brick.robotctrl;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lx on 2017/8/29.
 */

public class ActivityCollerctor {
    /*
    * 创建有个存储Activity 的集合
    * */
    public static List<Activity> listActivity = new ArrayList<>();


    /*创建一个方法, 方便把Activity 存入集合方法;
    * */
    public static void AddActivity(Activity activity){
       listActivity.add(activity);
    }

    /*创建一个方法, 方便把Activity 从集合中移除的方法;
   * */
    public static void RemoveActivity(Activity activity){
        listActivity.remove(activity);
    }

    /*销毁Activity的方法;
    * */
    public static void finishAll(){
        for (Activity activity: listActivity) {
            if (!activity.isFinishing()){
                activity.finish();
            }
        }
    }
    public static void finishA(Activity ac){
       ac.finish();
    }
}
