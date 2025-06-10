package com.example.movieapp.data.local;

import android.provider.BaseColumns;

/**
 * Contract class for defining database schema
 */
public final class DatabaseContract {

    // To prevent someone from instantiating the contract class
    private DatabaseContract() {}
    
    /**
     * Inner class that defines the table contents
     */
    public static class BookmarkEntry implements BaseColumns {
        public static final String TABLE_NAME = "bookmarks";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}