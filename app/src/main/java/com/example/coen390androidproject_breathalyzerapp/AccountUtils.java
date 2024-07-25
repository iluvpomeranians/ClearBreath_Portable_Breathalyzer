package com.example.coen390androidproject_breathalyzerapp;

import android.content.Context;
import android.database.Cursor;

public class AccountUtils {

    private DBHelper dbHelper;

    public AccountUtils(Context context) {
        dbHelper = new DBHelper(context);
    }

    public long registerAccount(String fullName, String username, String password, String gender, int age, String email, double bmi) {
        return dbHelper.insertAccount(fullName, username, password, gender, age, email, bmi);
    }

    public AccountInfo loginAccount(String username, String password) {
        Cursor cursor = dbHelper.getAccountByUsername(username);
        if (cursor != null && cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PASSWORD));
            if (storedPassword.equals(password)) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID));
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_FULL_NAME));
                String gender = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_GENDER));
                int age = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_AGE));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EMAIL));
                double bmi = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BMI));
                cursor.close();
                return new AccountInfo(id, fullName, username, password, gender, age, email, bmi);
            }
        }
        return null;
    }

    public boolean updateAccount(int id, String fullName, String username, String password, String gender, int age, String email, double bmi) {
        return dbHelper.updateAccount(id, fullName, username, password, gender, age, email, bmi);
    }

    public boolean deleteAccount(int id) {
        return dbHelper.deleteAccount(id);
    }

    public AccountInfo getAccount(int id) {
        Cursor cursor = dbHelper.getAccount(id);
        if (cursor != null && cursor.moveToFirst()) {
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_FULL_NAME));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USERNAME));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PASSWORD));
            String gender = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_GENDER));
            int age = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_AGE));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EMAIL));
            double bmi = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BMI));
            cursor.close();
            return new AccountInfo(id, fullName, username, password, gender, age, email, bmi);
        }
        return null;
    }
}
