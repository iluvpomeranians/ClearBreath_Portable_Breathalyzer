package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BACDataActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView textViewBACData;
    private DBHelper dbHelper;
    private int currentUserId;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    private ExecutorService executorService;
    private boolean isPaused = false;
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bac_data);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);

        textViewBACData = findViewById(R.id.textViewBACData);
        dbHelper = new DBHelper(this);
        currentUserId = getIntent().getIntExtra("currentUserId", -1);

        executorService = Executors.newSingleThreadExecutor();

        fetchBACData(currentUserId);

        SettingsUtils.applySettings(this, textViewBACData);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("View RAW BAC Data - Account Details");
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent;
            if (id == R.id.nav_home) {
                intent = new Intent(BACDataActivity.this, HomeActivity.class);
            } else if (id == R.id.nav_settings) {
                intent = new Intent(BACDataActivity.this, SettingsActivity.class);
            } else if (id == R.id.nav_manage_account) {
                intent = new Intent(BACDataActivity.this, ManageAccountActivity.class);
            } else if (id == R.id.nav_account) {
                intent = new Intent(BACDataActivity.this, AccountActivity.class);
            } else {
                return false;
            }
            intent.putExtra("currentUserId", currentUserId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return true;
        });

        updateMenuItems();

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    navigateBackToHome();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

    }

    private void fetchBACData(int userId) {
        executorService.submit(() -> {
            if (!isPaused) {
                String data = getBACData(userId);
                runOnUiThread(() -> {
                    if (!isPaused) {
                        textViewBACData.setText(data);
                    }
                });
            }
        });
    }

    private String getBACData(int userId) {
        StringBuilder data = new StringBuilder();
        Cursor cursor = dbHelper.getBACRecords(userId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));
                double bac = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BAC_VALUE));
                data.append("Timestamp: ").append(timestamp).append(", BAC: ").append(bac).append("\n");
            } while (cursor.moveToNext());
            cursor.close();
        }
        return data.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        executorService.shutdownNow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
        executorService = Executors.newSingleThreadExecutor();
        currentUserId = sharedPreferences.getInt("currentUserId", -1);
        fetchBACData(currentUserId);
        updateMenuItems();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void navigateBackToHome() {
        Intent intent = new Intent(BACDataActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void updateUI(int currentUserId) {
        MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
        if (currentUserId == -1) {
            accountMenuItem.setTitle("Account");
        } else {
            Cursor cursor = dbHelper.getAccount(currentUserId);
            if (cursor != null && cursor.moveToFirst()) {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME));
                accountMenuItem.setTitle(username);
                cursor.close();
            } else {
                accountMenuItem.setTitle("Account");
            }
        }
    }

    private void updateMenuItems() {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("loggedIn", false);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_manage_account).setVisible(isLoggedIn);
        updateUI(currentUserId);
    }
}