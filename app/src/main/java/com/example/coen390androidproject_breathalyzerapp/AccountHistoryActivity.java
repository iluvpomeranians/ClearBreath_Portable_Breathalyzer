package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

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
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private DBHelper dbHelper;
    private int currentUserId;

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

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        currentUserId = getIntent().getIntExtra("currentUserId", -1);

        loadAccountHistory();


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToHome();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        SettingsUtils.applySettings(this);
    }

    private void loadAccountHistory() {
        List<BACRecord> bacRecordList = new ArrayList<>();
        Cursor cursor = dbHelper.getBACRecords(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));
                double bac = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BAC_VALUE));
                bacRecordList.add(new BACRecord(bac, timestamp));
            } while (cursor.moveToNext());
            cursor.close();
        }

        AccountHistoryAdapter adapter = new AccountHistoryAdapter(bacRecordList);
        recyclerView.setAdapter(adapter);
    }

    private void navigateBackToHome() {
        Intent intent = new Intent(AccountHistoryActivity.this, StartRecordingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBackToHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
