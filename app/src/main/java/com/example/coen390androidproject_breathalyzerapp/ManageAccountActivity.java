package com.example.coen390androidproject_breathalyzerapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ManageAccountActivity extends AppCompatActivity {
private TextView textViewManageAccount, textViewAccountCreated;
private EditText editTextUsername, editTextAge, editTextBMI;
private Button buttonSaveChanges, buttonDeleteAccount;
private DBHelper dbHelper;
private int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);
        dbHelper = new DBHelper(this);
        textViewAccountCreated = findViewById(R.id.textViewAccountCreated);
        textViewManageAccount = findViewById(R.id.textViewManageAccount);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextAge = findViewById(R.id.editTextAge);
        editTextBMI = findViewById(R.id.editTextBMI);

        userId = getIntent().getIntExtra("USER_ID", -1);
        loadUserData();

        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);

        buttonSaveChanges.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            int age = Integer.parseInt(editTextAge.getText().toString().trim());
            double bmi = Double.parseDouble(editTextBMI.getText().toString().trim());

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_USERNAME, username);
            values.put(DBHelper.COLUMN_AGE, age);
            values.put(DBHelper.COLUMN_BMI, bmi);

            db.update(DBHelper.ACCOUNT_TABLE, values, DBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
            db.close();
        });

        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
        buttonDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to delete your account?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete(DBHelper.ACCOUNT_TABLE, DBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
                        db.close();

                        Intent intent = new Intent(ManageAccountActivity.this, AccountActivity.class);
                        startActivity(intent);
                        finish();

                    })
                    .setNegativeButton("No", null)
                    .show();
        });

    }
    private void loadUserData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.ACCOUNT_TABLE, new String[]{DBHelper.COLUMN_USERNAME, DBHelper.COLUMN_AGE, DBHelper.COLUMN_BMI, DBHelper.COLUMN_TIMESTAMP}, DBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String accountCreatedTime = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME));
            int age = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_AGE));
            float bmi = cursor.getFloat(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BMI));
            textViewAccountCreated.setText("Account Created: " + accountCreatedTime);
            editTextUsername.setText(username);
            editTextAge.setText(String.valueOf(age));
            editTextBMI.setText(String.valueOf(bmi));
            cursor.close();
        }
        db.close();
    }
}
