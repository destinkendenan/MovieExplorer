package com.example.movieapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieapp.R;
import com.example.movieapp.data.model.Film;
import com.example.movieapp.data.model.MovieResponse;
import com.example.movieapp.data.network.ApiConfig;
import com.example.movieapp.utils.ThemeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final String PREF_NAME = "search_history";
    private static final String KEY_HISTORY = "history_items";
    private static final int MAX_HISTORY_ITEMS = 5;
    private EditText etSearch;
    private ImageView ivClear, ivBack;
    private ChipGroup chipGroupGenres;
    private ChipGroup chipGroupHistory;
    private TextView tvClearHistory;
    private TextView tvHistoryTitle;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;
    private ProgressBar footerProgressBar;
    private List<Film> searchResults = new ArrayList<>();
    private FilmAdapter adapter;
    private Map<Integer, Integer> genreMap = new HashMap<>();
    private List<Integer> selectedGenres = new ArrayList<>();
    private String currentQuery = "";
    private LinkedList<String> searchHistory = new LinkedList<>();
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initGenreMap();
        setupNavigationListeners();
        setupThemeToggle(view);
        setupRecyclerView();
        setupSearchFunctionality();
        setupGenreChips();

        loadSearchHistory();
    }

    private void initViews(View view) {
        ivBack = view.findViewById(R.id.ivBack);
        etSearch = view.findViewById(R.id.etSearch);
        ivClear = view.findViewById(R.id.ivClear);

        chipGroupGenres = view.findViewById(R.id.chipGroupGenres);
        chipGroupHistory = view.findViewById(R.id.chipGroupHistory);
        tvClearHistory = view.findViewById(R.id.tvClearHistory);
        tvHistoryTitle = view.findViewById(R.id.tvHistoryTitle);

        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        progressBar = view.findViewById(R.id.progressBar);
        footerProgressBar = view.findViewById(R.id.footerProgressBar);
    }

    private void initGenreMap() {
        genreMap.put(R.id.chipAction, 28);
        genreMap.put(R.id.chipAdventure, 12);
        genreMap.put(R.id.chipComedy, 35);
        genreMap.put(R.id.chipCrime, 80);
        genreMap.put(R.id.chipDrama, 18);
        genreMap.put(R.id.chipFantasy, 14);
        genreMap.put(R.id.chipHorror, 27);
        genreMap.put(R.id.chipMystery, 9648);
        genreMap.put(R.id.chipRomance, 10749);
        genreMap.put(R.id.chipSciFi, 878);
        genreMap.put(R.id.chipThriller, 53);
    }

    private void setupNavigationListeners() {
        // Initialize back button - returns to home tab
        ivBack.setOnClickListener(v -> {
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        });
    }

    private void setupThemeToggle(View view) {
        ImageView themeToggleSearch = view.findViewById(R.id.ivThemeToggleSearch);

        ThemeUtils.updateThemeIcon(themeToggleSearch, requireContext());

        themeToggleSearch.setOnClickListener(v -> {
            ThemeUtils.toggleTheme(requireContext());
        });
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvSearchResults.setLayoutManager(layoutManager);

        adapter = new FilmAdapter(searchResults);
        rvSearchResults.setAdapter(adapter);

        adapter.setOnItemClickListener(film -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("film", film);
            startActivity(intent);
        });

        setupPaginationListener(layoutManager);
    }

    private void setupPaginationListener(GridLayoutManager layoutManager) {
        rvSearchResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= 20) { // 20 is default page size
                        loadMoreResults();
                    }
                }
            }
        });
    }

    private void setupSearchFunctionality() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    addToSearchHistory(query);
                }
                performSearch();
                return true;
            }
            return false;
        });

        ivClear.setOnClickListener(v -> {
            etSearch.setText("");
            currentQuery = "";
            if (selectedGenres.isEmpty()) {
                searchResults.clear();
                adapter.notifyDataSetChanged();
                showEmptyState(true);
            } else {
                performSearch();
            }
        });

        ivClear.setVisibility(View.GONE);
    }

    private void setupGenreChips() {
        for (int i = 0; i < chipGroupGenres.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupGenres.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Integer genreId = genreMap.get(buttonView.getId());
                if (genreId != null) {
                    if (isChecked) {
                        selectedGenres.add(genreId);
                    } else {
                        selectedGenres.remove(genreId);
                    }
                    performSearch();
                }
            });
        }
    }

    private void performSearch() {
        currentPage = 1;
        isLastPage = false;

        currentQuery = etSearch.getText().toString().trim();

        if (currentQuery.isEmpty() && selectedGenres.isEmpty()) {
            searchResults.clear();
            adapter.notifyDataSetChanged();
            showEmptyState(true);
            return;
        }

        showLoading(true);
        isLoading = true;

        Call<MovieResponse> call = createSearchApiCall();

        executeSearchApiCall(call, true);
    }

    private void loadMoreResults() {
        footerProgressBar.setVisibility(View.VISIBLE);
        isLoading = true;

        currentPage++;

        Call<MovieResponse> call = createSearchApiCall();

        executeSearchApiCall(call, false);
    }

    private Call<MovieResponse> createSearchApiCall() {
        String genreQuery = buildGenreQueryString();

        if (!currentQuery.isEmpty() && !genreQuery.isEmpty()) {
            return ApiConfig.getApiService().discoverMoviesWithQuery(
                    ApiConfig.getApiKey(),
                    genreQuery.isEmpty() ? null : genreQuery,
                    currentQuery.isEmpty() ? null : currentQuery,
                    currentPage
            );
        } else if (!currentQuery.isEmpty()) {
            return ApiConfig.getApiService().searchMovies(
                    ApiConfig.getApiKey(), 
                    currentQuery, 
                    currentPage
            );
        } else {
            return ApiConfig.getApiService().discoverMovies(
                    ApiConfig.getApiKey(), 
                    genreQuery, 
                    currentPage
            );
        }
    }

    private String buildGenreQueryString() {
        if (selectedGenres.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Integer genreId : selectedGenres) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(genreId);
        }
        return sb.toString();
    }

    private void executeSearchApiCall(Call<MovieResponse> call, boolean isFirstPage) {
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (isFirstPage) {
                    showLoading(false);
                } else {
                    footerProgressBar.setVisibility(View.GONE);
                }
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulResponse(response.body(), isFirstPage);
                } else {
                    handleErrorResponse(response.code(), isFirstPage);
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                if (isFirstPage) {
                    showLoading(false);
                } else {
                    footerProgressBar.setVisibility(View.GONE);
                    currentPage--;
                }
                isLoading = false;

                Log.e(TAG, "Network Error: " + t.getMessage());
                String errorMessage = isFirstPage ? "Network error" : "Network error loading more results";
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();

                if (isFirstPage) {
                    showEmptyState(true);
                }
            }
        });
    }

    private void handleSuccessfulResponse(MovieResponse movieResponse, boolean isFirstPage) {
        List<Film> movies = movieResponse.getResults();

        if (movies != null && !movies.isEmpty()) {
            if (isFirstPage) {
                searchResults.clear();
                searchResults.addAll(movies);
                adapter.notifyDataSetChanged();
                showEmptyState(false);

                if (!currentQuery.isEmpty()) {
                    addToSearchHistory(currentQuery);
                }
            } else {
                int positionStart = searchResults.size();
                searchResults.addAll(movies);
                adapter.notifyItemRangeInserted(positionStart, movies.size());
            }

            if (currentPage >= movieResponse.getTotalPages()) {
                isLastPage = true;
            }
        } else {
            if (isFirstPage) {
                searchResults.clear();
                adapter.notifyDataSetChanged();
                showEmptyState(true);
            } else {
                isLastPage = true;
            }
        }
    }

    private void handleErrorResponse(int responseCode, boolean isFirstPage) {
        Log.e(TAG, "API Error: " + responseCode);
        String errorMessage = isFirstPage ? "Search failed" : "Error loading more results";
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        
        if (isFirstPage) {
            showEmptyState(true);
        } else {
            currentPage--;
        }
    }

    private void loadSearchHistory() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> historySet = prefs.getStringSet(KEY_HISTORY, new HashSet<>());
        searchHistory = new LinkedList<>(historySet);
        updateHistoryChips();
        updateHistoryVisibility();
    }

    private void saveSearchHistory() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> historySet = new HashSet<>(searchHistory);
        prefs.edit().putStringSet(KEY_HISTORY, historySet).apply();
    }

    private void addToSearchHistory(String query) {
        searchHistory.remove(query);

        searchHistory.addFirst(query);

        while (searchHistory.size() > MAX_HISTORY_ITEMS) {
            searchHistory.removeLast();
        }

        saveSearchHistory();

        updateHistoryChips();
        updateHistoryVisibility();
    }

    private void updateHistoryChips() {
        chipGroupHistory.removeAllViews();

        if (searchHistory.isEmpty()) {
            chipGroupHistory.setVisibility(View.GONE);
            tvClearHistory.setVisibility(View.GONE);
        } else {
            chipGroupHistory.setVisibility(View.VISIBLE);
            tvClearHistory.setVisibility(View.VISIBLE);

            for (String historyItem : searchHistory) {
                Chip chip = createHistoryChip(historyItem);
                chipGroupHistory.addView(chip);
            }
        }

        updateHistoryVisibility();
    }

    private Chip createHistoryChip(String historyItem) {
        Chip chip = new Chip(requireContext());
        chip.setText(historyItem);
        chip.setClickable(true);
        chip.setCheckable(false);
        chip.setCloseIconVisible(true);

        chip.setOnClickListener(v -> {
            etSearch.setText(historyItem);
            etSearch.setSelection(historyItem.length());
            performSearch();
        });

        chip.setOnCloseIconClickListener(v -> {
            searchHistory.remove(historyItem);
            saveSearchHistory();
            updateHistoryChips();
        });
        
        return chip;
    }

    private void updateHistoryVisibility() {
        View view = getView();
        if (view == null) return;

        TextView tvHistoryTitle = view.findViewById(R.id.tvHistoryTitle);
        TextView tvClearHistory = view.findViewById(R.id.tvClearHistory);

        boolean hasHistory = chipGroupHistory.getChildCount() > 0;

        int visibility = hasHistory ? View.VISIBLE : View.GONE;
        tvHistoryTitle.setVisibility(visibility);
        tvClearHistory.setVisibility(visibility);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvSearchResults.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean isEmpty) {
        llEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvSearchResults.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}