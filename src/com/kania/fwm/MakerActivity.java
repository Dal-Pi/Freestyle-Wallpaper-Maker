package com.kania.fwm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class MakerActivity extends Activity{
	RelativeLayout layoutMaker;
	RelativeLayout layoutItems;
	Button mbtnAdd, mbtnSet;
	
	ImagePresenter presenter;
	
	public static final int REQ_CODE_GALLERY = 10000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maker);
		
		layoutMaker = (RelativeLayout) findViewById(R.id.layoutMaker);
		mbtnAdd = (Button) findViewById(R.id.btnAddImage);
		mbtnSet = (Button) findViewById(R.id.btnSetWallpaper);
		layoutItems = (RelativeLayout) findViewById(R.id.layoutItems);
		
		presenter = new ImagePresenter(this, layoutItems);
		
		layoutItems.setOnLongClickListener(presenter.getBackgroundClickListener());
		
		mbtnAdd.setOnTouchListener(presenter.getButtonTouchListener());
		mbtnAdd.setOnClickListener(presenter.getAddbtnClickListener());
		mbtnSet.setOnTouchListener(presenter.getButtonTouchListener());
		mbtnSet.setOnClickListener(presenter.getSetbtnClickListener());
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
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		presenter.requestMakeContextMenu(menu, v, menuInfo);
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		presenter.requestHandleMenuSelect(item.getItemId());
		return super.onContextItemSelected(item);
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
