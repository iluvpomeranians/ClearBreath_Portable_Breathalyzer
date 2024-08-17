package com.example.coen390androidproject_breathalyzerapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
/// Class for settings to apply to the app
public class SettingsUtils {

    public static void applySettings(Context context, TextView... textViews) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieving and apply text size
        int textSize = sharedPreferences.getInt("text_size", 16);
        for (TextView textView : textViews) {
            textView.setTextSize(textSize);
        }

        // Retrieving and apply font type
        int fontIndex = sharedPreferences.getInt("font_index", 0);
        String font = SettingsActivity.getFonts()[fontIndex];
        Typeface typeface = Typeface.create(font, Typeface.NORMAL);
        for (TextView textView : textViews) {
            textView.setTypeface(typeface);
        }

        // Retrieving and apply toolbar color
        int toolbarColor = sharedPreferences.getInt("toolbar_color", Color.BLUE);
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setBackgroundColor(toolbarColor);
            }
            // Apply status bar color
            setStatusBarColor(activity, toolbarColor);
        }
    }

    public static void setStatusBarColor(AppCompatActivity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.getWindow().setStatusBarColor(color);
        }
    }
}
