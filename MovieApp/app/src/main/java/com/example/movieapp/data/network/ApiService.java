package com.example.movieapp.data.network;

import com.example.movieapp.data.model.CastResponse;
import com.example.movieapp.data.model.MovieResponse;
import com.example.movieapp.data.model.VideoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(
        @Query("api_key") String apiKey);
    
    @GET("movie/now_playing")
    Call<MovieResponse> getNowPlayingMovies(
        @Query("api_key") String apiKey,
        @Query("page") int page
    );
    
    @GET("search/movie")
    Call<MovieResponse> searchMovies(
        @Query("api_key") String apiKey,
        @Query("query") String query,
        @Query("page") int page
    );

    @GET("discover/movie")
    Call<MovieResponse> discoverMovies(
        @Query("api_key") String apiKey,
        @Query("with_genres") String genreIds,
        @Query("page") int page
    );
    
    // Keep the existing method for backwards compatibility
    @GET("movie/now_playing")
    Call<MovieResponse> getNowPlayingMovies(@Query("api_key") String apiKey);
    
    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String query
    );

    @GET("discover/movie")
    Call<MovieResponse> discoverMovies(
            @Query("api_key") String apiKey,
            @Query("with_genres") String genreIds
    );

    @GET("movie/{movie_id}/credits")
    Call<CastResponse> getMovieCredits(
        @Path("movie_id") int movieId,
        @Query("api_key") String apiKey
    );

    // Tambahkan ke file ApiService.java
    @GET("movie/{movie_id}/videos")
    Call<VideoResponse> getMovieVideos(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey
    );

    @GET("discover/movie")
    Call<MovieResponse> discoverMoviesWithQuery(
        @Query("api_key") String apiKey,
        @Query("with_genres") String genres,
        @Query("query") String query,
        @Query("page") int page
    );
}