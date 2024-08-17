package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import app.juky.squircleview.views.SquircleButton;


/// This class is to display the instructions of the device to the user.
public class MoreInfoActivity extends AppCompatActivity {

    private SquircleButton acknowledgeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info);

        // lock our app to portrait
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        acknowledgeButton = findViewById(R.id.acknowledge_button);

        acknowledgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBackToHome();
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToHome();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        SettingsUtils.applySettings(this, acknowledgeButton);
    }

    private void navigateBackToHome() {
        Intent intent = new Intent(MoreInfoActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
