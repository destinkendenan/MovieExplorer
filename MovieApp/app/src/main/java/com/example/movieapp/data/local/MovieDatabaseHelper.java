package com.example.movieapp.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "movie_app.db";
    private static final int DATABASE_VERSION = 1;
    
    // Singleton instance
    private static MovieDatabaseHelper instance;
    
    // Create table statement
    private static final String SQL_CREATE_BOOKMARKS_TABLE =
            "CREATE TABLE " + DatabaseContract.BookmarkEntry.TABLE_NAME + " (" +
                    DatabaseContract.BookmarkEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.BookmarkEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL," +
                    DatabaseContract.BookmarkEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                    DatabaseContract.BookmarkEntry.COLUMN_POSTER_PATH + " TEXT," +
                    DatabaseContract.BookmarkEntry.COLUMN_BACKDROP_PATH + " TEXT," +
                    DatabaseContract.BookmarkEntry.COLUMN_OVERVIEW + " TEXT," +
                    DatabaseContract.BookmarkEntry.COLUMN_RELEASE_DATE + " TEXT," +
                    DatabaseContract.BookmarkEntry.COLUMN_RATING + " REAL," +
                    DatabaseContract.BookmarkEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";
                    
    // Drop table statement
    private static final String SQL_DELETE_BOOKMARKS_TABLE =
            "DROP TABLE IF EXISTS " + DatabaseContract.BookmarkEntry.TABLE_NAME;
    
    // Private constructor for singleton
    private MovieDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    // Singleton access method
    public static synchronized MovieDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MovieDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BOOKMARKS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For simplicity, drop and recreate the table on upgrade
        db.execSQL(SQL_DELETE_BOOKMARKS_TABLE);
        onCreate(db);
    }
}