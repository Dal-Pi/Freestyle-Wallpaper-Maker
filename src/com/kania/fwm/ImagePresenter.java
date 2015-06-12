package com.kania.fwm;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ImagePresenter {
	
	private Activity mContext;
	private ViewGroup mlayoutItmes;
	
//	private ArrayList<RelativeLayout> mImageList;
	
	public ImagePresenter(Activity context, ViewGroup vg) {
		mContext = context;
		mlayoutItmes = vg;
//		mImageList = new ArrayList<RelativeLayout>();
	}
	
	public void getImageFromGallery() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		mContext.startActivityForResult(intent, MakerActivity.REQ_CODE_GALLERY);
	}
	
	public void addNewItemToMaker(Uri uri) {
		try {
			Bitmap bitmap = Images.Media.getBitmap(mContext.getContentResolver(), uri);
			if (bitmap != null) {
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.layout_item, null);
				ImageView image = new ImageView(mContext);
				image.setImageBitmap(bitmap);
				layout.addView(image);
				layout.setOnTouchListener(getImageTouchListener());
				mlayoutItmes.addView(layout);
			}
			
		} catch (FileNotFoundException e) {
			Toast.makeText(mContext, "Cannot find file : " + uri.getPath(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(mContext, "Occured an error to load image", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	private View.OnTouchListener mUpdownMovableTouchListener = new View.OnTouchListener() {
		public int savedY = 0;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			int y = (int) event.getRawY();
			
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				savedY = y;
				v.setPressed(true);
				return true;
			case MotionEvent.ACTION_MOVE:
				int[] origin = new int[2];
				v.getLocationInWindow(origin);
				int width = v.getWidth();
				int height = v.getHeight();
				v.layout(origin[0], y-height/2, origin[0]+width, y+height/2);
				return true;
			case MotionEvent.ACTION_UP:
				//TODO needed to change using timer.
				int  viewHeight = v.getHeight();
				if (Math.abs(savedY - y) < viewHeight/2) {
					v.setPressed(false);
					v.performClick();
				}
				return true;
			}
			return true;
		}
	};
	
	private View.OnTouchListener mMoveAndExpendableTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			int x = (int) event.getRawX();
			int y = (int) event.getRawY();
			
			//TODO needed to implement 2 point touch that can resize images.
			
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				int[] origin = new int[2];
				v.getLocationInWindow(origin);
				int width = v.getWidth();
				int height = v.getHeight();
				v.layout(x-width/2, y-height/2, x+width/2, y+height/2);
				return true;
			}
			return true;
		}
	};
	
	private View.OnClickListener mAddbtnCilckListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			getImageFromGallery();
		}
	};
	
	private View.OnClickListener mSetbtnCilckListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mlayoutItmes.setDrawingCacheEnabled(true);
			Bitmap wallpaper = mlayoutItmes.getDrawingCache();
			//TODO with setting image to wallpaper, save image to file for user set it to lockscreen image.
			if (wallpaper != null) {
				try {
					//TODO try to using wallpapermanager
					mContext.setWallpaper(wallpaper);
					mContext.finish();
				} catch (IOException e) {
					Toast.makeText(mContext, "Occured an error to set image as wallpaper", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
			
		}
	};
	
	public View.OnTouchListener getButtonTouchListener() {
		return mUpdownMovableTouchListener;
	}
	
	public View.OnTouchListener getImageTouchListener() {
		return mMoveAndExpendableTouchListener;
	}
	
	public View.OnClickListener getAddbtnClickListener() {
		return mAddbtnCilckListener;
	}
	
	public View.OnClickListener getSetbtnClickListener() {
		return mSetbtnCilckListener;
	}
}
