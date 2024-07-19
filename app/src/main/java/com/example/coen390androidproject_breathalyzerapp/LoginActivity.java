package com.example.coen390androidproject_breathalyzerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashSet;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private EditText et_username, et_password;
    private Button btn_login, buttonRegister;
    private DBHelper dbHelper;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String KEY_USER_ID = "userId";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);

        et_username = findViewById(R.id.editTextUsername);
        et_password = findViewById(R.id.editTextPassword);
        btn_login = findViewById(R.id.btn_login);
        buttonRegister = findViewById(R.id.buttonRegister);

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
        buttonRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
    });
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

