package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ConsentActivity extends AppCompatActivity {

    private TextView textViewConsent, textViewConsentDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);

        textViewConsent = findViewById(R.id.textViewConsent);
        textViewConsentDetails = findViewById(R.id.textViewConsentDetails);

        Button acknowledgeButton = findViewById(R.id.buttonAcknowledge);
        acknowledgeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ConsentActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        applySettings();

        textViewConsent.setText(String.format("We require your consent to collect and use your personal information, such as your gender, age and BMI. This information will be used solely for enhancing your experience within the application. By pressing 'I Acknowledge', you consent to provide this information."));
    }

    private void applySettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Retrieving and applying text size
        int textSize = sharedPreferences.getInt("text_size", 16);
        textViewConsent.setTextSize(textSize);
        textViewConsentDetails.setTextSize(textSize);

        // Retrieving and applying font type
        int fontIndex = sharedPreferences.getInt("font_index", 0);
        String font = SettingsActivity.getFonts()[fontIndex];
        Typeface typeface = Typeface.create(font, Typeface.NORMAL);
        textViewConsent.setTypeface(typeface);
        textViewConsentDetails.setTypeface(typeface);
    }
}