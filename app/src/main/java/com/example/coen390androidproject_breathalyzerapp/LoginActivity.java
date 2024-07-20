package com.example.coen390androidproject_breathalyzerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LoginActivity extends AppCompatActivity {

    private EditText et_username, et_password;
    private Button btn_login;
    private DBHelper dbHelper;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String KEY_USER_ID = "userId";
    private Toolbar toolbar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        toolbar = findViewById(R.id.toolbar);

        if (et_username == null || et_password == null || btn_login == null || toolbar == null) {
            throw new NullPointerException("One or more view elements are not properly initialized.");
        }

        // Set up toolbar
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Login page");
        }

        // Set up button click listener
        btn_login.setOnClickListener(v -> {
            String username = et_username.getText().toString().trim();
            String password = et_password.getText().toString().trim();

            if (checkCredentials(username, password)) {
                Cursor cursor = dbHelper.getAccount(username, password);
                if (cursor != null && cursor.moveToFirst()) {
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID));
                    saveUserId(userId);
                    cursor.close();
                }

                Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
                intent.putExtra("USER_ID", username); // Verify this
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        // Apply settings after views are initialized
        applySettings();
    }

    private boolean checkCredentials(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.ACCOUNT_TABLE, null, DBHelper.COLUMN_USERNAME + "=? AND " + DBHelper.COLUMN_PASSWORD + "=?", new String[]{username, password}, null, null, null);

        boolean isValid = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return isValid;
    }

    private void saveUserId(int userId) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    private void applySettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Apply toolbar color
        int toolbarColor = preferences.getInt("toolbar_color", Color.BLUE);
        toolbar.setBackgroundColor(toolbarColor);

        // Apply font size and type
        String fontSizePref = preferences.getString("font_size", "16"); // Default font size 16
        String fontTypePref = preferences.getString("font_type", "sans"); // Default font type sans

        try {
            int fontSize = Integer.parseInt(fontSizePref);
            Typeface typeface;
            switch (fontTypePref) {
                case "serif":
                    typeface = Typeface.SERIF;
                    break;
                case "monospace":
                    typeface = Typeface.MONOSPACE;
                    break;
                default:
                    typeface = Typeface.SANS_SERIF;
                    break;
            }

            if (et_username != null && et_password != null) {
                et_username.setTextSize(fontSize);
                et_username.setTypeface(typeface);
                et_password.setTextSize(fontSize);
                et_password.setTypeface(typeface);
            } else {
                throw new NullPointerException("EditText elements are not properly initialized.");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}











    /*
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button btnLogin;

    private static final String SHARED_PREFS_NAME = "UserPrefs";
    private static final String KEY_ACCOUNTS = "accounts";
    private static final String KEY_CURRENT_USER = "current_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Login");
        }

        editTextUsername = findViewById(R.id.et_username);
        editTextPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        String savedPassword = sharedPreferences.getString(username, null);

        if (password.equals(savedPassword)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CURRENT_USER, username);
            editor.apply();
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

