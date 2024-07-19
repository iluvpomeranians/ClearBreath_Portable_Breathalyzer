package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ConsentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);

        Button acknowledgeButton = findViewById(R.id.buttonAcknowledge);
        acknowledgeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ConsentActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}