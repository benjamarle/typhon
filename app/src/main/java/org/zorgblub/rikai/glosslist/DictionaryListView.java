package org.zorgblub.rikai.glosslist;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ListAdapter;

import net.zorgblub.typhon.Configuration;
import net.zorgblub.typhon.R;

import org.rikai.dictionary.Entries;
import org.zorgblub.rikai.DroidEntry;

/**
 * Created by Benjamin on 22/03/2016.
 */
public class DictionaryListView extends PinchableListView {

    private int mTextColor;

    private int mBackgroundColor;

    private int index;

    private Configuration config;


    public DictionaryListView(Context context) {
        super(context);
        init();
    }

    public DictionaryListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DictionaryListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init(){
        // prevent the ListView from changing its background colour when scrolling
        this.config = new Configuration(this.getContext());
        this.setCacheColorHint(Color.TRANSPARENT);
        this.setOnPinchListener(new OnPinchListener() {
            @Override
            public boolean onPinch(float scale) {
                if(!DictionaryListView.this.scaleTextSize(scale))
                    return false;

                DictionaryListView.this.invalidateViews();
                return true;
            }
        });
        mTextColor = getResources().getColor(R.color.default_def_text_color);
		/* background color of this view */
        mBackgroundColor = Color.BLACK;

        setDivider(new ColorDrawable(Color.parseColor("#C7C0C0C0")));
        this.setTextColor(Color.WHITE);
        this.setBackgroundColor(Color.parseColor("#C7000000"));
        this.setPadding(0, 0, 0, getPixels(20));
        setDividerHeight(getPixels(3));
    }

    private int getPixels(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
    }
    /**
     * set the data behind the listview of this compound control
     *
     * @param adapter the ListAdapter which is responsible for maintaining the data backing this list
     *                and for producing a view to represent an item in that data set.
     */
    public void setAdapter(ListAdapter adapter) {
        if (adapter instanceof DictionaryEntryAdapter) {
            DictionaryEntryAdapter dictionaryEntryAdapter = (DictionaryEntryAdapter) adapter;
            dictionaryEntryAdapter.setTextPixelSize(getTextSize());
            dictionaryEntryAdapter.setColor(mTextColor);
            dictionaryEntryAdapter.setTypeface(config.getDefaultFontFamily().getDefaultTypeface());
        }
        super.setAdapter(adapter);
    }

    public void setResults(Entries entries){
        DictionaryEntryAdapter<DroidEntry> adapter
                = new DictionaryEntryAdapter<>(this.getContext(), R.layout.definition_row, entries);
        this.setAdapter(adapter);
    }

    public void setDefintionBackgroundColor(int color) {
        mBackgroundColor = color;
        this.setBackgroundColor(color);
    }

    public void setTextColor(int color) {
        mTextColor = color;

        if (this.getAdapter() instanceof DictionaryEntryAdapter) {
            ((DictionaryEntryAdapter) this.getAdapter()).setColor(color);
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}