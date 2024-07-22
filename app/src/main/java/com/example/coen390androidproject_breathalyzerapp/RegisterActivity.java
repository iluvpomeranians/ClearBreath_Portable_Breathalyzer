package com.example.coen390androidproject_breathalyzerapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextFullName, editTextUsername, editTextPassword, editTextConfirmPassword, editTextAge, editTextGender, editTextBMI;
    private Button btnRegister;
    private DBHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int currentUserId = -1;
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextAge = findViewById(R.id.editTextAge);
        editTextGender = findViewById(R.id.editTextGender);
        editTextBMI = findViewById(R.id.editTextBMI);
        btnRegister = findViewById(R.id.buttonRegister);

        dbHelper = new DBHelper(this);
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE); // Initialize sharedPreferences here

        btnRegister.setOnClickListener(v -> register());
        SettingsUtils.applySettings(this, editTextFullName, editTextUsername, editTextPassword, editTextConfirmPassword, editTextAge, editTextGender, editTextBMI, btnRegister);

        updateMenuItems();

        boolean loggedIn = sharedPreferences.getBoolean("loggedIn", false);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void register() {
        String fullName = editTextFullName.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String gender = editTextGender.getText().toString().trim();
        String bmiStr = editTextBMI.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                ageStr.isEmpty() || gender.isEmpty() || bmiStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
            return;
        }

        double bmi;
        try {
            bmi = Double.parseDouble(bmiStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid BMI", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = dbHelper.insertAccount(fullName, username, password, gender, age, null, bmi);
        if (id > 0) {
            saveLoginState(true, (int) id);
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, AccountActivity.class);
            intent.putExtra("currentUserId", (int) id);
            updateMenuItems();
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLoginState(boolean loggedIn, int userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("loggedIn", loggedIn);
        editor.putInt("currentUserId", loggedIn ? userId : -1);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean loggedIn = sharedPreferences.getBoolean("loggedIn", false);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);
        updateMenuItems();
    }

    private void updateMenuItems() {
        boolean isLoggedIn = sharedPreferences.getBoolean("loggedIn", false);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            Menu menu = navigationView.getMenu();
            if (menu != null) {
                menu.findItem(R.id.nav_manage_account).setVisible(isLoggedIn);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            moveTaskToBack(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
