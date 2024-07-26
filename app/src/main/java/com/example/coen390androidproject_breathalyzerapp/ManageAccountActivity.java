package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class ManageAccountActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private EditText editTextUsername, editTextAge, editTextBMI;
    private Button buttonSaveChanges, buttonDeleteAccount;
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
            if (id == R.id.nav_home) {
                Intent intent = new Intent(ManageAccountActivity.this, HomeActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(ManageAccountActivity.this, SettingsActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_bac_data) {
                Intent intent = new Intent(ManageAccountActivity.this, BACDataActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_account) {
                Intent intent = new Intent(ManageAccountActivity.this, AccountActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                return true;
            }
            return false;
        });

        dbHelper = new DBHelper(this);
        currentUserId = getIntent().getIntExtra("currentUserId", -1);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextAge = findViewById(R.id.editTextAge);
        editTextBMI = findViewById(R.id.editTextBMI);

        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);

        if (currentUserId != -1) {
            Cursor cursor = dbHelper.getAccount(currentUserId);
            if (cursor != null && cursor.moveToFirst()) {
                editTextUsername.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME)));
                editTextAge.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_AGE))));
                editTextBMI.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BMI))));
                cursor.close();
            }
        }

        buttonSaveChanges.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            int age = Integer.parseInt(editTextAge.getText().toString().trim());
            double bmi = Double.parseDouble(editTextBMI.getText().toString().trim());

            boolean isUpdated = dbHelper.updateAccount(currentUserId, null, username, null, null, age, null, bmi);
            if (isUpdated) {
                Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManageAccountActivity.this, AccountActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to update account", Toast.LENGTH_SHORT).show();
            }
        });

        buttonDeleteAccount.setOnClickListener(v -> {
            boolean isDeleted = dbHelper.deleteAccount(currentUserId);
            if (isDeleted) {
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManageAccountActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
            }
        });

        SettingsUtils.applySettings(this, editTextUsername, editTextAge, editTextBMI, buttonSaveChanges, buttonDeleteAccount);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(ManageAccountActivity.this, AccountActivity.class);
            intent.putExtra("currentUserId", currentUserId);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
