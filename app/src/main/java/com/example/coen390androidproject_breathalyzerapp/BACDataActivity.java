package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BACDataActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerViewBACData;
    private DBHelper dbHelper;
    private int currentUserId;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    private ExecutorService executorService;
    private boolean isPaused = false;
    private OnBackPressedCallback onBackPressedCallback;

    // Handler and Runnable for auto-refresh
    private Handler refreshHandler = new Handler();
    private Runnable refreshRunnable;

    private static final long REFRESH_INTERVAL_MS = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bac_data);

        // Set the screen orientation to portrait
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Set up the RecyclerView for displaying BAC data
        recyclerViewBACData = findViewById(R.id.recyclerViewBACData);
        recyclerViewBACData.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the database helper
        dbHelper = new DBHelper(this);

        // Retrieve the current user ID from shared preferences
        currentUserId = sharedPreferences.getInt("currentUserId", -1);
        Log.d("BACDataActivity", "Attempting to fetch currentUserId from SharedPreferences.");

        // Initialize the executor service for background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Fetch BAC data for the current user
        fetchBACData(currentUserId);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("View RAW BAC Data");
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

        // Initialize and start the refresh runnable
        refreshRunnable = this::refreshBACData;
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);

        // Apply settings
        SettingsUtils.applySettings(this);
    }

    private void refreshBACData() {
        if (!isPaused) {
            fetchBACData(currentUserId);
            refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
        }
    }

    private void fetchBACData(int userId) {
        // Submit a task to the executor service to fetch BAC data in the background
        executorService.submit(() -> {
            // Check if the activity is not paused
            if (!isPaused) {
                // Retrieve the BAC data for the given user ID
                List<BACData> data = getBACData(userId);
                // Run the following code on the UI thread
                runOnUiThread(() -> {
                    // Check again if the activity is not paused
                    if (!isPaused) {
                        // Create a new adapter with the fetched data and set it to the RecyclerView
                        BACDataAdapter adapter = new BACDataAdapter(data, this); // Pass context to adapter
                        recyclerViewBACData.setAdapter(adapter);
                    }
                });
            }
        });
    }

    private List<BACData> getBACData(int userId) {
        List<BACData> data = new ArrayList<>();
        // Query the database for BAC records of the given user ID
        Cursor cursor = dbHelper.getBACRecords(userId);
        if (cursor != null && cursor.moveToFirst()) {
            // Iterate through the cursor and add each record to the list
            do {
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));
                double bac = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BAC_VALUE));
                data.add(new BACData(timestamp, bac));
            } while (cursor.moveToNext());
            // Close the cursor after use
            cursor.close();
        }
        return data;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        executorService.shutdownNow();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMenuItems();
        isPaused = false;
        executorService = Executors.newSingleThreadExecutor();
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);
        fetchBACData(currentUserId);
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
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

    private void updateMenuItems() {
        // Retrieve shared preferences
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        // Get the current user ID from shared preferences
        currentUserId = preferences.getInt("currentUserId", -1);
        // Check if the user is logged in
        boolean isLoggedIn = preferences.getBoolean("loggedIn", false);

        // Get the menu from the navigation view
        Menu menu = navigationView.getMenu();
        // Set the visibility of the manage account menu item based on the login status
        menu.findItem(R.id.nav_manage_account).setVisible(isLoggedIn);
        // Update the UI with the current user ID
        updateUI(currentUserId);
    }

    private void updateUI(int currentUserId) {
        // Get the account menu item from the navigation view
        MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
        // Check if the current user ID is valid or looged out
        if (currentUserId == -1) {
            // Set the title to "Account" if the user ID is invalid
            accountMenuItem.setTitle("Account");
        } else {
            // Query the database for the account with the given user ID
            Cursor cursor = dbHelper.getAccount(currentUserId);
            // Check if the cursor is not null and has at least one record
            if (cursor != null && cursor.moveToFirst()) {
                // Get the username from the cursor
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME));
                // Set the title of the account menu item to the username
                accountMenuItem.setTitle(username);
                // Close the cursor after use
                cursor.close();
            } else {
                // Set the title to "Account" if the account is not found
                accountMenuItem.setTitle("Account");
            }
        }
    }
}
