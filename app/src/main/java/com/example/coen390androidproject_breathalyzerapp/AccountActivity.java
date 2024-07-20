package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.Set;

public class AccountActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView textViewWelcome;
    private Button btnLogin, btnRegister, btnLogout, btnDeleteAccount;
    private NavigationView navigationView;
    private static final String SHARED_PREFS_NAME = "UserPrefs";
    private static final String KEY_ACCOUNTS = "accounts";
    private static final String KEY_CURRENT_USER = "current_user";

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

        drawerLayout = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        textViewWelcome = findViewById(R.id.textViewWelcome);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnLogout = findViewById(R.id.btn_logout);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        String currentUser = sharedPreferences.getString(KEY_CURRENT_USER, null);
        updateUI(currentUser);

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
            editor.remove(KEY_CURRENT_USER);
            editor.apply();
            Toast.makeText(AccountActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            updateUI(null);
        });

        btnDeleteAccount.setOnClickListener(v -> {
            String username = sharedPreferences.getString(KEY_CURRENT_USER, null);
            if (username != null) {
                ConfirmDeleteDialogFragment confirmDeleteDialogFragment = ConfirmDeleteDialogFragment.newInstance(username);
                confirmDeleteDialogFragment.show(getSupportFragmentManager(), "confirmDelete");
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToHome();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void navigateBackToHome() {
        Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBackToHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void deleteAccount(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        Set<String> accounts = sharedPreferences.getStringSet(KEY_ACCOUNTS, new HashSet<>());
        accounts.remove(username);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_ACCOUNTS, accounts);
        editor.remove(username);
        editor.remove("birthday_" + username);
        editor.remove("id_" + username);
        editor.remove(KEY_CURRENT_USER);
        editor.apply();
        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
        updateUI(null);
    }

    public void updateUI(String currentUser) {
        if (currentUser == null) {
            textViewWelcome.setText("Welcome");
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
            btnDeleteAccount.setVisibility(View.GONE);

            MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
            accountMenuItem.setTitle("Account");
        } else {
            textViewWelcome.setText("Welcome, " + currentUser);
            btnLogin.setVisibility(View.GONE);
            btnRegister.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
            btnDeleteAccount.setVisibility(View.VISIBLE);

            MenuItem accountMenuItem = navigationView.getMenu().findItem(R.id.nav_account);
            accountMenuItem.setTitle(currentUser);
        }
    }
}
