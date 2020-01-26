package org.zorgblub.rikai.glosslist;/*
Copyright (C) 2013 Ray Zhou

JadeRead is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

JadeRead is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with JadeRead.  If not, see <http://www.gnu.org/licenses/>

Author: ray
Date: 2013-06-09

*/

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ListView;

public class PinchableListView extends ListView {

	public static final int MAX_SIZE = 80;
	public static final int MIN_SIZE = 20;
	public static final int DEFAULT_SIZE = 40;

	private ScaleGestureDetector mScaleDetector;

	private OnPinchListener mOnPinchListener;
	private int mTextSize = DEFAULT_SIZE;

	private SizeChangeListener mSizeChangeListener;

	@SuppressWarnings("unused")
	public PinchableListView(Context context) {
		super(context);
		init();
	}

	@SuppressWarnings("UnusedDeclaration")
	public PinchableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@SuppressWarnings("UnusedDeclaration")
	public PinchableListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mScaleDetector = new ScaleGestureDetector(this.getContext(), new ScaleListener());
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mScaleDetector.onTouchEvent(ev);
		return super.onTouchEvent(ev);
	}

	public void setOnPinchListener(OnPinchListener onPinchListener) {
		mOnPinchListener = onPinchListener;
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if (mOnPinchListener != null) {
				return mOnPinchListener.onPinch(detector.getScaleFactor());
			}
			return true;
		}
	}

	public interface OnPinchListener {

		boolean onPinch(float scale);
	}



	public void setTextSize(int size) {
		mTextSize = size;
		if (this.getAdapter() instanceof DictionaryEntryAdapter) {
			((DictionaryEntryAdapter) this.getAdapter()).setTextPixelSize(size);
		}
		fireSizeChangeEvent(size);
	}

	public int getTextSize() {
		return mTextSize;
	}

	public boolean scaleTextSize(float scale) {
		int oldSize = getTextSize();
		int newSize = Math.round(oldSize * scale);
		newSize = Math.min(newSize, MAX_SIZE);
		newSize = Math.max(MIN_SIZE, newSize);

		if (newSize == oldSize)
			return false;
		this.setTextSize(newSize);
		return true;
	}



	public void setSizeChangeListener(SizeChangeListener sizeChangeListener) {
		this.mSizeChangeListener = sizeChangeListener;
	}

	public void fireSizeChangeEvent(int newSize){
		if(mSizeChangeListener != null){
			mSizeChangeListener.onSizeChange(newSize);
		}
	}



}
