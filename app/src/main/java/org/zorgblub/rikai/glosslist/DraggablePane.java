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
package org.zorgblub.rikai.glosslist;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import net.zorgblub.typhonkai.R;

public class DraggablePane extends RelativeLayout implements View.OnTouchListener {

    @SuppressWarnings("unused")
    private static final String TAG = "JTEXT";


    /**
     * the Views that made up of this ExpandableListView
     */
    private View mContentView;

    private Button mCloseButton;
    private Button mDragBar;

    private boolean mShowBar;

    private float mDragBarOriginalY;

    /**
     * true if the user is dragging the mDragBar
     */
    private boolean mResizing = false;

    @SuppressWarnings("unused")
    public DraggablePane(Context context) {
        super(context);
        init(context, null);
    }

    @SuppressWarnings("unused")
    public DraggablePane(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @SuppressWarnings("unused")
    public DraggablePane(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.expandable_listview, this);
        mContentView = view.findViewById(R.id.viewpager);

        mCloseButton = (Button) view.findViewById(R.id.gloss_close);
        mDragBar = (Button) view.findViewById(R.id.gloss_drag);


        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DraggablePane);

        ViewGroup.LayoutParams p;

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, r.getDisplayMetrics());

		/* the height of the top bar */
        int bar_height = a.getDimensionPixelSize(R.styleable.DraggablePane_bar_height, 0);
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
                a.getDimensionPixelSize(R.styleable.DraggablePane_close_button_width, 0);
        if (close_button_width != 0) {
            p = mCloseButton.getLayoutParams();
            p.width = close_button_width;
            mCloseButton.setLayoutParams(p);
        }



		/* the initial height of the view, -1 if not set */
        int initial_height = a.getDimensionPixelSize(R.styleable.DraggablePane_initial_height, -1);
        if (initial_height != -1) {
            setHeight(initial_height);
        }

        mShowBar = a.getBoolean(R.styleable.DraggablePane_show_bar, true);
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
        this.setVisibility(visibility);
        mContentView.setVisibility(visibility);
        mCloseButton.setVisibility(visibility);
        mDragBar.setVisibility(visibility);
    }

    /**
     * show this View
     */
    public void reveal() {
        internalSetVisibility(View.VISIBLE);
    }

    /**
     * Hide this view
     */
    public void conceal() {
        internalSetVisibility(View.INVISIBLE);
    }

    /**
     * returns whether this is visible
     *
     * @return true if the widget is visible, false otherwise
     */
    public boolean isDisplaying() {
        return mContentView.getVisibility() == View.VISIBLE;
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

}
