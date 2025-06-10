package com.example.movieapp.data.local;

import com.example.movieapp.data.model.Film;

/**
 * Entity class representing a bookmark record in the database
 */
public class BookmarkEntity {
    
    private int id;
    private int movieId;
    private String title;
    private String posterPath;
    private String backdropPath;
    private String overview;
    private String releaseDate;
    private float rating;
    private long timestamp;
    
    // Constructor for creating from database query
    public BookmarkEntity(int id, int movieId, String title, String posterPath,
                          String backdropPath, String overview, String releaseDate,
                          float rating, long timestamp) {
        this.id = id;
        this.movieId = movieId;
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.timestamp = timestamp;
    }
    
    // Constructor for creating from Film object
    public BookmarkEntity(Film film) {
        this.movieId = film.getId();
        this.title = film.getTitle();
        this.posterPath = film.getPosterPath();
        this.backdropPath = film.getBackdropPath();
        this.overview = film.getOverview();
        this.releaseDate = film.getReleaseDate();
        this.rating = film.getRating();
        this.timestamp = System.currentTimeMillis();
    }
    
    // Convert to Film object
    public Film toFilm() {
        return new Film(
            movieId,          // Use movieId, not database id
            title,
            posterPath,
            backdropPath,
            overview,
            releaseDate,
            rating
        );
    }
    
    // Getters
    public int getId() { return id; }
    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public String getOverview() { return overview; }
    public String getReleaseDate() { return releaseDate; }
    public float getRating() { return rating; }
    public long getTimestamp() { return timestamp; }
    
    // Get release year from the full date
    public String getReleaseYear() {
        if (releaseDate != null && releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        }
        return "";
    }
}