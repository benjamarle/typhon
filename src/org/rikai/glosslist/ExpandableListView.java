/*
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

Author: Ray Zhou
Date: 2013 04 26

*/
package org.rikai.glosslist;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;


import net.rikaiwhistler.pageturner.R;

public class ExpandableListView extends RelativeLayout implements Concealable, View.OnTouchListener {

    @SuppressWarnings("unused")
    private static final String TAG = "JTEXT";
    public static final int MAX_SIZE = 80;
    public static final int MIN_SIZE = 20;
    public static final int DEFAULT_SIZE = 40;

    /**
     * the Views that made up of this ExpandableListView
     */
    private PinchableListView mContentView;

    private Button mCloseButton;
    private Button mDragBar;

    private boolean mShowBar;
    private int mTextSize = DEFAULT_SIZE;

    private int mTextColor;
    private int mBackgroundColor;

    private float mDragBarOriginalY;

    private SizeChangeListener mSizeChangeListener;

    /**
     * true if the user is dragging the mDragBar
     */
    private boolean mResizing = false;

    private OnConcealListener mOnConcealListener = null;
    private OnRevealListener mOnRevealListener = null;

    @SuppressWarnings("unused")
    public ExpandableListView(Context context) {
        super(context);
        init(context, null);
    }

    @SuppressWarnings("unused")
    public ExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @SuppressWarnings("unused")
    public ExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.expandable_listview, this);

        mContentView = (PinchableListView) view.findViewById(R.id.gloss);

        // prevent the ListView from changing its background colour when scrolling
        mContentView.setCacheColorHint(Color.TRANSPARENT);
        mContentView.setOnPinchListener(new PinchableListView.OnPinchListener() {
            @Override
            public boolean onPinch(float scale) {
                if(!scaleTextSize(scale))
                    return false;

                mContentView.invalidateViews();
                return true;
            }
        });

        mCloseButton = (Button) view.findViewById(R.id.gloss_close);
        mDragBar = (Button) view.findViewById(R.id.gloss_drag);


        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableListView);

        ViewGroup.LayoutParams p;

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, r.getDisplayMetrics());

		/* the height of the top bar */
        int bar_height = a.getDimensionPixelSize(R.styleable.ExpandableListView_bar_height, 0);
        if (bar_height != 0) {
            // set mCloseButton height
            p = mCloseButton.getLayoutParams();
            p.height = bar_height;
            mCloseButton.setLayoutParams(p);

            // set mDragBar height
            p = mDragBar.getLayoutParams();
            p.height = bar_height;
            mDragBar.setLayoutParams(p);

            // set top padding of the ListView to contain the mDragBar and mCloseButton
            mContentView.setPadding(0, bar_height, 0, 0);
        }

		/* width of the close button */
        int close_button_width =
                a.getDimensionPixelSize(R.styleable.ExpandableListView_close_button_width, 0);
        if (close_button_width != 0) {
            p = mCloseButton.getLayoutParams();
            p.width = close_button_width;
            mCloseButton.setLayoutParams(p);
        }

        mTextColor = getResources().getColor(R.color.default_def_text_color);

		/* background color of this view */
        mBackgroundColor =
                a.getColor(R.styleable.ExpandableListView_background_color, getResources().getColor(R.color.default_def_bg_color));
        mContentView.setBackgroundColor(mBackgroundColor);

		/* the initial height of the view, -1 if not set */
        int initial_height = a.getDimensionPixelSize(R.styleable.ExpandableListView_initial_height, -1);
        if (initial_height != -1) {
            setHeight(initial_height);
        }

        mShowBar = a.getBoolean(R.styleable.ExpandableListView_show_bar, true);
        if (mShowBar) {
            mCloseButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    conceal();
                }
            });
            mDragBar.setOnTouchListener(this); // drag to resize this compound control
        } else {
            mCloseButton.setText("");
        }

        a.recycle();
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
        mContentView.setAdapter(adapter);
    }

    /**
     * return the adapter behind the listview of this compound control
     *
     * @return the adapter behind the listview of this compound control
     */
    @SuppressWarnings("unused")
    public ListAdapter getAdapter() {
        return mContentView.getAdapter();
    }


    public void setTextSize(int size) {
        mTextSize = size;
        if (mContentView.getAdapter() instanceof AdvancedArrayAdapter) {
            ((AdvancedArrayAdapter) mContentView.getAdapter()).setTextPixelSize(size);
        }
        fireSizeChangeEvent(size);
    }

    public int getTextSize() {
        return mTextSize;
    }

    public boolean scaleTextSize(float scale) {
        int oldSize = getTextSize();
        int newSize = (int) Math.round(oldSize * scale);
        newSize = Math.min(newSize, MAX_SIZE);
        newSize = Math.max(MIN_SIZE, newSize);

        if (newSize == oldSize)
            return false;
        this.setTextSize(newSize);
        return true;
    }

    public void setTextColor(int color) {
        mTextColor = color;

        if (mContentView.getAdapter() instanceof AdvancedArrayAdapter) {
            ((AdvancedArrayAdapter) mContentView.getAdapter()).setColor(color);
        }
    }

    public void setDefintionBackgroundColor(int color) {
        mBackgroundColor = color;
        mContentView.setBackgroundColor(color);
    }

    /**
     * set the height of this compound control
     *
     * @param height the height
     */
    public void setHeight(int height) {
        ViewGroup.LayoutParams layout_params = mContentView.getLayoutParams();
        layout_params.height = height;
        mContentView.setLayoutParams(layout_params);
    }

    public void invertItemColor(int position) {
    }

    /**
     * set the visibility of every view in this ExpandableListView
     *
     * @param visibility vsibility can be VISIBLE, INVISIBLE or NONE
     */
    private void internalSetVisibility(int visibility) {
        if (visibility == VISIBLE && mOnRevealListener != null) {
            mOnRevealListener.onReveal(this);
        } else if ((visibility == INVISIBLE || visibility == GONE) && mOnConcealListener != null) {
            mOnConcealListener.onConceal(this);
        }
        this.setVisibility(visibility);
        mContentView.setVisibility(visibility);
        mCloseButton.setVisibility(visibility);
        mDragBar.setVisibility(visibility);
    }

    /**
     * show this View
     */
    @Override
    public void reveal() {
        internalSetVisibility(View.VISIBLE);
    }

    /**
     * Hide this view
     */
    @Override
    public void conceal() {
        internalSetVisibility(View.INVISIBLE);
    }

    /**
     * returns whether this is visible
     *
     * @return true if the widget is visible, false otherwise
     */
    @Override
    public boolean isDisplaying() {
        return mContentView.getVisibility() == View.VISIBLE;
    }

    public void setOnConcealListener(OnConcealListener onConcealListener) {
        mOnConcealListener = onConcealListener;
    }

    @SuppressWarnings("unused")
    public void setOnRevealListener(OnRevealListener onRevealListener) {
        mOnRevealListener = onRevealListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mContentView.setOnItemClickListener(listener);
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        mContentView.setOnItemLongClickListener(listener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == mDragBar) {
            int dark = 0xB7000000;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // make the bar darker
                    mDragBarOriginalY = event.getY();
                    mDragBar.setBackgroundColor(dark);
                    mCloseButton.setBackgroundColor(dark);
                    mResizing = true;
                    break;
                case MotionEvent.ACTION_UP:
                    // transparent
                    mDragBar.setBackgroundColor(0);
                    mCloseButton.setBackgroundColor(0);
                    mResizing = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mResizing) {
                        ViewGroup.LayoutParams layout_params = mContentView.getLayoutParams();
                        layout_params.height = (int) (layout_params.height - (event.getY() - mDragBarOriginalY));
                        mContentView.setLayoutParams(layout_params);
                        mContentView.invalidate();
                        return true;
                    }
                    break;
            }
            return true;
        }
        return false;
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
