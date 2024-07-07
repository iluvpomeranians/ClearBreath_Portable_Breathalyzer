package com.example.coen390androidproject_breathalyzerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextBirthday;
    private Button btnRegister;

    private static final String SHARED_PREFS_NAME = "UserPrefs";
    private static final String KEY_ACCOUNTS = "accounts";
    private static final String KEY_CURRENT_USER = "current_user";

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

        editTextUsername = findViewById(R.id.et_username);
        editTextPassword = findViewById(R.id.et_password);
        editTextBirthday = findViewById(R.id.et_birthday);
        btnRegister = findViewById(R.id.btn_register);

        editTextBirthday.setOnClickListener(v -> showDatePickerDialog());

        btnRegister.setOnClickListener(v -> register());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
            editTextBirthday.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

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
}
