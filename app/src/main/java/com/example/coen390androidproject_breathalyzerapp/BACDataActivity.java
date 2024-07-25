package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class BACDataActivity extends AppCompatActivity {

    private TextView textViewBACData;
    private DBHelper dbHelper;
    private int currentUserId;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

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

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(BACDataActivity.this, HomeActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(BACDataActivity.this, SettingsActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_manage_account) {
                Intent intent = new Intent(BACDataActivity.this, ManageAccountActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_account) {
                Intent intent = new Intent(BACDataActivity.this, AccountActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            }
            return false;
        });

    }

    private void displayBACData() {
        Cursor cursor = dbHelper.getBACRecords(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder data = new StringBuilder();
            do {
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));
                double bac = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BAC_VALUE));
                data.append("Timestamp: ").append(timestamp).append(", BAC: ").append(bac).append("\n");
            } while (cursor.moveToNext());
            textViewBACData.setText(data.toString());
            cursor.close();
        }
    }
}
