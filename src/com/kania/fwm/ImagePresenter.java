package com.kania.fwm;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent.PointerCoords;
import android.view.ViewGroupOverlay;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ImagePresenter {
	private final int ALIGN_COLUMN_COUNT = 40; //ROW is using COLUMN's size.
	private final int MINIMUM_SIZE_OF_IMAGE_RATE = 10; //ROW is using COLUMN's size.
	private final int TOUCH_MODE_NONE = 0;
	private final int TOUCH_MODE_DRAG = 1;
	private final int TOUCH_MODE_ZOOM = 2;
	
	private Activity mContext;
	private ViewGroup mlayoutItmes;
	
	private boolean mIsAutoAlignMode = true;
	
	
	public ImagePresenter(Activity context, ViewGroup vg) {
		mContext = context;
		mlayoutItmes = vg;
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
				ImageView image = new ImageView(mContext);
				image.setImageBitmap(bitmap);
				image.setScaleType(ScaleType.CENTER_CROP);
				image.setOnTouchListener(getImageTouchListener());
				mlayoutItmes.addView(image);
			}
			
		} catch (FileNotFoundException e) {
			Toast.makeText(mContext, "Cannot find file : " + uri.getPath(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(mContext, "Occured an error to load image", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	public void setIsAutoAlignMode(boolean enable) {
		mIsAutoAlignMode = enable;
	}
	public boolean getIsAutoAlignMode() {
		return mIsAutoAlignMode;
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
				setButtonLocation(v, origin[0], y-height/2, origin[0]+width, y+height/2);
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
		int dis2L = 0, dis2T = 0, dis2R = 0, dis2B = 0; //distance to left ... 
		//used to two pointer action
		int disWidthOri = 0, disHeightOri = 0; // original distance 
		int touchMode = TOUCH_MODE_NONE;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getActionMasked();
			int x = (int) event.getRawX();
			int y = (int) event.getRawY();
			int[] origin = new int[2];
			v.getLocationInWindow(origin);
			int width = v.getWidth();
			int height = v.getHeight();
			
			//TODO needed to synchronization condition (really is it needs?).
			
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				dis2L = x - origin[0];
				dis2T = y - origin[1];
				dis2R = origin[0] + width - x;
				dis2B = origin[1] + height - y;
				touchMode = TOUCH_MODE_DRAG;
				return true;
			case MotionEvent.ACTION_MOVE:
				if (touchMode == TOUCH_MODE_DRAG) {
					if (mIsAutoAlignMode) {
						int alignInterval = mlayoutItmes.getWidth() / ALIGN_COLUMN_COUNT;
						int leftSub = (x - dis2L) % alignInterval;
						int topSub = (y - dis2T) % alignInterval;
						setImageLocation(v, x-dis2L - leftSub, y-dis2T - topSub, x+dis2R - leftSub, y+dis2B - topSub);
					} else {
						setImageLocation(v, x-dis2L, y-dis2T, x+dis2R, y+dis2B);
					}
					return true;
				} else if (touchMode == TOUCH_MODE_ZOOM) {
					int disWidthNew = getDistance((int)event.getX(0), (int)event.getX(1));
					int disHeightNew = getDistance((int)event.getY(0), (int)event.getY(1));
					int disGapWidth = disWidthNew - disWidthOri;
					int disGapHeight = disHeightNew - disHeightOri; 
					if (Math.abs(disGapWidth) > Math.abs(disGapHeight)) {
						//width case
						float ratio = (float) height / (float) width;
						setImageLocation(v,
								origin[0],
								origin[1],
								origin[0] + width + disGapWidth,
								origin[1] + Math.round((width + disGapWidth)*ratio)
								);
					} else {
						//height case
						float ratio = (float) width / (float) height;
						v.layout(
								origin[0],
								origin[1],
								origin[0] + Math.round((height + disGapHeight)*ratio),
								origin[1] + height + disGapHeight
								);
					}
					disWidthOri = disWidthNew;
					disHeightOri = disHeightNew;
				}
				return true;
			case MotionEvent.ACTION_POINTER_DOWN:
				disWidthOri = getDistance((int)event.getX(0), (int)event.getX(1));
				disHeightOri = getDistance((int)event.getY(0), (int)event.getY(1)); 
				touchMode = TOUCH_MODE_ZOOM;
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				touchMode = TOUCH_MODE_NONE;
				return true;
				
			}
			return false;
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

	public void setButtonLocation(View v, int left, int top, int right, int bottom) {
		v.layout(left, top, right, bottom);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
		params.topMargin = top;
		params.bottomMargin = - bottom;
		//when first moving button, remove gravity option. it is for initial point of buttons
		params.addRule(RelativeLayout.CENTER_VERTICAL, 0);
		v.setLayoutParams(params);
	}
	
	public void setImageLocation(View v, int left, int top, int right, int bottom) {
		//if image size is smaller than 1/100 of window size, cancel.
		if ((right - left) < (mlayoutItmes.getWidth() / MINIMUM_SIZE_OF_IMAGE_RATE) ||
				(bottom - top) < (mlayoutItmes.getWidth() / MINIMUM_SIZE_OF_IMAGE_RATE))
			return;
		v.layout(left, top, right, bottom);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
		params.leftMargin = left;
		params.topMargin = top;
		params.rightMargin = - right;
		params.bottomMargin = - bottom;
		params.width = v.getWidth();
		params.height = v.getHeight();
		v.setLayoutParams(params);
	}
	
	public int getDistance(int p1, int p2) {
		return Math.abs(p2 - p1);
	}
}
