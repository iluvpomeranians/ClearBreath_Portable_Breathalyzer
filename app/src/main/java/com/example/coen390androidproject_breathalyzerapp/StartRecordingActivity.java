package com.example.coen390androidproject_breathalyzerapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class StartRecordingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView textViewBlow;
    private Button buttonStartRecording;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_recording);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progressBar = findViewById(R.id.progressBar);
        textViewBlow = findViewById(R.id.textView_blow);
        buttonStartRecording = findViewById(R.id.button_start_recording);

        buttonStartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                textViewBlow.setVisibility(View.VISIBLE);
                buttonStartRecording.setEnabled(false);
                startProgressBar();
            }
        });
        SettingsUtils.applySettings(this, textViewBlow, buttonStartRecording);
    }

    private void startProgressBar() {
        // Reset progress status
        progressStatus = 0;
        progressBar.setProgress(progressStatus);

        // Start long running operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;

                    // Update the progress bar and display the current value
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        // Sleep for 150 milliseconds to simulate the progress
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // When the progress is completed
                handler.post(new Runnable() {
                    public void run() {
                        textViewBlow.setVisibility(View.GONE);
                        buttonStartRecording.setEnabled(true);
                    }
                });
            }
        }).start();
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
