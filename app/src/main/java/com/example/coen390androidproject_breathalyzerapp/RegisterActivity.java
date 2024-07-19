package com.example.coen390androidproject_breathalyzerapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameEditText, usernameEditText, passwordEditText, confirmPasswordEditText, ageEditText, genderEditText, bmiEditText;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Register");
        }

        // Initialize DBHelper
        dbHelper = new DBHelper(this);

        fullNameEditText = findViewById(R.id.editTextFullName);
        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        ageEditText = findViewById(R.id.editTextAge);
        genderEditText = findViewById(R.id.editTextGender);
        bmiEditText = findViewById(R.id.editTextBMI);

        Button registerButton = findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(v -> {
            String fullName = fullNameEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            int age = Integer.parseInt(ageEditText.getText().toString().trim());
            boolean gender = genderEditText.getText().toString().trim().equalsIgnoreCase("Male");
            double bmi = Double.parseDouble(bmiEditText.getText().toString().trim());

            if (password.equals(confirmPassword)) {
                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMN_USERNAME, username);
                values.put(DBHelper.COLUMN_PASSWORD, password);
                values.put(DBHelper.COLUMN_FULL_NAME, fullName);
                values.put(DBHelper.COLUMN_AGE, age);
                values.put(DBHelper.COLUMN_GENDER, gender ? "Male" : "Female");
                values.put(DBHelper.COLUMN_BMI, bmi);

                long id = dbHelper.insertAccount(values);
                if (id != -1) {
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

/*
    private void register() {
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        String birthday = editTextBirthday.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> accounts = sharedPreferences.getStringSet(KEY_ACCOUNTS, new HashSet<>());
        accounts.add(username);
        editor.putStringSet(KEY_ACCOUNTS, accounts);
        editor.putString(username, password);
        editor.putString("birthday_" + username, birthday);
        editor.putString("id_" + username, UUID.randomUUID().toString());
        editor.putString(KEY_CURRENT_USER, username);
        editor.apply();

        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this, AccountActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(RegisterActivity.this, AccountActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

