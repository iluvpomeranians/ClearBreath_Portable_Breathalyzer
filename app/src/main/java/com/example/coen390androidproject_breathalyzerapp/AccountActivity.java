package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class AccountActivity extends AppCompatActivity {

    private TextView tvUsername;
    private Button btnLogin;
    private Button btnRegister;
    private Button btnLogout;
    private Button btnDeleteAccount;

    private static final String SHARED_PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LOGGED_IN = "loggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        tvUsername = findViewById(R.id.tv_username);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnLogout = findViewById(R.id.btn_logout);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        boolean loggedIn = sharedPreferences.getBoolean(KEY_LOGGED_IN, false);

        if (loggedIn) {
            String username = sharedPreferences.getString(KEY_USERNAME, "Unknown User");
            tvUsername.setText("Welcome, " + username);
        } else {
            tvUsername.setText("Please log in or register");
        }

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_LOGGED_IN, false);
            editor.apply();
            tvUsername.setText("Please log in or register");
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(KEY_USERNAME);
            editor.remove(KEY_PASSWORD);
            editor.remove(KEY_BIRTHDAY);
            editor.remove(KEY_USER_ID);
            editor.putBoolean(KEY_LOGGED_IN, false);
            editor.apply();
            tvUsername.setText("Please log in or register");
            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
