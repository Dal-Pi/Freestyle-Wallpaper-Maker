package com.kania.fwm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class MakerActivity extends Activity{
	RelativeLayout layoutMaker;
	RelativeLayout layoutItems;
	ImageButton mImgbtnAdd, mImgbtnSet;
	
	ImagePresenter presenter;
	
	public static final int REQ_CODE_GALLERY = 10000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maker);
		
		layoutMaker = (RelativeLayout) findViewById(R.id.layoutMaker);
		mImgbtnAdd = (ImageButton) findViewById(R.id.imgbtnAddImage);
		mImgbtnSet = (ImageButton) findViewById(R.id.imgbtnSetWallpaper);
		layoutItems = (RelativeLayout) findViewById(R.id.layoutItems);
		
		presenter = new ImagePresenter(this, layoutItems);
		
		mImgbtnAdd.setOnTouchListener(presenter.getButtonTouchListener());
		mImgbtnAdd.setOnClickListener(presenter.getAddbtnClickListener());
		mImgbtnSet.setOnTouchListener(presenter.getButtonTouchListener());
		mImgbtnSet.setOnClickListener(presenter.getSetbtnClickListener());
	}
	
	@Override
	protected void onResume() {
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		super.onResume();
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQ_CODE_GALLERY:
			if (data != null) {
				Uri imgUri = data.getData();
				presenter.addNewItemToMaker(imgUri);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
