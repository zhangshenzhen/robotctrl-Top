package com.brick.robotctrl;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
public class ShowProgressDialog {
    public static ProgressDialog wait;
    public static void show(Context context,String msg,Thread thread) {
        final Thread th = thread;
        wait = new ProgressDialog(context);
        wait.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        wait.setTitle(null);
        wait.setIcon(null);
        //设置提示信息
        wait.setMessage(msg);
        //设置是否可以通过返回键取消
        wait.setCancelable(true);
        wait.setIndeterminate(false);
        //设置取消监听
        wait.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                th.interrupt();
            }
        });
        wait.show();
    }
}
