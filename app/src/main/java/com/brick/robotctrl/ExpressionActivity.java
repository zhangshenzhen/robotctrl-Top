package com.brick.robotctrl;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.ant.liao.FrameAnimation;
import com.ant.liao.GifView;
import com.facebook.drawee.view.SimpleDraweeView;

public class ExpressionActivity extends BaseActivity  {
	private static final String TAG = "ExpressionActivity";

	private static GifView gifView;
	private int index = 0;
//	UserTimer userTimer = null;
	private static int currentIndex = -1;
	private GestureDetector mGestureDetector;
	private int screenWidth;
	private int screenHeight;
	public static ImageView imgv;
	public static SimpleDraweeView f;
	public static FrameAnimation frameAnimation ;
	public static int [] arrjpg2 = { R.array.duzui0,R.array.duzui,R.array.superise,R.array.huachi,R.array.kelian,
			R.array.keai,R.array.cry,R.array.tiaoqi,R.array.weiqu,R.array.weixiao,R.array.yumen,R.array.chongdian};


	enum EXPRESSION{
		机器人愤怒(R.drawable.fennu, "fennu", 0),
		机器人嘟嘴(R.drawable.duzui, "duzui", 1),
		机器人惊讶(R.drawable.jingya, "jingya", 2),
		机器人花痴(R.drawable.huachi, "huachi", 3),
		机器人可怜(R.drawable.kelian, "kelian", 4),
		机器人可爱(R.drawable.keai, "keshui", 5),
		机器人哭泣(R.drawable.kuqi, "kuqi", 6),
		机器人调皮(R.drawable.tiaopi, "tiaopi", 7),
		机器人委屈(R.drawable.weiqu, "weiqu", 8),
		机器人微笑(R.drawable.weixiao, "weixiao", 9),
		机器人郁闷(R.drawable.yumen, "yumen", 10),
		机器人充电(R.drawable.chongdian, "chongdian", 11);


		private int id;
		private String name;
		private int index;
		EXPRESSION(int id, String name, int index) {
			this.id = id;
			this.name = name;
			this.index = index;
		}
		public static int getExpressionSize() {
			int ExpressionSize = 1;
			for ( EXPRESSION exp: EXPRESSION.values()) {
				ExpressionSize++;
			}
			return ExpressionSize;
		}
		public static EXPRESSION getExpression( int index ) {
			for ( EXPRESSION exp: EXPRESSION.values()) {
				if ( index == exp.index ) {
					return exp;
				}
			}
			return EXPRESSION.机器人嘟嘴;//return EXPRESSION.机器人可爱;
		}
		public static EXPRESSION getExpression( String name ) {
			for ( EXPRESSION exp: EXPRESSION.values()) {
				if ( name.equals(exp.name) ) {
					return exp;
				}
			}
			return EXPRESSION.机器人嘟嘴;
		}
	}

	@Override
	protected void initViews(Bundle savedInstanceState) {
		//Fresco.initialize(this);//初始化
		setContentView(R.layout.gif);

		screenWidth = getWindowManager().getDefaultDisplay().getWidth();       // 屏幕宽（像素，如：480px）
		screenHeight = getWindowManager().getDefaultDisplay().getHeight();      // 屏幕高（像素，如：800p）
	    Log.e("TAG" + "  getDefaultDisplay", "screenWidth=" + screenWidth + "; screenHeight=" + screenHeight);
		    Intent intent = getIntent();
		 //   	index = intent.getStringExtra("index");
		 //      Log.e("express","................"+express);
		index = intent.getIntExtra("index",1);
		 Log.i("express","........<...>........"+index);
		  //  userTimer = new UserTimer();
          imgv =  (ImageView) findViewById(R.id.imgv);

		//暂时注释
		/*gifView = (GifView) findViewById(R.id.gif2);
		gifView.setGifImageType(GifView.GifImageType.COVER);*/

		//屏幕适配;
//     gifView.setShowDimension(screenWidth, screenHeight);

		 changeExpression(index);
	//	mGestureDetector = new GestureDetector(this, new ExGestureListener());
	}

/*
* 暂时不用先屏蔽掉*/
	@Override
	protected void initData() {
		/*gifView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent( ExpressionActivity.this , PrintActivity.class));
			}
		});*/
	}

