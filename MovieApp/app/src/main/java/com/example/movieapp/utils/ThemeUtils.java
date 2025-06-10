package com.example.movieapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.movieapp.R;

public class ThemeUtils {

    // Update the icon based on current theme
    public static void updateThemeIcon(ImageView imageView, Context context) {
        if (imageView != null) {
            int currentNightMode = context.getResources().getConfiguration().uiMode &
                    Configuration.UI_MODE_NIGHT_MASK;

            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                // In dark mode, show light mode icon
                imageView.setImageResource(R.drawable.ic_light_mode);
            } else {
                // In light mode, show dark mode icon
                imageView.setImageResource(R.drawable.ic_dark_mode);
            }
        }
    }

    // Toggle theme
    public static void toggleTheme(Context context) {
        // Get current theme mode
        int currentNightMode = context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;

        // Save the preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // Currently in dark mode, switch to light
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            editor.putInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            // Currently in light mode, switch to dark
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            editor.putInt("theme_mode", AppCompatDelegate.MODE_NIGHT_YES);
        }
        editor.apply();

        // Recreate activity with state preservation
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.recreate();
        }
    }
}