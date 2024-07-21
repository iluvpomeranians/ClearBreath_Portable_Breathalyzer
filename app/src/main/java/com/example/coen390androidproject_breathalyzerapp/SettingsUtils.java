package com.example.coen390androidproject_breathalyzerapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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

        // Retrieving and applying toolbar color if there's a toolbar
        int toolbarColor = sharedPreferences.getInt("toolbar_color", Color.BLUE);
        if (context instanceof AppCompatActivity) {
            Toolbar toolbar = ((AppCompatActivity) context).findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setBackgroundColor(toolbarColor);
            }
        }
    }
}