package com.rg2.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.brick.robotctrl.*;
import com.rg2.listener.MyOnClickListener;
import com.rg2.utils.CityDialog;


/**
 * 作者：王先云 on 2016/8/29 15:05
 * 邮箱：wangxianyun1@163.com
 * 描述：一句话简单描述
 */
public class MainActivity extends com.brick.robotctrl.BaseActivity
{

    private TextView mUserNameTv;
    private TextView mIdNumberTv;
    private TextView mAddressTv;
    private Button mSubmitBtn;



    @Override
    protected void initData()
    {

    }

    @Override
    protected void initViews(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_id_card);
        mUserNameTv = (TextView) findViewById(R.id.tv_userName);
        mIdNumberTv = (TextView) findViewById(R.id.tv_idNumber);
        mAddressTv = (TextView) findViewById(R.id.tv_address);
        mSubmitBtn = (Button) findViewById(R.id.btn_submit);
    }

    @Override
    protected void initEvent()
    {
        mAddressTv.setOnClickListener(this);
        mSubmitBtn.setOnClickListener(this);
    }

    @Override
    protected void initViewData(){
    }

    @Override
    public void onClick(View v)
    {
        super.onClick(v);

        if (v == mAddressTv)
        {
            CityDialog mCityDialog = new CityDialog();
            mCityDialog.showCityDialog(MainActivity.this, new MyOnClickListener()
            {
                @Override
                public void onClicked(String content)
                {
                    mAddressTv.setText(content);
                }
            });
        }
        else if (v == mSubmitBtn)
        {

            String mUserName= mUserNameTv.getText().toString();
            String mIdNumber = mIdNumberTv.getText().toString();
            String mAddress = mAddressTv.getText().toString();


//            if(StringUtils.stringIsEmpty(mUserName) || StringUtils.stringIsEmpty(mIdNumber))
//            {
//                ToastUtil.show(MainActivity.this,"请刷身份证");
//                return;
//            }


//            if(StringUtils.stringIsEmpty(mAddress))
//            {
//                ToastUtil.show(MainActivity.this,"请选择公司所在区域");
//                return;
//            }

          //  startActivity(new Intent(this,TwoActivity.class));
        }
    }
}
