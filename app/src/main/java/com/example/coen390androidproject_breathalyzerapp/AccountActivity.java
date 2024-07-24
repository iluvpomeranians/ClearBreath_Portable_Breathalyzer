package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class AccountActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView textViewWelcome;
    private Button btnLogin, btnRegister;
    private NavigationView navigationView;
    private DBHelper dbHelper;
    private Button btnLogout;
    private int currentUserId = -1;
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Account Management");
        }

        dbHelper = new DBHelper(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.nav_home) {
                intent = new Intent(AccountActivity.this, HomeActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                intent = new Intent(AccountActivity.this, SettingsActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                intent = new Intent(AccountActivity.this, SettingsActivity.class);
            } else if (id == R.id.nav_manage_account) {
                intent = new Intent(AccountActivity.this, ManageAccountActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_bac_data) {
                intent = new Intent(AccountActivity.this, BACDataActivity.class);
                intent.putExtra("currentUserId", currentUserId);
            }
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        textViewWelcome = findViewById(R.id.textViewWelcome);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnLogout = findViewById(R.id.btn_logout);

        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        boolean loggedIn = preferences.getBoolean("loggedIn", false);
        currentUserId = preferences.getInt("currentUserId", -1);
        Log.d("AccountACT:", "currentUserId onCreate: " + currentUserId); // Debug log

        updateUI(currentUserId);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, ConsentActivity.class);
            startActivity(intent);
        });

        SettingsUtils.applySettings(this, textViewWelcome);

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
                    moveTaskToBack(true);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void saveLoginState(boolean loggedIn) {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("loggedIn", loggedIn);
        editor.putInt("currentUserId", loggedIn ? currentUserId : -1);
        editor.apply();
    }

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

    private void updateUI(int currentUserId) {
        if (currentUserId == -1) {
            Log.d("AccountACT:", "currentUserId does not exist!");
            textViewWelcome.setText("Welcome");
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);

            MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
            accountMenuItem.setTitle("Account");
        } else {
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
