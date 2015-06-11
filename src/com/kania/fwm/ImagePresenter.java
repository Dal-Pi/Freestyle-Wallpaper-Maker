package com.kania.fwm;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class ImagePresenter {
	
	private Context mContext;
	private ViewGroup mRootView;
	
	public ImagePresenter(Context context, ViewGroup vg) {
		mContext = context;
		mRootView = vg;
	}
	
	private View.OnTouchListener mUpdownMovableTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			int y = (int) event.getRawY();
			
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				int[] origin = new int[2];
				v.getLocationInWindow(origin);
				int width = v.getWidth();
				int height = v.getHeight();
				v.layout(origin[0], y-height/2, origin[0]+width, y+height/2);
				return true;
			}
			return true;
		}
	};
	
	private View.OnTouchListener mMoveAndExpendableTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			
			return true;
		}
	};
	
	public View.OnTouchListener getButtonListener() {
		return mUpdownMovableTouchListener;
	}
	
	public View.OnTouchListener getImageListener() {
		return mMoveAndExpendableTouchListener;
	}
}
