package com.example.coen390androidproject_breathalyzerapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BACDataActivity extends AppCompatActivity {

    private TextView textViewBACData;
    private DBHelper dbHelper;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bac_data);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewBACData = findViewById(R.id.textViewBACData);
        dbHelper = new DBHelper(this);
        currentUserId = getIntent().getIntExtra("currentUserId", -1);

        displayBACData();
        SettingsUtils.applySettings(this, textViewBACData);
    }

    private void displayBACData() {
        Cursor cursor = dbHelper.getBACRecords(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder data = new StringBuilder();
            do {
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));
                double bac = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BAC));
                data.append("Timestamp: ").append(timestamp).append(", BAC: ").append(bac).append("\n");
            } while (cursor.moveToNext());
            textViewBACData.setText(data.toString());
            cursor.close();
        }
    }
}
