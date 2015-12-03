package org.rikai.glosslist;/*
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
import android.widget.ListView;import java.lang.Override;import java.lang.SuppressWarnings;

public class PinchableListView extends ListView {

	private ScaleGestureDetector mScaleDetector;

	private OnPinchListener mOnPinchListener;

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
				mOnPinchListener.onPinch(detector.getScaleFactor());
			}
			return true;
		}
	}

	public interface OnPinchListener {

		public void onPinch(float scale);
	}
}
