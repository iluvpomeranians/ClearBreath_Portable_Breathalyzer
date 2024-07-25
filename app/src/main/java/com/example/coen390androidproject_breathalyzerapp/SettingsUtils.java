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

public class SettingsUtils {

    public static void applySettings(Context context, TextView... textViews) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieving and applying text size
        int textSize = sharedPreferences.getInt("text_size", 16);
        for (TextView textView : textViews) {
            textView.setTextSize(textSize);
        }

        // Retrieving and applying font type
        int fontIndex = sharedPreferences.getInt("font_index", 0);
        String font = SettingsActivity.getFonts()[fontIndex];
        Typeface typeface = Typeface.create(font, Typeface.NORMAL);
        for (TextView textView : textViews) {
            textView.setTypeface(typeface);
        }

        // Retrieving and applying toolbar color to the status bar so it matches the toolbar colour
        int toolbarColor = sharedPreferences.getInt("toolbar_color", Color.BLUE);
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setBackgroundColor(toolbarColor);
                // Set the status bar color to the same as the toolbar color with for API33 (Android 13)
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    activity.getWindow().setStatusBarColor(toolbarColor);
                }
            }
        }
    }
}
