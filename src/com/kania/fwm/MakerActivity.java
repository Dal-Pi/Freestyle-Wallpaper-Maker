package com.kania.fwm;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class MakerActivity extends Activity{
	RelativeLayout layoutMaker;
	ImageButton mImgbtnAdd, mImgbtnSet;
	
	ImagePresenter presenter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maker);
		
		layoutMaker = (RelativeLayout) findViewById(R.id.layoutMaker);
		mImgbtnAdd = (ImageButton) findViewById(R.id.imgbtnAddImage);
		mImgbtnSet = (ImageButton) findViewById(R.id.imgbtnSetWallpaper);
		
		presenter = new ImagePresenter(this, layoutMaker);
		
		mImgbtnAdd.setOnTouchListener(presenter.getButtonListener());
		mImgbtnSet.setOnTouchListener(presenter.getButtonListener());
	}
	
	@Override
	protected void onResume() {
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		super.onResume();
	}
}
