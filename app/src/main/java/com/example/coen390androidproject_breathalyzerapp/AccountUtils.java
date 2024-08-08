package com.example.coen390androidproject_breathalyzerapp;

import android.content.Context;
import android.database.Cursor;

// Utility class for managing account-related operations
public class AccountUtils {


    //THIS FILE IS OLD, WE KEPT IT JUST IN CASE, BUT THESE OPERATIONS ARE NOW EVERYWHERE AND THE "HEAD" IS DBHelper

    private DBHelper dbHelper;

    public AccountUtils(Context context) {
        dbHelper = new DBHelper(context);
    }

    public long registerAccount(String fullName, String username, String password, String gender, int age, String email, double bmi) {
        // Insert the account details into the database and return the new account ID
        return dbHelper.insertAccount(fullName, username, password, gender, age, email, bmi);
    }

    // Method to log in to an account
    public AccountInfo loginAccount(String username, String password) {
        // Query the database for the account with the given username
        Cursor cursor = dbHelper.getAccountByUsername(username);
        if (cursor != null && cursor.moveToFirst()) {
            // Retrieve the stored password from the database
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PASSWORD));
            // Check if the stored password matches the provided password
            if (storedPassword.equals(password)) {
                // Retrieve account details from the database
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID));
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_FULL_NAME));
                String gender = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_GENDER));
                int age = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_AGE));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EMAIL));
                double bmi = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BMI));
                cursor.close();
                // Return an AccountInfo object with the retrieved details
                return new AccountInfo(id, fullName, username, password, gender, age, email, bmi);
            }
        }
        // Return null if login fails
        return null;
    }

    // Method to update an existing account
    public boolean updateAccount(int id, String fullName, String username, String password, String gender, int age, String email, double bmi) {
        // Update the account details in the database and return the success status
        return dbHelper.updateAccount(id, fullName, username, password, gender, age, email, bmi);
    }

    // Method to delete an account
    public boolean deleteAccount(int id) {
        // Delete the account from the database and return the success status
        return dbHelper.deleteAccount(id);
    }

    // Method to retrieve account details by ID
    public AccountInfo getAccount(int id) {
        // Query the database for the account with the given ID
        Cursor cursor = dbHelper.getAccount(id);
        if (cursor != null && cursor.moveToFirst()) {
            // Retrieve account details from the database
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_FULL_NAME));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PASSWORD));
            String gender = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_GENDER));
            int age = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_AGE));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EMAIL));
            double bmi = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BMI));
            cursor.close();
            // Return an AccountInfo object with the retrieved details
            return new AccountInfo(id, fullName, username, password, gender, age, email, bmi);
        }
        // Return null if the account is not found
        return null;
    }
}