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
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ListAdapter;
import android.widget.ListView;

import net.zorgblub.typhon.R;

public class PinchableListView extends ListView {

	public static final int MAX_SIZE = 80;
	public static final int MIN_SIZE = 20;
	public static final int DEFAULT_SIZE = 40;

	private ScaleGestureDetector mScaleDetector;

	private OnPinchListener mOnPinchListener;

	private int mTextSize = DEFAULT_SIZE;

	private int mTextColor;
	private int mBackgroundColor;
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


		// prevent the ListView from changing its background colour when scrolling
		this.setCacheColorHint(Color.TRANSPARENT);
		this.setOnPinchListener(new PinchableListView.OnPinchListener() {
			@Override
			public boolean onPinch(float scale) {
				if(!PinchableListView.this.scaleTextSize(scale))
					return false;

				PinchableListView.this.invalidateViews();
				return true;
			}
		});
		mTextColor = getResources().getColor(R.color.default_def_text_color);

		/* background color of this view */
		mBackgroundColor = Color.BLACK;
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

	/**
	 * set the data behind the listview of this compound control
	 *
	 * @param adapter the ListAdapter which is responsible for maintaining the data backing this list
	 *                and for producing a view to represent an item in that data set.
	 */
	public void setAdapter(ListAdapter adapter) {
		if (adapter instanceof AdvancedArrayAdapter) {
			AdvancedArrayAdapter advancedArrayAdapter = (AdvancedArrayAdapter) adapter;
			advancedArrayAdapter.setTextPixelSize(getTextSize());
			advancedArrayAdapter.setColor(mTextColor);
		}
		super.setAdapter(adapter);
	}

	public void setTextSize(int size) {
		mTextSize = size;
		if (this.getAdapter() instanceof AdvancedArrayAdapter) {
			((AdvancedArrayAdapter) this.getAdapter()).setTextPixelSize(size);
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

	public void setTextColor(int color) {
		mTextColor = color;

		if (this.getAdapter() instanceof AdvancedArrayAdapter) {
			((AdvancedArrayAdapter) this.getAdapter()).setColor(color);
		}
	}

	public void setDefintionBackgroundColor(int color) {
		mBackgroundColor = color;
		this.setBackgroundColor(color);
	}

	/**
	 * return the adapter behind the listview of this compound control
	 *
	 * @return the adapter behind the listview of this compound control
	 */
	@SuppressWarnings("unused")
	public ListAdapter getAdapter() {
		return super.getAdapter();
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
