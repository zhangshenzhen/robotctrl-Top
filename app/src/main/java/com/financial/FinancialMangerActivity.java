package com.financial;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaRouter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brick.robotctrl.R;
import com.hdos.idCardUartDevice.JniReturnData;
import com.hdos.idCardUartDevice.publicSecurityIDCardLib;
import com.presentation.IdCardPresentation;
import com.rg2.activity.BaseActivity;
import com.rg2.utils.LogUtil;

import java.io.UnsupportedEncodingException;

import butterknife.ButterKnife;


public class FinancialMangerActivity extends BaseActivity {
    private byte[] name = new byte[32];
    private byte[] sex = new byte[6];
    private byte[] birth = new byte[18];
    private byte[] nation = new byte[12];
    private byte[] address = new byte[72];
    private byte[] Department = new byte[32];
    private byte[] IDNo = new byte[38];
    private byte[] EffectDate = new byte[18];
    private byte[] ExpireDate = new byte[18];
    private byte[] pErrMsg = new byte[20];
    private byte[] BmpFile = new byte[38556];
    String port = "/dev/ttyUSB0";

    public LinearLayout llGroup;
    private publicSecurityIDCardLib iDCardDevice;
    public static boolean IDflag = false;
    private TextView mUserNameTv;
    private TextView mIdNumberTv;
    private TextView mAddressTv;
    private Button mSubmitBtn;
    private TextView mtvBack;

    private EditText metphone;

    //副屏
    private IdCardPresentation mInputFingerPresentation;

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setContentView(R.layout.activity_financial);

