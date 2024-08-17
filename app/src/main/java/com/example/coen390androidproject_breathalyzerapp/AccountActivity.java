package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import app.juky.squircleview.views.SquircleButton;

public class AccountActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView textViewWelcome;
    private SquircleButton btnLogin, btnRegister;
    private NavigationView navigationView;
    private DBHelper dbHelper;
    private SquircleButton btnLogout;
    private int currentUserId = -1;
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // lock our app to portrait
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set up the toolbar, this code should be in every page
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable home button and set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Account Management");
        }

        dbHelper = new DBHelper(this);

        // Seting up the navigation drawer and creating the mapping to other pages
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent;
            if (id == R.id.nav_home) {
                intent = new Intent(AccountActivity.this, HomeActivity.class);
                intent.putExtra("currentUserId", currentUserId);
            } else if (id == R.id.nav_settings) {
                intent = new Intent(AccountActivity.this, SettingsActivity.class);
                intent.putExtra("currentUserId", currentUserId);
            } else if (id == R.id.nav_manage_account) {
                intent = new Intent(AccountActivity.this, ManageAccountActivity.class);
                intent.putExtra("currentUserId", currentUserId);
            } else if (id == R.id.nav_bac_data) {
                intent = new Intent(AccountActivity.this, BACDataActivity.class);
                intent.putExtra("currentUserId", currentUserId);
            } else {
                return false;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return true;
        });

        textViewWelcome = findViewById(R.id.textViewWelcome);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnLogout = findViewById(R.id.btn_logout);

        // Retrieve login state and current user ID from SharedPreferences to update UI account name and use/map correct data
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        boolean loggedIn = preferences.getBoolean("loggedIn", false);
        currentUserId = preferences.getInt("currentUserId", -1);
        Log.d("AccountACT:", "currentUserId onCreate: " + currentUserId); // Debug log

        // Update UI based on current user ID and checks if user is logged in through the id number later on
        updateUI(currentUserId);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, ConsentActivity.class);
            startActivity(intent);
        });

        // Apply settings to the UI components, should be in every page
        SettingsUtils.applySettings(this, textViewWelcome, btnLogout, btnRegister, btnLogin);

        btnLogout.setOnClickListener(v -> {
            currentUserId = -1;
            saveLoginState(false);
            Toast.makeText(AccountActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            updateUI(currentUserId);
            updateMenuItems(); // Update menu items on logout
        });

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

    private void navigateBackToHome() {
        Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // Save login state to SharedPreferences
    private void saveLoginState(boolean loggedIn) {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("loggedIn", loggedIn);
        editor.putInt("currentUserId", loggedIn ? currentUserId : -1);
        editor.apply();
    }

    // Update menu items based on login state
    private void updateMenuItems() {
        boolean isLoggedIn = getSharedPreferences("UserPreferences", MODE_PRIVATE).getBoolean("loggedIn", false);
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_manage_account).setVisible(isLoggedIn);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retrieve login state and current user ID from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        boolean loggedIn = preferences.getBoolean("loggedIn", false);
        currentUserId = preferences.getInt("currentUserId", -1);
        Log.d("AccountACT:", "currentUserId onResume: " + currentUserId);
        updateUI(currentUserId);
        updateMenuItems(); // Update menu items on resume
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (onBackPressedCallback != null && onBackPressedCallback.isEnabled()) {
                onBackPressedCallback.handleOnBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // Update UI based on current user ID
    private void updateUI(int currentUserId) {
        if (currentUserId == -1) { //if true, either they just opened the app/page or logged out
            Log.d("AccountACT:", "currentUserId does not exist!");
            textViewWelcome.setText("Welcome to ClearBreath!");
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);

            MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
            accountMenuItem.setTitle("Account");
        } else {
            //coming here would mean they are logged in, we update UI and show the correct buttons
            Cursor cursor = dbHelper.getAccount(currentUserId);
            if (cursor != null && cursor.moveToFirst()) {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME));
                textViewWelcome.setText("Welcome, " + username);
                btnLogin.setVisibility(View.GONE);
                btnRegister.setVisibility(View.GONE);
                btnLogout.setVisibility(View.VISIBLE);

                MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
                accountMenuItem.setTitle(username);
                cursor.close();
            } else {
                //error that shouldnt happen.
                Log.d("AccountACT:", "User not found in DB!");
                textViewWelcome.setText("Welcome");
                btnLogin.setVisibility(View.VISIBLE);
                btnRegister.setVisibility(View.VISIBLE);
                btnLogout.setVisibility(View.GONE);

                MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
                accountMenuItem.setTitle("Account");
            }
        }
    }
}
