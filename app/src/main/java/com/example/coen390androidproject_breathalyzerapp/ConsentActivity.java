package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import app.juky.squircleview.views.SquircleButton;

public class ConsentActivity extends AppCompatActivity {

    private TextView textViewConsent, textViewConsentDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);

        // Lock the app to portrait
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textViewConsent = findViewById(R.id.textViewConsent);
        textViewConsentDetails = findViewById(R.id.textViewConsentDetails);

        SquircleButton acknowledgeButton = findViewById(R.id.buttonAcknowledge);
        acknowledgeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ConsentActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        textViewConsent.setText("We require your consent to collect and use your personal information, such as your gender, age, and BMI. This information will be used solely for enhancing your experience within the application. By pressing 'I Acknowledge', you consent to provide this information.");

        // Apply settings to TextView and Button
        SettingsUtils.applySettings(this, textViewConsent, textViewConsentDetails, acknowledgeButton);
    }
}
