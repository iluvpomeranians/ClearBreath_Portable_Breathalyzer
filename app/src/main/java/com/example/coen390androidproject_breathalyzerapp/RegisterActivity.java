package com.example.coen390androidproject_breathalyzerapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextFullName, editTextUsername, editTextPassword, editTextConfirmPassword, editTextAge, editTextGender, editTextBMI;
    private Button btnRegister;
    private DBHelper dbHelper;
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

        btnRegister.setOnClickListener(v -> register());
        SettingsUtils.applySettings(this, editTextFullName, editTextUsername, editTextPassword, editTextConfirmPassword, editTextAge, editTextGender, editTextBMI, btnRegister);

        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        boolean loggedIn = preferences.getBoolean("loggedIn", false);
        currentUserId = preferences.getInt("currentUserId", -1);

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
        String gender = editTextGender.getText().toString().trim();
        int age = Integer.parseInt(editTextAge.getText().toString().trim());
        double bmi = Double.parseDouble(editTextBMI.getText().toString().trim());

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
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLoginState(boolean loggedIn, int userId) {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("loggedIn", loggedIn);
        editor.putInt("currentUserId", loggedIn ? userId : -1);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        boolean loggedIn = preferences.getBoolean("loggedIn", false);
        currentUserId = preferences.getInt("currentUserId", -1);
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
