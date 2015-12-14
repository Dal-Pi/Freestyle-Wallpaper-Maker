package com.kania.fwm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ImagePresenter {
	private final int ALIGN_COLUMN_COUNT = 40; //ROW is using COLUMN's size.
	private final int CLICK_RECONIZE_CRITICAL_POINT = 20; //ROW is using COLUMN's size.
	private final int MINIMUM_SIZE_OF_IMAGE_RATE = 10; //ROW is using COLUMN's size.
	private final int TOUCH_MODE_NONE = 0;
	private final int TOUCH_MODE_DRAG = 1;
	private final int TOUCH_MODE_ZOOM = 2;
	public static final String PATH_PROJECT_NAME = "FWM";
	
	private final int CONTEXT_MENU_INDEX_CANCEL = 0;
	private final int CONTEXT_MENU_INDEX_REMOVE = 1;
	private final int CONTEXT_MENU_INDEX_TOP = 2;
//	private final int CONTEXT_MENU_INDEX_END = 3;
	
	private Activity mContext;
	private ViewGroup mlayoutItmes;
	
	private ImageView mSelectedView;
	
	private boolean mIsAutoAlignMode = true;
	
	private View mNowTouchView = null;
	
	
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
		//case 1. occurred Out Of Memory exception and cannot load image to imageview
		//			Bitmap bitmap = Images.Media.getBitmap(mContext.getContentResolver(), uri);
		
		//case 2. 
		Bitmap bitmap = getResizedBitmapIfNeed(uri);
		ImageView image = new ImageView(mContext);
		image.setScaleType(ScaleType.CENTER_CROP);
		image.setImageBitmap(bitmap);
		image.setOnTouchListener(getImageTouchListener());
		mContext.registerForContextMenu(image);
		mlayoutItmes.addView(image);
	}
	
	public Bitmap getResizedBitmapIfNeed(Uri uri) {
		Bitmap resizedBitmap = null;
		try {
			Bitmap bitmap = Images.Media.getBitmap(mContext.getContentResolver(), uri);
			int bitmapWidth = bitmap.getWidth();
			int bitmapHeight = bitmap.getHeight();
			DisplayMetrics metrics = new DisplayMetrics();
			Display display = mContext.getWindowManager().getDefaultDisplay();
//			Method methodGetRawW = null, methodGetRawH = null; //get real size of phone - for JB (API17) and onward
			display.getRealMetrics(metrics);
//			int layoutWidth = mlayoutItmes.getWidth();
//			int layoutHeight = mlayoutItmes.getHeight();
			int layoutWidth = metrics.widthPixels;
			int layoutHeight = metrics.heightPixels;
			
			if (layoutWidth >= bitmapWidth && layoutHeight >= bitmapHeight) {
				return bitmap;
			} else {
				float bitmapRatio = (float) bitmapWidth / (float) bitmapHeight;
				Log.d("FWM", "bitmapWidth = " + " / bitmapScale = " + bitmapWidth + ", " + bitmapHeight);
				float layoutRatio = (float) layoutWidth / (float) layoutHeight;
				Log.d("FWM", "layoutRatio = " + " / layoutScale = " + layoutWidth + ", " + layoutHeight);
				if (layoutRatio < bitmapRatio) {
					Log.d("FWM", "scaled case  layoutRatio < bitmapRatio / scale = " + layoutWidth + ", " + (int) (layoutWidth / bitmapRatio));
					resizedBitmap = Bitmap.createScaledBitmap(bitmap, layoutWidth, (int) (layoutWidth / bitmapRatio), true);
				} else if (layoutRatio == bitmapRatio) {
					Log.d("FWM", "scaled case  layoutRatio == bitmapRatio / scale = " + layoutWidth + ", " + layoutHeight);
					resizedBitmap = Bitmap.createScaledBitmap(bitmap, layoutWidth, layoutHeight, true);
				} else {
					Log.d("FWM", "scaled case  layoutRatio > bitmapRatio / scale = " + (int) (layoutHeight * bitmapRatio) + ", " + layoutHeight);
					resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int) (layoutHeight * bitmapRatio), layoutHeight, true);
				}
			}
		} catch (FileNotFoundException e) {
			Toast.makeText(mContext, "Cannot find file : " + uri.getPath(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(mContext, "Occured an error to load image", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		return resizedBitmap;
	}
	
	public void requestMakeContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(0, CONTEXT_MENU_INDEX_REMOVE, 0, "Delete");
		menu.add(0, CONTEXT_MENU_INDEX_TOP, 0, "Send to Top (Z-order)");
//		menu.add(0, CONTEXT_MENU_INDEX_END, 0, "Send to End (z-layer)");
		menu.add(0, CONTEXT_MENU_INDEX_CANCEL, 0, "Cancel");
		mSelectedView = (ImageView) v;
	}
	
	public void requestHandleMenuSelect(int menuId) {
		switch (menuId) {
		case CONTEXT_MENU_INDEX_REMOVE:
			if (mSelectedView != null) {
				mlayoutItmes.removeView(mSelectedView);
				mSelectedView = null;
			}
			break;
		case CONTEXT_MENU_INDEX_TOP:
			if (mSelectedView != null)
				mlayoutItmes.bringChildToFront(mSelectedView);
			break;
		case CONTEXT_MENU_INDEX_CANCEL:
			break;
		}
		hideStatusAndNavibar();
	}
	
	public void setIsAutoAlignMode(boolean enable) {
		mIsAutoAlignMode = enable;
	}
	public boolean getIsAutoAlignMode() {
		return mIsAutoAlignMode;
	}
	
	public void hideStatusAndNavibar() {
		View decorView = mContext.getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
				int  viewHeight = v.getHeight();
				if (Math.abs(savedY - y) < CLICK_RECONIZE_CRITICAL_POINT) {
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
		int savedX = 0, savedY = 0;
		//used to two pointer action
		int disWidthOri = 0, disHeightOri = 0; // original distance 
		int touchMode = TOUCH_MODE_NONE;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//condition for synchronization.
			if (mNowTouchView != null && mNowTouchView != v)
				return true;
			int action = event.getActionMasked();
			int x = (int) event.getRawX();
			int y = (int) event.getRawY();
			int[] origin = new int[2];
			v.getLocationInWindow(origin);
			int width = v.getWidth();
			int height = v.getHeight();
			
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				dis2L = x - origin[0];
				dis2T = y - origin[1];
				dis2R = origin[0] + width - x;
				dis2B = origin[1] + height - y;
				savedX = x;
				savedY = y;
				touchMode = TOUCH_MODE_DRAG;
				mNowTouchView = v;
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
				if (touchMode == TOUCH_MODE_DRAG &&
				(Math.abs(savedX - x) < CLICK_RECONIZE_CRITICAL_POINT) && 
				(Math.abs(savedY - y) < CLICK_RECONIZE_CRITICAL_POINT)) {
					v.performLongClick();
				}
				touchMode = TOUCH_MODE_NONE;
				mNowTouchView = null;
				return true;
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
			if (wallpaper != null) {
				try {
					//TODO try to using wallpapermanager
					mContext.setWallpaper(wallpaper);
					saveWallpaperToFile(wallpaper);
					mContext.finish();
				} catch (IOException e) {
					Toast.makeText(mContext, "Occured an error to set image as wallpaper", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
			
		}
	};
	
	private View.OnLongClickListener mBackgroundClickListener = new View.OnLongClickListener() {
		public boolean onLongClick(View v) {
			//TODO (needs to be improved) make dialog for color pick
			new AlertDialog.Builder(mContext)
			.setTitle("Background Color")
			.setItems(R.array.background_colors, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String[] color = mContext.getResources().getStringArray(R.array.background_colors);
					if ("White".equalsIgnoreCase(color[which])) {
						mlayoutItmes.setBackgroundColor(Color.WHITE); mlayoutItmes.invalidate();
						Toast.makeText(mContext, "White", Toast.LENGTH_SHORT).show();
					} else if ("Black".equalsIgnoreCase(color[which])) {
						mlayoutItmes.setBackgroundColor(Color.BLACK); mlayoutItmes.invalidate();
						Toast.makeText(mContext, "Black", Toast.LENGTH_SHORT).show();
					}
					hideStatusAndNavibar();
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					hideStatusAndNavibar();
				}
			})
			.show();
			return false;
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
	
	public View.OnLongClickListener getBackgroundClickListener() {
		return mBackgroundClickListener;
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
	
	public void saveWallpaperToFile(Bitmap wallpaper) {
		String projectPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File dir = new File(projectPath + "/" + PATH_PROJECT_NAME);
		if (!dir.exists()) {
			dir.mkdir();
		}
		Calendar calendar = Calendar.getInstance();
		String filename = "fwm_" + calendar.getTimeInMillis() + ".png";
		try {
			File filepath = new File(projectPath + "/" + PATH_PROJECT_NAME + "/" +filename);
			FileOutputStream fos = new FileOutputStream(filepath);
			wallpaper.compress(Bitmap.CompressFormat.PNG, 100, fos);
			mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(filepath)));
			Toast.makeText(mContext, "save to file : " + projectPath + "/" + PATH_PROJECT_NAME + "/" +filename, Toast.LENGTH_LONG).show();
		} catch (FileNotFoundException e) {
			Toast.makeText(mContext, "error occured on save wallpaper to file!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
}
