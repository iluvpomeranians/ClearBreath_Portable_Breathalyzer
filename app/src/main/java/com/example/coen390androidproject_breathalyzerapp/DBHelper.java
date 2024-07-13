package com.example.coen390androidproject_breathalyzerapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pb.db";
    private static final int DATABASE_VERSION = 1;
    private static final String ACCOUNT_TABLE = "Account";
    private static final String ACCOUNT_ID = "Account_id";
    private static final String ACCOUNT_NAME = "Account_name";
    private static final String ACCOUNT_LAST_NAME = "Account_last_name";
    private static final String ACCOUNT_GENDER = "Account_gender";
    private static final String ACCOUNT_EMAIL = "Account_email";
    private static final String ACCOUNT_PASSWORD = "Account_password";
    private static final String ACCOUNT_BMI = "Account_bmi";

    private static final String HISTORY_ID = "History_id";
    private static final String HISTORY_TIMESTAMP = "History_timestamp";
    private static final String HISTORY_VALUE = "History_value";



    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
