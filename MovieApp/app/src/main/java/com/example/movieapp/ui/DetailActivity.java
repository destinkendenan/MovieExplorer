package com.example.movieapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieapp.R;
import com.example.movieapp.data.local.BookmarkManager;
import com.example.movieapp.data.model.Cast;
import com.example.movieapp.data.model.CastResponse;
import com.example.movieapp.data.model.Film;
import com.example.movieapp.data.model.Video;
import com.example.movieapp.data.model.VideoResponse;
import com.example.movieapp.data.network.ApiConfig;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private ImageView ivBackdrop, ivPoster, ivBack, ivFavorite;
    private TextView tvTitle, tvReleaseYear, tvRating, tvGenres, tvOverview;
    private RatingBar rbRating;
    private RecyclerView rvCast;
    private YouTubePlayerView youtubePlayerView;
    private FrameLayout trailerThumbnailContainer;
    private Film film;
    private CastAdapter castAdapter;
    private List<Cast> castList = new ArrayList<>();
    private BookmarkManager bookmarkManager;
    private boolean isBookmarked = false;
    private String trailerKey;
    private boolean isPlayerInitialized = false;
    private YouTubePlayer youTubePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        bookmarkManager = new BookmarkManager(this);
        bookmarkManager.open();

        film = getIntent().getParcelableExtra("film");
        if (film == null) {
            Log.e(TAG, "No film data received");
            Toast.makeText(this, "Error loading movie details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();

        checkBookmarkStatus();

        displayFilmData();

        loadCast();
        loadTrailer();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (youtubePlayerView != null) {
            youtubePlayerView.release();
        }
        if (bookmarkManager != null) {
            bookmarkManager.close();
        }
    }

    private void initViews() {
        ivBackdrop = findViewById(R.id.ivBackdrop);
        ivPoster = findViewById(R.id.ivPoster);
        ivBack = findViewById(R.id.ivBack);
        ivFavorite = findViewById(R.id.ivFavorite);
        tvTitle = findViewById(R.id.tvTitle);
        tvReleaseYear = findViewById(R.id.tvReleaseYear);
        tvRating = findViewById(R.id.tvRating);
        rbRating = findViewById(R.id.rbRating);
        tvGenres = findViewById(R.id.tvGenres);
        tvOverview = findViewById(R.id.tvOverview);

        rvCast = findViewById(R.id.rvCast);
        rvCast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        castAdapter = new CastAdapter(castList);
        rvCast.setAdapter(castAdapter);

        youtubePlayerView = findViewById(R.id.youtubePlayerView);
        trailerThumbnailContainer = findViewById(R.id.trailerThumbnailContainer);

        getLifecycle().addObserver(youtubePlayerView);
    }
    

    private void setupListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());

        ivFavorite.setOnClickListener(v -> toggleBookmark());
        ivFavorite.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_background));

        trailerThumbnailContainer.setOnClickListener(v -> playTrailer());
    }

    private void displayFilmData() {
        tvTitle.setText(film.getTitle());
        tvReleaseYear.setText(film.getReleaseYear());
        tvOverview.setText(film.getOverview());

        String formattedRating = String.format("%.1f", film.getRating());
        tvRating.setText(formattedRating);

        float starRating = film.getRating() / 2;
        rbRating.setRating(starRating);

        Glide.with(this)
            .load(ApiConfig.getImageUrl(film.getPosterPath()))
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(ivPoster);
            
        Glide.with(this)
            .load(ApiConfig.getBackdropUrl(film.getBackdropPath()))
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(ivBackdrop);

        setGenres(film.getGenreIds());
    }

    private void updateBookmarkIcon() {
        if (isBookmarked) {
            ivFavorite.setImageResource(R.drawable.ic_favorite_solid);
        } else {
            ivFavorite.setImageResource(R.drawable.ic_favorite_outline);
        }
    }

    private void setGenres(int[] genreIds) {
        StringBuilder genres = new StringBuilder();
        for (int i = 0; i < genreIds.length; i++) {
            if (i > 0) genres.append(", ");
            genres.append(getGenreName(genreIds[i]));
        }
        tvGenres.setText(genres.toString());
    }

    private String getGenreName(int genreId) {
        switch (genreId) {
            case 28: return "Action";
            case 12: return "Adventure";
            case 16: return "Animation";
            case 35: return "Comedy";
            case 80: return "Crime";
            case 99: return "Documentary";
            case 18: return "Drama";
            case 10751: return "Family";
            case 14: return "Fantasy";
            case 36: return "History";
            case 27: return "Horror";
            case 10402: return "Music";
            case 9648: return "Mystery";
            case 10749: return "Romance";
            case 878: return "Science Fiction";
            case 10770: return "TV Movie";
            case 53: return "Thriller";
            case 10752: return "War";
            case 37: return "Western";
            default: return "Unknown";
        }
    }

    private void checkBookmarkStatus() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        
        executor.execute(() -> {
            boolean result = bookmarkManager.isBookmarked(film.getId());
            
            handler.post(() -> {
                isBookmarked = result;
                updateBookmarkIcon();
            });
        });
    }

    private void loadCast() {
        Call<CastResponse> call = ApiConfig.getApiService().getMovieCredits(film.getId(), ApiConfig.getApiKey());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<CastResponse> call, Response<CastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Cast> cast = response.body().getCast();
                    if (cast != null && !cast.isEmpty()) {
                        int maxSize = Math.min(cast.size(), 10);
                        castList.addAll(cast.subList(0, maxSize));
                        castAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<CastResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load cast: " + t.getMessage());
            }
        });
    }

    private void loadTrailer() {
        Call<VideoResponse> call = ApiConfig.getApiService().getMovieVideos(film.getId(), ApiConfig.getApiKey());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Video> videos = response.body().getResults();
                    if (videos != null && !videos.isEmpty()) {
                        for (Video video : videos) {
                            if ("YouTube".equals(video.getSite()) &&
                                    ("Trailer".equals(video.getType()) || "Teaser".equals(video.getType()))) {
                                trailerKey = video.getKey();

                                youtubePlayerView.setVisibility(View.VISIBLE);
                                initializeYouTubePlayer();

                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load trailer: " + t.getMessage());
            }
        });
    }

    private void toggleBookmark() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        
        executor.execute(() -> {
            boolean success;
            if (isBookmarked) {
                success = bookmarkManager.removeBookmark(film.getId()) > 0;
                if (success) {
                    isBookmarked = false;
                }
            } else {
                success = bookmarkManager.addBookmark(film) > 0;
                if (success) {
                    isBookmarked = true;
                }
            }

            final boolean finalIsBookmarked = isBookmarked;
            
            handler.post(() -> {
                updateBookmarkIcon();
                String message = finalIsBookmarked ? 
                        "Added to bookmarks" : "Removed from bookmarks";
                Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void playTrailer() {
        if (trailerKey == null || trailerKey.isEmpty()) {
            Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
            return;
        }

        trailerThumbnailContainer.setVisibility(View.GONE);
        youtubePlayerView.setVisibility(View.VISIBLE);
        
        if (isPlayerInitialized && youTubePlayer != null) {
            youTubePlayer.loadVideo(trailerKey, 0);
        } else {
            youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(YouTubePlayer player) {
                    youTubePlayer = player;
                    isPlayerInitialized = true;
                    player.loadVideo(trailerKey, 0);
                }
            });
        }
    }

    private void initializeYouTubePlayer() {
        youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer player) {
                youTubePlayer = player;
                isPlayerInitialized = true;

                if (trailerKey != null && !trailerKey.isEmpty()) {
                    player.loadVideo(trailerKey, 0);
                }
            }
        });
    }
}