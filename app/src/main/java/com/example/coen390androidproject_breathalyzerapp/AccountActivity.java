package com.example.coen390androidproject_breathalyzerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Map;
import java.util.UUID;

public class AccountActivity extends AppCompatActivity {

    private static final String SHARED_PREFS_NAME = "UserPrefs";
    private static final String KEY_LOGGED_IN = "loggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_ID = "id";

    private TextView textViewWelcome;
    private Button btnLogin;
    private Button btnRegister;
    private Button btnLogout;
    private Button btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Account");
        }

        textViewWelcome = findViewById(R.id.textViewWelcome);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnLogout = findViewById(R.id.btn_logout);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        boolean loggedIn = sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
        String username = sharedPreferences.getString(KEY_USERNAME, "");

        if (loggedIn) {
            textViewWelcome.setText("Welcome, " + username);
            showLoggedInState();
        } else {
            showLoggedOutState();
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
            showLoggedOutState();
            Intent intent = new Intent(AccountActivity.this, AccountActivity.class);
            startActivity(intent);
        });

        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog(username));
    }

    private void showDeleteAccountDialog(String username) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete account " + username + "?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteAccount(username))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteAccount(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PASSWORD);
        editor.remove(KEY_BIRTHDAY);
        editor.remove(KEY_ID);
        editor.putBoolean(KEY_LOGGED_IN, false);
        editor.apply();
        showLoggedOutState();
        Intent intent = new Intent(AccountActivity.this, AccountActivity.class);
        startActivity(intent);
    }

    private void showLoggedInState() {
        btnLogin.setVisibility(View.GONE);
        btnRegister.setVisibility(View.GONE);
        btnLogout.setVisibility(View.VISIBLE);
        btnDeleteAccount.setVisibility(View.VISIBLE);
    }

    private void showLoggedOutState() {
        btnLogin.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.GONE);
        btnDeleteAccount.setVisibility(View.GONE);
        textViewWelcome.setText("Please log in or register");
    }
}
