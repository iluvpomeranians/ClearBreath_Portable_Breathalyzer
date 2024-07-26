package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class AccountHistoryActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private RecyclerView recyclerView;
    private AccountHistoryAdapter adapter;
    private DBHelper dbHelper;
    private int currentUserId;
    private TextView textViewBACData;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Account History");
        }

        textViewBACData = findViewById(R.id.textViewBACData);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                Intent intent = new Intent(AccountHistoryActivity.this, HomeActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(AccountHistoryActivity.this, SettingsActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_manage_account) {
                Intent intent = new Intent(AccountHistoryActivity.this, ManageAccountActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_account) {
                Intent intent = new Intent(AccountHistoryActivity.this, AccountActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void displayBACData() {
        List<BACRecord> bacRecordList = new ArrayList<>();
        Cursor cursor = dbHelper.getBACRecords(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder data = new StringBuilder();
            do {
                double bac = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BAC_VALUE));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));
                bacRecordList.add(new BACRecord(bac, timestamp));
                data.append("Timestamp: ").append(timestamp).append(", BAC: ").append(bac).append("\n");
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter = new AccountHistoryAdapter(bacRecordList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(AccountHistoryActivity.this, StartRecordingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
