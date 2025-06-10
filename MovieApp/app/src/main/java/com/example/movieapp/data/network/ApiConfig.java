package com.example.movieapp.data.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiConfig {
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String API_KEY = "b530b611c7708d4ffb363a402e1a6e2b"; // Replace with your actual TMDB API key

    private static Retrofit retrofit = null;
    
    /**
     * Get the API key for TMDB API
     */
    public static String getApiKey() {
        return API_KEY;
    }
    
    /**
     * Get Retrofit instance
     */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Set up logging interceptor for debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();
            
            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
    
    /**
     * Get API service interface
     */
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }
    
    /**
     * Get full image URL from image path
     */
    public static String getImageUrl(String imagePath) {
        return "https://image.tmdb.org/t/p/w500" + imagePath;
    }

    /**
     * Get the full URL for a backdrop image
     * @param backdropPath The backdrop path from movie data
     * @return Complete URL for the backdrop image
     */
    public static String getBackdropUrl(String backdropPath) {
        if (backdropPath == null || backdropPath.isEmpty()) {
            return null;
        }
        // Backdrop images are typically larger than posters
        // w1280 is a good size for full-width backdrops
        return "https://image.tmdb.org/t/p/w1280" + backdropPath;
    }
}