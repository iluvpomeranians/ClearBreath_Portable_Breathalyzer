package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MoreInfoActivity extends AppCompatActivity {

    private Button acknowledgeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info);

        acknowledgeButton = findViewById(R.id.acknowledge_button);

        acknowledgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to HomeActivity
                Intent intent = new Intent(MoreInfoActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}