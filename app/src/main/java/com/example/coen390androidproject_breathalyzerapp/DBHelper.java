package com.example.coen390androidproject_breathalyzerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "breathalyzerApp.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_BMI = "bmi";

    public static final String TABLE_BAC_DATA = "bac_data";
    public static final String COLUMN_ACCOUNT_ID = "account_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_BAC_VALUE = "bac_value";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAccountsTable = "CREATE TABLE " + TABLE_ACCOUNTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FULL_NAME + " TEXT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_GENDER + " TEXT, " +
                COLUMN_AGE + " INTEGER, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_BMI + " REAL" +
                ")";
        db.execSQL(createAccountsTable);

        String createBACDataTable = "CREATE TABLE " + TABLE_BAC_DATA + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ACCOUNT_ID + " INTEGER, " +
                COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_BAC_VALUE + " REAL, " +
                "FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNTS + "(" + COLUMN_ID + ")" +
                ")";
        db.execSQL(createBACDataTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BAC_DATA);
        onCreate(db);
    }

    public long insertAccount(String fullName, String username, String password, String gender, int age, String email, double bmi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_BMI, bmi);
        return db.insert(TABLE_ACCOUNTS, null, values);
    }

    public boolean updateAccount(int id, String fullName, String username, String password, String gender, Integer age, String email, Double bmi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (fullName != null) values.put(COLUMN_FULL_NAME, fullName);
        if (username != null) values.put(COLUMN_USERNAME, username);
        if (password != null) values.put(COLUMN_PASSWORD, password);
        if (gender != null) values.put(COLUMN_GENDER, gender);
        if (age != null) values.put(COLUMN_AGE, age);
        if (email != null) values.put(COLUMN_EMAIL, email);
        if (bmi != null) values.put(COLUMN_BMI, bmi);
        int rowsAffected = db.update(TABLE_ACCOUNTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    public boolean deleteAccount(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ACCOUNTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor getAccount(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ACCOUNTS, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
    }

    public Cursor getAccountByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ACCOUNTS, null, COLUMN_USERNAME + " = ?", new String[]{username}, null, null, null);
    }

    public Cursor getBACRecords(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BAC_DATA, null, COLUMN_ACCOUNT_ID + " = ?", new String[]{String.valueOf(accountId)}, null, null, COLUMN_TIMESTAMP + " DESC");
    }

    public long insertBACRecord(int accountId, String timestamp, double bacValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ACCOUNT_ID, accountId);
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_BAC_VALUE, bacValue);
        return db.insert(TABLE_BAC_DATA, null, values);
    }
}
