/*
 * Copyright (C) 2011 Alex Kuiper
 * 
 * This file is part of PageTurner
 *
 * PageTurner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PageTurner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PageTurner.  If not, see <http://www.gnu.org/licenses/>.*
 */
package net.zorgblub.typhon.view.bookview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import net.zorgblub.typhon.dto.HighLight;
import net.zorgblub.typhon.epub.TyphonSpine;
import net.zorgblub.typhon.view.HighlightManager;

import java.util.List;

import javax.inject.Inject;

import jedi.option.Option;

import static jedi.option.Options.none;
import static jedi.option.Options.option;


public class ScrollingStrategy implements PageChangeStrategy {


    @Inject
    HighlightManager highlightManager;

	private Context context;

	private BookView bookView;
	
	private TextView childView;
	private int storedPosition = -1;
	private double storedPercentage = -1;
	
	private Spannable text;

	@Inject
	public ScrollingStrategy(Context context) {
		this.context = context;
	}

	@Override
    public void setBookView(BookView bookView) {
		this.bookView = bookView;
		this.childView = bookView.getInnerView();
	}
	
	@Override
	public Option<CharSequence> getNextPageText() { return none(); }
	
	@Override
	public Option<CharSequence> getPreviousPageText() { return none(); }
	
	@Override
	public int getTopLeftPosition() {
		if ( childView.getText().length() == 0 ) {
			return storedPosition;
		} else {
			int yPos = bookView.getScrollY();
		
			return findTextOffset(findClosestLineBottom(yPos));
		}
	}

    public int getProgressPosition() {
        return getTopLeftPosition();
    }
	
	@Override
	public boolean isAtEnd() {
		int ypos = bookView.getScrollY() + bookView.getHeight();
		
		Layout layout = this.childView.getLayout();
		if ( layout == null ) {
			return false;
		}
		
		int line = layout.getLineForVertical(ypos);
		return line == layout.getLineCount() -1;
	}
	
	@Override
	public boolean isAtStart() {
		return getTopLeftPosition() == 0;
	}
	
	@Override
	public void loadText(Spanned newText) {
        SpannableStringBuilder builder = new SpannableStringBuilder(newText);
		this.text = addEndTag(builder);
        addHighlights(builder);
	}

    @Override
    public void updateGUI() {
        addHighlights( this.text);

        try {
            childView.setText(this.text);
        } catch ( IndexOutOfBoundsException i ) {
            this.childView.setText( this.text.toString() );
        }

        updatePosition();
    }

	private Spannable addEndTag(SpannableStringBuilder builder) {
		
		//Don't add the tag to the last section.
		TyphonSpine spine = bookView.getSpine();
		
		if (spine == null || spine.getPosition() >= spine.size() -1 ) {
			return builder;
		}
		
		int length = builder.length();
		builder.append("\uFFFC");
		builder.append("\n");
		builder.append( context.getString(net.zorgblub.typhon.R.string.end_of_section));
		//If not, consider it an internal nav link.			
		ClickableSpan span = new ClickableSpan() {
				
			@Override
			public void onClick(View widget) {
				pageDown();					
			}
		};

		Drawable img = context.getResources().getDrawable(net.zorgblub.typhon.R.drawable.gateway);
		img.setBounds(0, 0, img.getIntrinsicWidth(), img.getIntrinsicHeight() );
		builder.setSpan(new ImageSpan(img), length, length+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		builder.setSpan(span, length, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setSpan( (AlignmentSpan) () -> Alignment.ALIGN_CENTER
			, length, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return builder;		
	}

    private void addHighlights( Spannable builder ) {
        List<HighLight> highLights = highlightManager.getHighLights( bookView.getFileName() );

        for ( final HighLight highLight: highLights ) {
            if ( highLight.getIndex() == bookView.getIndex() ) {

                builder.setSpan(new HighlightSpan(highLight),
                        highLight.getStart(), highLight.getEnd(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
	
	@Override
	public void pageDown() {
		this.scroll( bookView.getHeight() - 2 * bookView.getVerticalMargin());
	}
	
	@Override
	public void pageUp() {
		this.scroll( (bookView.getHeight() - 2* bookView.getVerticalMargin() ) * -1);
	}
	
	@Override
	public void setPosition(int pos) {
		this.storedPosition = pos;
		updatePosition();
	}
	
	@Override
	public void clearText() {
		this.childView.setText("");	
		this.text = null;
	}
	
	@Override
	public void setRelativePosition(double position) {
		this.storedPercentage = position;
		updatePosition();
	}
	
	public void updatePosition() {

		if ( storedPosition == -1 && this.storedPercentage == -1d ) {
			return;  //Hopefully come back later
		}

		if ( childView.getText().length() == 0 ) {
			return;
		}

		if ( storedPercentage != -1d ) {
			this.storedPosition = (int) (this.childView.getText().length() * storedPercentage);
			this.storedPercentage = -1d;
		}

		Layout layout = this.childView.getLayout();

		if ( layout != null ) {
			int pos = Math.max(0, this.storedPosition);
			int line = layout.getLineForOffset(pos);

			if ( line > 0 ) {
				int newPos = layout.getLineBottom(line -1);
				bookView.scrollTo(0, newPos);
			} else {
				bookView.scrollTo(0, 0);
			}
		}						 
	}

	
	@Override
	public Option<Spanned> getText() {
		return option(text);
	}
	
	@Override
	public void reset() {
		this.storedPosition = -1;		
	}
	
	@Override
	public void clearStoredPosition() {
		this.storedPosition = -1;	
	}
	
	private void scroll( int delta ) {
		
		if ( this.bookView == null ) {
			return;
		}
		
		int currentPos = bookView.getScrollY();
		
		int newPos = currentPos + delta;
		
		bookView.scrollTo(0, findClosestLineBottom(newPos));
		
		if ( bookView.getScrollY() == currentPos ) {
						
			if ( delta < 0 ) {				
				if (bookView.getSpine() == null || ! bookView.getSpine().navigateBack() ) {					
					return;
				}
			} else {				
				if (bookView.getSpine() == null ||  ! bookView.getSpine().navigateForward() ) {
					return;
				}
			}
			
			this.childView.setText("");
			
			if ( delta > 0 ) {
				bookView.scrollTo(0,0);
				this.storedPosition = -1;
			} else {
				bookView.scrollTo(0, bookView.getHeight());
				
				//We scrolled back up, so we want the very bottom of the text.
				this.storedPosition = Integer.MAX_VALUE;
			}	
			
			bookView.loadText();
		}
	}
	
	private int findClosestLineBottom( int ypos ) {
				
		Layout layout = this.childView.getLayout();
		
		if ( layout == null ) {
			return ypos;
		}
		
		int currentLine = layout.getLineForVertical(ypos);
		
		if ( currentLine > 0 ) {
			return layout.getLineBottom(currentLine -1);
		} else {
			return 0;
		}		
	}
	
	private int findTextOffset(int ypos) {
		
		Layout layout = this.childView.getLayout();
		if ( layout == null ) {
			return 0;
		}
		
		return layout.getLineStart(layout.getLineForVertical(ypos));		
	}
	
	@Override
	public boolean isScrolling() {
		return true;
	}
	
}