        iDCardDevice = new publicSecurityIDCardLib();
    }

    @Override
    protected void initData() {
        mUserNameTv = (TextView) findViewById(R.id.tv_userName);
        mIdNumberTv = (TextView) findViewById(R.id.tv_idNumber);
        metphone = (EditText) findViewById(R.id.et_phone);
        mSubmitBtn = (Button) findViewById(R.id.btn_submit);
        mtvBack = (TextView) findViewById(R.id.tv_back);
        mSubmitBtn.setOnClickListener(this);
        mtvBack.setOnClickListener(this);
        hdler.sendEmptyMessage(1);
    }


    @Override
    protected void initViewData() {

    }


    @Override
    protected void initEvent() {

    }

    /*身份证选卡*/
    public void click4(View view) {
        byte[] cmdSelect = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x02, 0x21};
        byte[] response = null;
        returnData = iDCardDevice.idSamDataExchange(returnData, port, cmdSelect);
        if (returnData.result > 0) {
            response = iDCardDevice.strToHex(returnData.iDCardData, returnData.result);
            showString("选成功");
            showString(response, returnData.result);
        } else {
            showString("选卡失败");
        }
        LogUtil.e("IDcardActivity", ".........................54");
    }

    /*身份证寻卡*/
    JniReturnData returnData = new JniReturnData();

    public void click6(View view) {
        byte[] cmdRequst = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22};
        byte[] response = null;
        returnData = iDCardDevice.idSamDataExchange(returnData, port, cmdRequst);
        if (returnData.result > 0) {
            response = iDCardDevice.strToHex(returnData.iDCardData, returnData.result);
            showString("寻卡成功");
            showString(response, returnData.result);
        } else {
            showString("寻卡失败");
        }
        LogUtil.e("IDcardActivity", ".........................55");
    }

    Handler hdler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
           //                 getCard();
                        }
                    }).start();
                    break;
            }
        }
    };


    private void getCard() {
        int retval;
        //  llGroup.removeAllViews();// 清空
        String pkName;
        pkName = getPackageName();
        pkName = "/data/data/" + pkName + "/lib/libwlt2bmp.so";
        try {
            retval = iDCardDevice.readBaseMsg(port, pkName, BmpFile, name, sex, nation, birth, address, IDNo, Department,
                    EffectDate, ExpireDate, pErrMsg);
            if (retval < 0) {
                // showString("读卡错误，原因：" + new String(pErrMsg, "Unicode"));
                LogUtil.e("TAG", "读卡错误1111");
                hdler.sendEmptyMessageDelayed(1, 1000);
            } else {
                LogUtil.e("TAG", new String(name, "Unicode"));
                LogUtil.e("TAG", new String(IDNo, "Unicode"));
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            mUserNameTv.setText(new String(name, "Unicode"));
                            mIdNumberTv.setText(new String(IDNo, "Unicode"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LogUtil.e("IDcardActivity", ".........................56");
    }


    /*读身份证信息*/
    public void click5(View view) {
        int retval;
        llGroup.removeAllViews();// 清空
        String pkName;
        pkName = this.getPackageName();
        pkName = "/data/data/" + pkName + "/lib/libwlt2bmp.so";
        try {
            retval = iDCardDevice.readBaseMsg(port, pkName, BmpFile, name, sex, nation, birth, address, IDNo, Department,
                    EffectDate, ExpireDate, pErrMsg);
            if (retval < 0) {
                showString("读卡错误，原因：" + new String(pErrMsg, "Unicode"));
            } else {
                int[] colors = iDCardDevice.convertByteToColor(BmpFile);
                Bitmap bm = Bitmap.createBitmap(colors, 102, 126, Bitmap.Config.ARGB_8888);
                Bitmap bm1 = Bitmap.createScaledBitmap(bm, (int) (102 * 1), (int) (126 * 1), false);
                ImageView imageView = new ImageView(this);
                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                imageView.setImageBitmap(bm);
                llGroup.addView(imageView);

                showString("");
                showString("名字=" + new String(name, "Unicode"));
                showString("性别=" + new String(sex, "Unicode"));
                showString("民族=" + new String(nation, "Unicode"));
                showString("生日=" + new String(birth, "Unicode"));
                showString("地址=" + new String(address, "Unicode"));
                showString("身份证号=" + new String(IDNo, "Unicode"));
                showString("发卡机构=" + new String(Department, "Unicode"));
                showString("有效日期=" + new String(EffectDate, "Unicode") + "至" + new String(ExpireDate, "Unicode"));
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LogUtil.e("IDcardActivity", ".........................57");
    }

    public void click2(View view) {
        llGroup.removeAllViews();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
//		commonApi.setGpioOut(125, 0);
        super.onDestroy();
    }

    //Display
    public void showString(String RecvBuffer) {
        TextView tv = new TextView(this);
        tv.setText(RecvBuffer);
        llGroup.addView(tv);
    }

    public void showString(byte[] RecvBuffer, int length) {
        String logOut = new String();
        for (int i = 0; i < length; i++) {
            logOut = logOut + String.format("%02x ", RecvBuffer[i] & 0xFF);
        }
        TextView tv = new TextView(this);
        tv.setText(logOut);
        llGroup.addView(tv);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                Log.d("","....................");
                finish();
                break;
            case R.id.btn_submit:
                eventTouch();
                break;
        }
    }

    String mUserName;
    String mIdNumber;
    String mphoneNumber;

    private void eventTouch() {
        mUserName = mUserNameTv.getText().toString();
        mIdNumber = mIdNumberTv.getText().toString();
        mphoneNumber = metphone.getText().toString().trim();

     /*   if (StringUtils.stringIsEmpty(mUserName) || StringUtils.stringIsEmpty(mIdNumber)) {
            ToastUtil.show(getApplicationContext(), "请刷身份证");
            return;
        }
        if (StringUtils.stringIsEmpty(mphoneNumber)) {
            ToastUtil.show(getApplicationContext(), "请输入手机号");
            return;
        }*/

        startActivityForResult(new Intent(getApplicationContext(), ManageFinaicalSelect.class), 1);

    }

    // String post(String url,String json)throws IOException{
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            finish();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        updatePresentation();
        LogUtil.e("updatePresentation", ".........................55");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void updatePresentation() {
        // Log.d(TAG, "updatePresentation: ");
        //得到当前route and its presentation display
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;
        // 注释 : Dismiss the current presentation if the display has changed.
        if (mInputFingerPresentation != null && mInputFingerPresentation.getDisplay() != presentationDisplay) {
            mInputFingerPresentation.dismiss();
            mInputFingerPresentation = null;
        }
        if (mInputFingerPresentation == null && presentationDisplay != null) {
            // Initialise a new Presentation for the Display
            mInputFingerPresentation = new IdCardPresentation(this, presentationDisplay);
            //把当前的对象引用赋值给BaseActivity中的引用;
            mPresentation = mInputFingerPresentation;
            mInputFingerPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mInputFingerPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                mInputFingerPresentation = null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

}
