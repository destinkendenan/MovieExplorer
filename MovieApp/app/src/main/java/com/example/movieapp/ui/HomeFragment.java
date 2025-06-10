package com.example.movieapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieapp.R;
import com.example.movieapp.data.model.Film;
import com.example.movieapp.data.network.ApiConfig;
import com.example.movieapp.data.model.MovieResponse;
import com.example.movieapp.data.network.ApiService;
import com.example.movieapp.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private RecyclerView rvTopFive, rvLatest;
    private View contentContainer;
    private ProgressBar mainProgressBar;
    private View networkErrorView;
    private Button btnRefresh;
    private List<Film> topRatedList = new ArrayList<>();
    private List<Film> latestFilmList = new ArrayList<>();
    private FilmAdapter topRatedAdapter, latestAdapter;
    private boolean isTopRatedLoading = false;
    private boolean isLatestLoading = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAdapters();
        setupListeners(view);
        setupThemeToggle(view);

        loadData();
    }

    private void initViews(View view) {
        contentContainer = view.findViewById(R.id.contentContainer);
        mainProgressBar = view.findViewById(R.id.mainProgressBar);

        rvTopFive = view.findViewById(R.id.rvTopFive);
        rvLatest = view.findViewById(R.id.rvLatest);

        networkErrorView = view.findViewById(R.id.networkErrorView);
        btnRefresh = networkErrorView.findViewById(R.id.btnRefresh);
    }

    private void setupAdapters() {
        topRatedAdapter = new FilmAdapter(topRatedList, FilmAdapter.TYPE_TOP_RATED);
        rvTopFive.setAdapter(topRatedAdapter);
        topRatedAdapter.setOnItemClickListener(this::navigateToDetail);

        latestAdapter = new FilmAdapter(latestFilmList, FilmAdapter.TYPE_LATEST);
        rvLatest.setAdapter(latestAdapter);
        latestAdapter.setOnItemClickListener(this::navigateToDetail);
    }

    private void setupListeners(View view) {
        btnRefresh.setOnClickListener(v -> {
            hideNetworkError();
            refreshData();
        });

        TextView tvSeeMoreLatest = view.findViewById(R.id.tvSeeMoreLatest);
        tvSeeMoreLatest.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DiscoverActivity.class);
            startActivity(intent);
        });
    }

    private void setupThemeToggle(View view) {
        ImageView themeToggleTop = view.findViewById(R.id.ivThemeToggleTop);

        ThemeUtils.updateThemeIcon(themeToggleTop, requireContext());

        themeToggleTop.setOnClickListener(v -> {
            ThemeUtils.toggleTheme(requireContext());
        });
    }

    private void loadData() {
        loadTopRatedMovies();
        loadLatestMovies();
    }

    public void refreshData() {
        showLoading(true);

        isTopRatedLoading = false;
        isLatestLoading = false;

        loadData();
    }

    private void loadTopRatedMovies() {
        isTopRatedLoading = true;
        updateLoadingState();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ApiService apiService = ApiConfig.getRetrofitInstance().create(ApiService.class);
                Call<MovieResponse> call = apiService.getTopRatedMovies(ApiConfig.getApiKey());

                Response<MovieResponse> response = call.execute();

                handler.post(() -> {
                    isTopRatedLoading = false;
                    updateLoadingState();

                    if (response.isSuccessful() && response.body() != null) {
                        handleTopRatedResponse(response.body());
                    } else {
                        handleTopRatedError(response.code());
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    isTopRatedLoading = false;
                    updateLoadingState();

                    showNetworkError();
                    Log.e(TAG, "Network error: " + e.getMessage());
                });
            }
        });
    }

    private void handleTopRatedResponse(MovieResponse response) {
        List<Film> films = response.getResults();
        if (films != null && !films.isEmpty()) {
            topRatedList.clear();
            topRatedList.addAll(films.subList(0, Math.min(films.size(), 5)));
            topRatedAdapter.notifyDataSetChanged();
        }
    }

    private void handleTopRatedError(int responseCode) {
        Toast.makeText(getContext(),
                "Failed to load top rated movies: " + responseCode,
                Toast.LENGTH_SHORT).show();
    }

    private void loadLatestMovies() {
        isLatestLoading = true;
        updateLoadingState();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ApiService apiService = ApiConfig.getRetrofitInstance().create(ApiService.class);
                Call<MovieResponse> call = apiService.getNowPlayingMovies(ApiConfig.getApiKey());

                Response<MovieResponse> response = call.execute();

                handler.post(() -> {
                    isLatestLoading = false;
                    updateLoadingState();

                    if (response.isSuccessful() && response.body() != null) {
                        handleLatestMoviesResponse(response.body());
                    } else {
                        handleLatestMoviesError(response.code());
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    isLatestLoading = false;
                    updateLoadingState();

                    showNetworkError();
                    Log.e(TAG, "Network error loading latest movies: " + e.getMessage());
                });
            }
        });
    }

    private void handleLatestMoviesResponse(MovieResponse response) {
        List<Film> movies = response.getResults();

        if (movies != null && !movies.isEmpty()) {
            Collections.sort(movies, (film1, film2) -> {
                if (film1.getReleaseDate() != null && film2.getReleaseDate() != null) {
                    return film2.getReleaseDate().compareTo(film1.getReleaseDate());
                }
                return 0;
            });

            List<Film> latestMovies = new ArrayList<>();
            int count = Math.min(5, movies.size());
            for (int i = 0; i < count; i++) {
                latestMovies.add(movies.get(i));
            }

            latestFilmList.clear();
            latestFilmList.addAll(latestMovies);
            latestAdapter.notifyDataSetChanged();
        }
    }

    private void handleLatestMoviesError(int responseCode) {
        Log.e(TAG, "Failed to load latest movies: " + responseCode);
        Toast.makeText(getContext(), "Failed to load latest movies", Toast.LENGTH_SHORT).show();
    }

    private void navigateToDetail(Film film) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("film", film);
        startActivity(intent);
    }

    private void updateLoadingState() {
        boolean isAnyLoading = isTopRatedLoading || isLatestLoading;
        showLoading(isAnyLoading);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            contentContainer.setVisibility(View.GONE);
            mainProgressBar.setVisibility(View.VISIBLE);

            networkErrorView.setVisibility(View.GONE);
        } else {
            contentContainer.setVisibility(View.VISIBLE);
            mainProgressBar.setVisibility(View.GONE);
        }
    }

    private void showNetworkError() {
        if (networkErrorView != null) {
            contentContainer.setVisibility(View.GONE);
            mainProgressBar.setVisibility(View.GONE);
            networkErrorView.setVisibility(View.VISIBLE);
        }
    }

    private void hideNetworkError() {
        if (networkErrorView != null) {
            networkErrorView.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
        }
    }
}