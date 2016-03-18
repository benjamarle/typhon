package org.zorgblub.rikai;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.rikai.dictionary.db.DatabaseException;
import org.rikai.dictionary.db.ResultCursor;
import org.rikai.dictionary.db.SqliteDatabase;

/**
 * Created by Benjamin on 15/09/2015.
 */
public class DroidSqliteDatabase implements SqliteDatabase {

    private boolean loaded;

    private String searchQuery;

    private SQLiteDatabase database = null;

    @Override
    public boolean loadEdict(String s) {
        try {
            database = SQLiteDatabase.openDatabase(s, null, SQLiteDatabase.OPEN_READONLY);
            return true;
        } catch (SQLiteException e) {
            throw new DatabaseException("Error loading the dictionary on ["+s+"]", e);
        }
    }

    @Override
    public String getSearchQuery() {
        return searchQuery;
    }

    @Override
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @Override
    public void close() {
        if (database != null) {
            database.close();
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public ResultCursor findWord(String word) {
        Cursor cursor = database.rawQuery(
                searchQuery,
                new String[]{word, word}
        );

        return new DroidResultCursor(cursor);
    }

    public static class DroidResultCursor implements ResultCursor {
        private Cursor cursor;

        public DroidResultCursor(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public boolean next() {
            return this.cursor.moveToNext();
        }

        @Override
        public String getValue(String s) {
            return cursor.getString(cursor.getColumnIndex(s));

        }

        @Override
        public void close() {
            cursor.close();
        }
    }
}
