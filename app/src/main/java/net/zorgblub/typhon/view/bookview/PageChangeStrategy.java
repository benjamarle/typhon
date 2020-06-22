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

import android.text.Spanned;

import jedi.option.Option;

public interface PageChangeStrategy {

	/**
	 * Loads the given section of text.
	 * 
	 * This will be a whole "file" from an epub.
	 * 
	 * @param text
	 */
	void loadText(Spanned text);

    /**
     * Called on the main thread to actually update the TextView
     */
	void updateGUI();
	
	/**
	 * Returns the text-offset of the top-left character on the screen.
	 * 
	 * @return
	 */
	int getTopLeftPosition();


    /**
     * Gets the current reading progress in the chapter.
     * @return
     */
	int getProgressPosition();

	
	/**
	 * Returns if we're at the start of the current section
	 * @return
	 */
	boolean isAtStart();
	
	/**
	 * Returns if we're at the end of the current section
	 * @return
	 */
	boolean isAtEnd();
	
	/**
	 * Tells this strategy to move the window so the specified
	 * position ends up on the top line of the windows.
	 * 
	 * @param pos
	 */
	void setPosition(int pos);
	
	/**
	 * Sets a position relative to the text length:
	 * 0 means the start of the text, 1 means the end of 
	 * the text.
	 * 
	 * @param position a value between 0 and 1
	 */
	void setRelativePosition(double position);
	
	/**
	 * Move the view one page up.
	 */
	void pageUp();
	
	/**
	 * Move the view one page down.
	 */
	void pageDown();
	
	/** Simple way to differentiate without instanceof **/
	boolean isScrolling();
	
	/**
	 * Clears all text held in this strategy's buffer.
	 */
	void clearText();
	
	/**
	 * Clears the stored position in this strategy.
	 */
	void clearStoredPosition();
	
	/**
	 * Updates all fields to reflect a new configuration.
	 */
	void updatePosition();
	
	/**
	 * Clears both the buffer and stored position.
	 */
	void reset();
	
	/**
	 * Gets the text held in this strategy's buffer.
	 * 
	 * @return the text
	 */
	Option<Spanned> getText();
	
	/**
	 * Gets the text for the next page to be displayed, or null if we've reached the end.
	 * 
	 * @return
	 */
	Option<CharSequence> getNextPageText();
	
	/**
	 * Gets the text for the previous page to be displayed, or null if we've reached the start.
	 * 
	 * @return
	 */
	Option<CharSequence> getPreviousPageText();

    void setBookView(BookView bookView);
	
}
