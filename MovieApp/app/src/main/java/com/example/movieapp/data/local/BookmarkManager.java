package com.example.movieapp.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.movieapp.data.model.Film;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager class for bookmark operations
 */
public class BookmarkManager {

    private MovieDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private Context context;
    
    public BookmarkManager(Context context) {
        this.context = context;
        dbHelper = MovieDatabaseHelper.getInstance(context);
    }
    
    public void open() {
        database = dbHelper.getWritableDatabase();
    }
    
    public void close() {
        // Check if app is running in debug mode using Android API
        boolean isDebug = 0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
        
        if (isDebug) {
            return;
        }

        if (database != null && database.isOpen()) {
            database.close();
        }
    }
    
    // Tambahkan method ensureDatabaseOpen untuk memastikan database terbuka
    private void ensureDatabaseOpen() {
        if (database == null || !database.isOpen()) {
            open();
        }
    }
    
    /**
     * Add a movie to bookmarks
     */
    public long addBookmark(Film film) {
        ensureDatabaseOpen();
        
        BookmarkEntity entity = new BookmarkEntity(film);
    
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.BookmarkEntry.COLUMN_MOVIE_ID, entity.getMovieId());
        values.put(DatabaseContract.BookmarkEntry.COLUMN_TITLE, entity.getTitle());
        values.put(DatabaseContract.BookmarkEntry.COLUMN_POSTER_PATH, entity.getPosterPath());
        values.put(DatabaseContract.BookmarkEntry.COLUMN_BACKDROP_PATH, entity.getBackdropPath());
        values.put(DatabaseContract.BookmarkEntry.COLUMN_OVERVIEW, entity.getOverview());
        values.put(DatabaseContract.BookmarkEntry.COLUMN_RELEASE_DATE, entity.getReleaseDate());
        values.put(DatabaseContract.BookmarkEntry.COLUMN_RATING, entity.getRating());
    
        return database.insert(DatabaseContract.BookmarkEntry.TABLE_NAME, null, values);
    }
    
    /**
     * Remove a movie from bookmarks
     */
    public int removeBookmark(int movieId) {
        ensureDatabaseOpen();
        
        String selection = DatabaseContract.BookmarkEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(movieId) };
        
        return database.delete(
                DatabaseContract.BookmarkEntry.TABLE_NAME, 
                selection, 
                selectionArgs);
    }
    
    /**
     * Check if a movie is bookmarked
     */
    public boolean isBookmarked(int movieId) {
        ensureDatabaseOpen();
        
        String[] projection = { DatabaseContract.BookmarkEntry.COLUMN_MOVIE_ID };
        String selection = DatabaseContract.BookmarkEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(movieId) };
        
        Cursor cursor = database.query(
                DatabaseContract.BookmarkEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
                
        boolean isBookmarked = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        
        return isBookmarked;
    }
    
    /**
     * Get all bookmarked movies as BookmarkEntity objects
     */
    public List<BookmarkEntity> getAllBookmarkEntities() {
        ensureDatabaseOpen();
        
        List<BookmarkEntity> bookmarkList = new ArrayList<>();
        
        String[] projection = {
                DatabaseContract.BookmarkEntry._ID,
                DatabaseContract.BookmarkEntry.COLUMN_MOVIE_ID,
                DatabaseContract.BookmarkEntry.COLUMN_TITLE,
                DatabaseContract.BookmarkEntry.COLUMN_POSTER_PATH,
                DatabaseContract.BookmarkEntry.COLUMN_BACKDROP_PATH,
                DatabaseContract.BookmarkEntry.COLUMN_OVERVIEW,
                DatabaseContract.BookmarkEntry.COLUMN_RELEASE_DATE,
                DatabaseContract.BookmarkEntry.COLUMN_RATING,
                DatabaseContract.BookmarkEntry.COLUMN_TIMESTAMP
        };
        
        String sortOrder = DatabaseContract.BookmarkEntry.COLUMN_TIMESTAMP + " DESC";
        
        Cursor cursor = database.query(
                DatabaseContract.BookmarkEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder);
                
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry._ID));
                int movieId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COLUMN_MOVIE_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COLUMN_TITLE));
                String posterPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COLUMN_POSTER_PATH));
                String backdropPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COLUMN_BACKDROP_PATH));
                String overview = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COLUMN_OVERVIEW));
                String releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COLUMN_RELEASE_DATE));
                float rating = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COLUMN_RATING));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.BookmarkEntry.COLUMN_TIMESTAMP));
                
                BookmarkEntity entity = new BookmarkEntity(
                        id, movieId, title, posterPath, backdropPath, 
                        overview, releaseDate, rating, timestamp);
                        
                bookmarkList.add(entity);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        return bookmarkList;
    }
    
    /**
     * Get all bookmarked movies as Film objects
     */
    public List<Film> getAllBookmarks() {
        // Tidak perlu memanggil ensureDatabaseOpen() disini
        // karena getAllBookmarkEntities() sudah melakukannya
        
        List<Film> filmList = new ArrayList<>();
        List<BookmarkEntity> entities = getAllBookmarkEntities();
        
        for (BookmarkEntity entity : entities) {
            filmList.add(entity.toFilm());
        }
        
        return filmList;
    }
}