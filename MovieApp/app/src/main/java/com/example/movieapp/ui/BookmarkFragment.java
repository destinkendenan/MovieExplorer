package com.example.movieapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieapp.R;
import com.example.movieapp.data.local.BookmarkManager;
import com.example.movieapp.data.model.Film;
import com.example.movieapp.utils.ThemeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookmarkFragment extends Fragment {

    private RecyclerView rvBookmarks;
    private LinearLayout llEmptyState;
    private ImageView ivBack;
    private ImageView ivThemeToggle;
    private FilmAdapter adapter;
    private List<Film> bookmarkList = new ArrayList<>();
    private BookmarkManager bookmarkManager;
    private Handler mainHandler = new Handler(Looper.getMainLooper());


    public BookmarkFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupDatabase();
        setupRecyclerView();
        setupListeners();
        setupThemeToggle();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadBookmarks();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews(View view) {
        rvBookmarks = view.findViewById(R.id.rvBookmarks);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        ivBack = view.findViewById(R.id.ivBack);
        ivThemeToggle = view.findViewById(R.id.ivThemeToggleBookmark);
    }

    private void setupDatabase() {
        bookmarkManager = new BookmarkManager(requireContext());
        bookmarkManager.open();
    }

    private void setupRecyclerView() {
        adapter = new FilmAdapter(bookmarkList, FilmAdapter.TYPE_BOOKMARK);
        rvBookmarks.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBookmarks.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> {
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);

            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        });

        adapter.setOnItemClickListener(film -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("film", film);
            startActivity(intent);
        });

        adapter.setOnRemoveClickListener(position -> {
            Film film = bookmarkList.get(position);
            removeBookmark(film, position);
        });
    }

    private void setupThemeToggle() {
        ThemeUtils.updateThemeIcon(ivThemeToggle, requireContext());

        ivThemeToggle.setOnClickListener(v -> {
            ThemeUtils.toggleTheme(requireContext());
        });
    }

    private void loadBookmarks() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            List<Film> films = bookmarkManager.getAllBookmarks();

            mainHandler.post(() -> {
                bookmarkList.clear();
                if (films != null && !films.isEmpty()) {
                    bookmarkList.addAll(films);
                    showEmptyState(false);
                } else {
                    showEmptyState(true);
                }
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void removeBookmark(Film film, int position) {
        new Thread(() -> {
            bookmarkManager.removeBookmark(film.getId());
            mainHandler.post(() -> {
                bookmarkList.remove(position);
                adapter.notifyItemRemoved(position);
                showEmptyStateIfNeeded();
                Toast.makeText(getContext(), "Removed from bookmarks", 
                        Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvBookmarks.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvBookmarks.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showEmptyStateIfNeeded() {
        if (bookmarkList.isEmpty()) {
            showEmptyState(true);
        }
    }
}