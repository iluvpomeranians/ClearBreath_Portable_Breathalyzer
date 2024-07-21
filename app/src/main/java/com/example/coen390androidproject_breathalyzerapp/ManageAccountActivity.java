package com.example.coen390androidproject_breathalyzerapp;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ManageAccountActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextAge, editTextBMI, editTextPassword;
    private Button buttonSaveChanges, buttonDeleteAccount;
    private DBHelper dbHelper;
    private int currentUserId = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DBHelper(this);
        currentUserId = getIntent().getIntExtra("currentUserId", -1);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextAge = findViewById(R.id.editTextAge);
        editTextBMI = findViewById(R.id.editTextBMI);

        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);

        if (currentUserId != -1) {
            Cursor cursor = dbHelper.getAccount(currentUserId);
            if (cursor != null && cursor.moveToFirst()) {
                editTextUsername.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME)));
                editTextAge.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_AGE))));
                editTextBMI.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BMI))));
                cursor.close();
            }
        }

        buttonSaveChanges.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            int age = Integer.parseInt(editTextAge.getText().toString().trim());
            double bmi = Double.parseDouble(editTextBMI.getText().toString().trim());
//            String password = editTextPassword.getText().toString().trim();


            boolean isUpdated = dbHelper.updateAccount(currentUserId,
                    null,
                     username,
                    null,
                    null,
                     age,
                    null, bmi);
            if (isUpdated) {
                Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManageAccountActivity.this, AccountActivity.class);
                intent.putExtra("currentUserId", currentUserId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to update account", Toast.LENGTH_SHORT).show();
            }
        });

        buttonDeleteAccount.setOnClickListener(v -> {
            boolean isDeleted = dbHelper.deleteAccount(currentUserId);
            if (isDeleted) {
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManageAccountActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(ManageAccountActivity.this, AccountActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }
}
