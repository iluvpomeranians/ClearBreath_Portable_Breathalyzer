package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.Set;

public class AccountActivity extends AppCompatActivity {


    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView textViewWelcome;
    private Button btnLogin, btnRegister;
    private NavigationView navigationView;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String KEY_USER_ID = "userId";
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Account Management");
        }
        dbHelper = new DBHelper(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(AccountActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                else if (id == R.id.nav_manage_account)
                {
                    Intent intent = new Intent(AccountActivity.this, ManageAccountActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
        });

        textViewWelcome = findViewById(R.id.textViewWelcome);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);


        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String currentUser = sharedPreferences.getString(KEY_USER_ID, null);
        updateUI(currentUser);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, ConsentActivity.class);
            startActivity(intent);
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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
