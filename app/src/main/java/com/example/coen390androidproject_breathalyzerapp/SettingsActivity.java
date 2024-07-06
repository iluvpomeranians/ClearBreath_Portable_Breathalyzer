package com.example.coen390androidproject_breathalyzerapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar textSizeSeekBar;
    private Spinner fontSpinner;
    private TextView sampleTextView;
    private GridLayout colorGrid;
    private Toolbar toolbar;

    private SharedPreferences sharedPreferences;

    private static final String[] FONTS = {
            "sans-serif", "sans-serif-light", "sans-serif-condensed", "serif", "monospace"
    };

    private static final int[] COLORS = {
            R.color.color1, R.color.color2, R.color.color3, R.color.color4,
            R.color.color5, R.color.color6, R.color.color7, R.color.color8,
            R.color.color9, R.color.color10, R.color.color11, R.color.color12
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        textSizeSeekBar = findViewById(R.id.text_size_seekbar);
        fontSpinner = findViewById(R.id.font_spinner);
        sampleTextView = findViewById(R.id.sample_text_view);
        colorGrid = findViewById(R.id.color_grid);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setupTextSizeSeekBar();
        setupFontSpinner();
        setupColorGrid();
    }

    private void setupTextSizeSeekBar() {
        textSizeSeekBar.setMax(50);
        textSizeSeekBar.setProgress(sharedPreferences.getInt("text_size", 16));
        sampleTextView.setTextSize(textSizeSeekBar.getProgress());

        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sampleTextView.setTextSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sharedPreferences.edit().putInt("text_size", seekBar.getProgress()).apply();
            }
        });
    }

    private void setupFontSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, FONTS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSpinner.setAdapter(adapter);

        fontSpinner.setSelection(sharedPreferences.getInt("font_index", 0));
        sampleTextView.setTypeface(Typeface.create(FONTS[fontSpinner.getSelectedItemPosition()], Typeface.NORMAL));

        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sampleTextView.setTypeface(Typeface.create(FONTS[position], Typeface.NORMAL));
                sharedPreferences.edit().putInt("font_index", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupColorGrid() {
        int savedColor = sharedPreferences.getInt("toolbar_color", Color.BLACK);
        toolbar.setBackgroundColor(savedColor);

        for (int colorRes : COLORS) {
            View colorView = new View(this);
            colorView.setBackgroundColor(getResources().getColor(colorRes));
            colorView.setLayoutParams(new GridLayout.LayoutParams());
            colorView.setMinimumWidth(100);
            colorView.setMinimumHeight(100);

            colorView.setOnClickListener(v -> {
                int color = getResources().getColor(colorRes);
                toolbar.setBackgroundColor(color);
                sharedPreferences.edit().putInt("toolbar_color", color).apply();
            });

            colorGrid.addView(colorView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
