/*
 * Copyright (C) 2013 Alex Kuiper
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

import net.zorgblub.typhon.scheduling.QueueableAsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jedi.option.Option;

import static jedi.option.Options.none;

/**
 * Task which deletes all files in the library that no longer exist.
 */
public class CleanFilesTask extends QueueableAsyncTask<Void, Void, Void> {

    private LibraryService libraryService;

    private DeleteBooksCallback callback;

    private int deletedFiles = 0;

    public interface DeleteBooksCallback {
        void booksDeleted(int numberOfDeletedBooks);
    }

    public CleanFilesTask(LibraryService service) {
        this.libraryService = service;
    }

    public CleanFilesTask(LibraryService service, DeleteBooksCallback callback ) {
        this(service);
        setCallback(callback);
    }

    @Override
    public Option<Void> doInBackground(Void... voids) {

        QueryResult<LibraryBook> allBooks = libraryService.findAllByTitle(null);
        List<String> filesToDelete = new ArrayList<>();

        for ( int i=0; i < allBooks.getSize() && ! isCancelled(); i++ ) {
            LibraryBook book = allBooks.getItemAt(i);
            File file = new File(book.getFileName());

            if ( ! file.exists() ) {
                filesToDelete.add(book.getFileName());
            }
        }

        allBooks.close();

        for ( String fileName: filesToDelete ) {
            if ( ! isCancelled() ) {
                libraryService.deleteBook(fileName);
                deletedFiles++;
            }
        }

        return none();
    }

    @Override
    public void doOnPostExecute(Option<Void> none)  {
        if ( this.callback != null ) {
            callback.booksDeleted(deletedFiles);
        }
    }

    public void setCallback(DeleteBooksCallback callback) {
        this.callback = callback;
    }
}
