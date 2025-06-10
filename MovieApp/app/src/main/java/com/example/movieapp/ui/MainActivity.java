package com.example.movieapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.movieapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        loadFragment(new HomeFragment());

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            bottomNavigationView.getMenu().findItem(R.id.nav_home).setIcon(R.drawable.ic_home_outline);
            bottomNavigationView.getMenu().findItem(R.id.nav_search).setIcon(R.drawable.ic_search_outline);
            bottomNavigationView.getMenu().findItem(R.id.nav_wishlist).setIcon(R.drawable.ic_wishlist_outline);

            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.nav_home) {
                item.setIcon(R.drawable.ic_home_solid);
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                item.setIcon(R.drawable.ic_search_solid);
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.nav_wishlist) {
                item.setIcon(R.drawable.ic_wishlist_solid);
                selectedFragment = new BookmarkFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}