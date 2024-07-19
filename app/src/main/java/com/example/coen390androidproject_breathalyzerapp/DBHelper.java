package com.example.coen390androidproject_breathalyzerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "user_db";
    public static final int DATABASE_VERSION = 1;
    public static final String ACCOUNT_TABLE = "account";
    public static final String COLUMN_ID = "account_id";
    public static final String COLUMN_FULL_NAME = "account_full_name";
    public static final String COLUMN_USERNAME = "account_username";
    public static final String COLUMN_PASSWORD = "account_password";
    public static final String COLUMN_GENDER = "account_gender";
    public static final String COLUMN_AGE = "account_age";
    public static final String COLUMN_EMAIL = "account_email";
    public static final String COLUMN_BMI = "account_bmi";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + ACCOUNT_TABLE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FULL_NAME + " TEXT, " +
                    COLUMN_USERNAME + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_GENDER + " TEXT, " +
                    COLUMN_AGE + " INTEGER, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_BMI + " REAL, " +
                    COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ACCOUNT_TABLE);
        onCreate(db);
    }

    public long insertAccount(ContentValues values)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(ACCOUNT_TABLE, null, values);
        db.close();
        return id;
    }

    public Cursor getAccount(String username, String password)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ACCOUNT_TABLE, null, COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{username, password}, null, null, null);
        return cursor;
    }

    public Cursor getAccountById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ACCOUNT_TABLE, null, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        return cursor;
    }

    public int updateAccount(int id, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.update(ACCOUNT_TABLE, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public int deleteAccount(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(ACCOUNT_TABLE, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

}
