package com.example.movieapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieapp.R;
import com.example.movieapp.data.model.Film;
import com.example.movieapp.data.network.ApiConfig;
import com.example.movieapp.data.network.ApiService;
import com.example.movieapp.data.model.MovieResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class DiscoverActivity extends AppCompatActivity {
    private RecyclerView rvDiscoverMovies;
    private ProgressBar progressBar;
    private ProgressBar footerProgressBar;
    private ImageView ivBack;
    private List<Film> movieList = new ArrayList<>();
    private FilmAdapter adapter;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        initThreadingComponents();
        initViews();
        setupRecyclerView();
        setupListeners();

        resetPagination();
        loadLatestMovies();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void initThreadingComponents() {
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void initViews() {
        rvDiscoverMovies = findViewById(R.id.rvDiscoverMovies);
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.ivBack);
        footerProgressBar = findViewById(R.id.footerProgressBar);
    }

    private void setupRecyclerView() {
        adapter = new FilmAdapter(movieList, FilmAdapter.TYPE_DISCOVER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvDiscoverMovies.setLayoutManager(layoutManager);
        rvDiscoverMovies.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        adapter.setOnItemClickListener(film -> {
            Intent intent = new Intent(DiscoverActivity.this, DetailActivity.class);
            intent.putExtra("film", film);
            startActivity(intent);
        });

        setupScrollListener();
    }

    private void setupScrollListener() {
        rvDiscoverMovies.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= 20) {
                        loadMoreMovies();
                    }
                }
            }
        });
    }

    private void resetPagination() {
        currentPage = 1;
        isLastPage = false;
        movieList.clear();
        adapter.notifyDataSetChanged();
    }

    private void loadLatestMovies() {
        showLoading(true);
        isLoading = true;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ApiService apiService = ApiConfig.getRetrofitInstance().create(ApiService.class);
                Call<MovieResponse> call = apiService.getNowPlayingMovies(ApiConfig.getApiKey(), currentPage);

                Response<MovieResponse> response = call.execute();

                handler.post(() -> {
                    showLoading(false);
                    isLoading = false;

                    if (response.isSuccessful() && response.body() != null) {
                        handleMovieResponse(response.body(), true);
                    } else {
                        // Show error message
                        Toast.makeText(DiscoverActivity.this,
                                "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    showLoading(false);
                    isLoading = false;
                    Toast.makeText(DiscoverActivity.this,
                            "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadMoreMovies() {
        footerProgressBar.setVisibility(View.VISIBLE);
        isLoading = true;

        currentPage++;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ApiService apiService = ApiConfig.getRetrofitInstance().create(ApiService.class);
                Call<MovieResponse> call = apiService.getNowPlayingMovies(ApiConfig.getApiKey(), currentPage);

                Response<MovieResponse> response = call.execute();

                handler.post(() -> {
                    footerProgressBar.setVisibility(View.GONE);
                    isLoading = false;

                    if (response.isSuccessful() && response.body() != null) {
                        handleMovieResponse(response.body(), false);
                    } else {
                        currentPage--;
                        Toast.makeText(DiscoverActivity.this,
                                "Error loading more: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    footerProgressBar.setVisibility(View.GONE);
                    isLoading = false;
                    currentPage--;
                    Toast.makeText(DiscoverActivity.this,
                            "Failed to load more: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void handleMovieResponse(MovieResponse movieResponse, boolean isFirstPage) {
        List<Film> films = movieResponse.getResults();

        if (films != null && !films.isEmpty()) {
            if (isFirstPage) {
                movieList.clear();
            }

            int positionStart = movieList.size();

            movieList.addAll(films);

            if (isFirstPage) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeInserted(positionStart, films.size());
            }

            if (currentPage >= movieResponse.getTotalPages()) {
                isLastPage = true;
            }
        } else {
            if (isFirstPage) {
                movieList.clear();
                adapter.notifyDataSetChanged();
            }
            isLastPage = true;
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}