package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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

public class ManageAccountActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private EditText editTextUsername, editTextAge, editTextBMI;
    private SquircleButton buttonSaveChanges, buttonDeleteAccount;
    private OnBackPressedCallback onBackPressedCallback;
    private DBHelper dbHelper;
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Manage Account");
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
                intent = new Intent(ManageAccountActivity.this, HomeActivity.class);
            } else if (id == R.id.nav_settings) {
                intent = new Intent(ManageAccountActivity.this, SettingsActivity.class);
            } else if (id == R.id.nav_bac_data) {
                intent = new Intent(ManageAccountActivity.this, BACDataActivity.class);
            } else if (id == R.id.nav_account) {
                intent = new Intent(ManageAccountActivity.this, AccountActivity.class);
            } else {
                return false;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("currentUserId", currentUserId);
            startActivity(intent);
            return true;
        });

        dbHelper = new DBHelper(this);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);
        Log.d("ManageAccountActivity", "onCreate currentUserId: " + currentUserId);

        updateMenuItems();

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextAge = findViewById(R.id.editTextAge);
        editTextBMI = findViewById(R.id.editTextBMI);

        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);

        loadAccountDetails(currentUserId);

        buttonSaveChanges.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String ageStr = editTextAge.getText().toString().trim();
            String bmiStr = editTextBMI.getText().toString().trim();

            // Validate username
            if (!username.matches("^[a-zA-Z0-9]+$")) {
                Toast.makeText(this, "Username can only contain alphanumeric characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate age
            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age < 18) {
                    Toast.makeText(this, "Age must be at least 18", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate BMI
            double bmi;
            try {
                bmi = Double.parseDouble(bmiStr);
                if (bmi <= 0 || bmi > 50) {
                    Toast.makeText(this, "Please enter a valid BMI (0 < BMI <= 50)", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid BMI", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update the user account in the database
            boolean isUpdated = dbHelper.updateAccount(currentUserId, null, username, null, null, age, null, bmi);
            if (isUpdated) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("currentUserId", currentUserId);
                editor.putString("username", username);
                editor.putInt("age", age);
                editor.putFloat("bmi", (float) bmi);
                editor.apply();
                updateMenuItems();
                Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show();
                loadAccountDetails(currentUserId);
            } else {
                Toast.makeText(this, "Failed to update account", Toast.LENGTH_SHORT).show();
            }
        });



        buttonDeleteAccount.setOnClickListener(v -> {
            boolean isDeleted = dbHelper.deleteAccount(currentUserId);
            if (isDeleted) {
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("loggedIn", false);
                editor.putInt("currentUserId", -1);
                editor.apply();
                Intent intent = new Intent(ManageAccountActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
            }
        });

        SettingsUtils.applySettings(this, editTextUsername, editTextAge, editTextBMI, buttonSaveChanges, buttonDeleteAccount);

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

    @Override
    protected void onResume() {
        super.onResume();
        updateMenuItems();
    }

    private void loadAccountDetails(int userId) {
        if (userId != -1) {
            Cursor cursor = dbHelper.getAccount(userId);
            if (cursor != null && cursor.moveToFirst()) {
                editTextUsername.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME)));
                editTextAge.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_AGE))));
                editTextBMI.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BMI))));
                cursor.close();
            }
        }
    }

    private void updateMenuItems() {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        currentUserId = preferences.getInt("currentUserId", -1);
        boolean isLoggedIn = preferences.getBoolean("loggedIn", false);

        Log.d("ManageAccountActivity", "updateMenuItems currentUserId: " + currentUserId);
        Log.d("ManageAccountActivity", "isLoggedIn: " + isLoggedIn);

        Menu menu = navigationView.getMenu();
        if (menu == null) {
            Log.e("ManageAccountActivity", "Menu is null");
            return;
        }

        menu.findItem(R.id.nav_manage_account).setVisible(isLoggedIn);
        updateUI(currentUserId);
    }

    private void updateUI(int currentUserId) {
        MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
        if (accountMenuItem == null) {
            Log.e("ManageAccountActivity", "accountMenuItem is null");
            return;
        }

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBackToHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateBackToHome() {
        Intent intent = new Intent(ManageAccountActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("currentUserId", currentUserId);
        startActivity(intent);
        finish();
    }
}
