package com.example.coen390androidproject_breathalyzerapp;


import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.content.Intent;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextFullName, editTextUsername, editTextPassword, editTextConfirmPassword, editTextAge, editTextGender, editTextBMI;
    private Button btnRegister;

    private DBHelper dbHelper;

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


        Button registerButton = findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(v -> {
            String fullName = editTextFullName.getText().toString().trim();
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();
            int age = Integer.parseInt(editTextAge.getText().toString().trim());
            boolean gender = editTextGender.getText().toString().trim().equalsIgnoreCase("Male");
            double bmi = Double.parseDouble(editTextBMI.getText().toString().trim());

            if (password.equals(confirmPassword)) {
                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMN_USERNAME, username);
                values.put(DBHelper.COLUMN_PASSWORD, password);
                values.put(DBHelper.COLUMN_FULL_NAME, fullName);
                values.put(DBHelper.COLUMN_AGE, age);
                values.put(DBHelper.COLUMN_GENDER, gender ? "Male" : "Female");
                values.put(DBHelper.COLUMN_BMI, bmi);

                /*long id = dbHelper.insertAccount(values);
                if (id != -1) {
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            }*/
            }

        });
        btnRegister.setOnClickListener(v -> register());
        SettingsUtils.applySettings(this, editTextFullName, editTextUsername, editTextPassword, editTextConfirmPassword, editTextAge, editTextGender, editTextBMI, btnRegister);
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
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, AccountActivity.class);
            intent.putExtra("currentUserId", (int) id);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(RegisterActivity.this, AccountActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}


