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
package net.zorgblub.typhon.library;

import java.io.IOException;

import jedi.option.Option;
import nl.siegmann.epublib.domain.Book;


public interface LibraryService {
	
		
	/**
	 * Adds a new book to the library database, optionally copying it.
	 * 
	 * @param fileName
	 * @param book
	 * @param updateLastRead
	 * @param copyFile
	 * @throws IOException
	 */
	void storeBook(String fileName, Book book, boolean updateLastRead, boolean copyFile) throws IOException;
	
	void updateReadingProgress(String fileName, int progress);
	
	QueryResult<LibraryBook> findAllByLastRead(String filter);
	
	QueryResult<LibraryBook> findAllByLastAdded(String filter);
	
	QueryResult<LibraryBook> findAllByTitle(String filter);
	
	QueryResult<LibraryBook> findAllByAuthor(String filter);
	
	QueryResult<LibraryBook> findUnread(String filter);
	
	Option<LibraryBook> getBook(String fileName);
	
	boolean hasBook(String fileName);
	
	void deleteBook(String fileName);
	
	void close();
}