	@Override
	protected void initViewData() {
	}
	@Override    //触摸表情返回到选择界面
	protected void initEvent() {

	}

	int doX,  doY;
	int movX,  movY;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		if (mGestureDetector.onTouchEvent(event))
//			return true;

		return super.onTouchEvent(event);
	}


	public static void changeExpression(int index) {
		Log.d(TAG, "changeExpression: current expression:" + currentIndex + "\tset expression:" + index);
		if ( currentIndex != index ) {
			if (index > arrjpg2.length-1) {
				//如果表情角标大于表情枚举个数，从1开始一次递增显示表情
				index = index -(arrjpg2.length)+1;
				Log.d(TAG, "changeExpression 大于枚举长度: ..... " + index);
			}
			System.gc();//垃圾回收机制
			/*gifView.setGifImage(EXPRESSION.getExpression(index).id);
			gifView.showAnimation();*/
			//Glide.with(RobotApplication.getAppContext()).load(EXPRESSION.getExpression(index).id).asGif().skipMemoryCache(false).priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(igv);
		    currentIndex = index;

			animiantor(index);

			Log.d(TAG, "changeExpression: ..... " + index);
		}else {
		  Log.d(TAG, "changeExpression等于当前表情编号: ..... " + index);

		}
	}
	private static void animiantor(int index) {
		if ( frameAnimation != null){
			frameAnimation.release();
			frameAnimation =null;
		}
		frameAnimation = new FrameAnimation(imgv, getRes(index), 400, true);
	}
	/**
	 * 获取需要播放的动画资源
	 */
	public static int[] getRes(int index) {
		TypedArray typedArray =RobotApplication.getAppContext().getResources().obtainTypedArray(arrjpg2[index]);
		int len = typedArray.length();
		int[] resId = new int[len];
		for (int i = 0; i < len; i++) {
			resId[i] = typedArray.getResourceId(i, -1);
		}
		typedArray.recycle();
		return resId;
	}

	public void onClick(View v) {
		//clearTimerCount();
		int index = currentIndex;
		index++;
		if ( index >= EXPRESSION.getExpressionSize())
		 index = 0;
		/*//如果表情角标大于表情美剧个数，从0开始一次递增显示表情
		index = index - EXPRESSION.getExpressionSize();*/
		Log.d(TAG, "index - EXPRESSION.getExpressionSize(): ..... " + index);
		changeExpression(index);

	  }
	public static void startAction(Context context, int index) {
		Log.d(TAG, "Expression: clear Event");
		Intent changeMotionIntent = new Intent();
		changeMotionIntent.setClass(context, ExpressionActivity.class);
		changeMotionIntent.putExtra("index", index);
		context.startActivity(changeMotionIntent);
	}
	// 接收尚未写好，需要再try中判断传递的参数是什么类型
	public static void startActions(Context context, String expName) {
		Intent changeMotionIntent = new Intent();
		changeMotionIntent.setClass(context, ExpressionActivity.class);
		changeMotionIntent.putExtra("expName", expName);
		context.startActivity(changeMotionIntent);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		//updatePresentation();
		Log.i(TAG, "生命------onRestart:updatePresentation 执行了");
	}

	@Override
	protected void onResume() {
		super.onResume();
		//updatePresentation();//父类中已经被调用了;
		Log.i(TAG, "生命------onResume:ExpresionActivity updatePresentation 执行了");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "生命------onPause:updatePresentation 执行了");
	}

	@Override
    protected void onStop() {
        currentIndex = -1;		// restart expression
        super.onStop();

        Log.i(TAG, "生命------updatePresentation onStop");
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "生命------updatePresentation onDestroy");
	}

	@Override
	protected void onStart() {
		Log.i(TAG, "生命------onStart");
		super.onStart();
	}
}
