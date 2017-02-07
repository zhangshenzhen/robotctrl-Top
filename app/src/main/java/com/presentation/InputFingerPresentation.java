package com.presentation;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

import com.brick.robotctrl.R;

/**
 * Created by shenzhen on 2017/1/13.
 */

public class InputFingerPresentation extends  BasePresentation {

    private TextView mprint;
    private TextView mpaper;

    public InputFingerPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
      protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.persentation_inputfinger);
      }

}
