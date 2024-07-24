package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button btnLogin;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editTextUsername = findViewById(R.id.et_username);
        editTextPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        dbHelper = new DBHelper(this);
        btnLogin.setOnClickListener(v -> login());
        SettingsUtils.applySettings(this, editTextUsername, editTextPassword, btnLogin);
    }

    private void saveLoginState(int userId, boolean loggedIn) {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("loggedIn", loggedIn);
        editor.putInt("currentUserId", userId);
        editor.apply();
    }

    private void login() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        Cursor cursor = dbHelper.getAccountByUsername(username);
        if (cursor != null && cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PASSWORD));
            if (storedPassword == null) {
                Log.d("Debug", "Password in database does not exist yet");
            }
            if (storedPassword.equals(password)) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID));
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                saveLoginState(id, true);
                Log.d("LoginActivity", "Saved currentUserId: " + id);
                Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
                intent.putExtra("currentUserId", id);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                cursor.close();
                finish();
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
