package com.example.coen390androidproject_breathalyzerapp;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AccountUtils {
    private static final String SHARED_PREFS_NAME = "UserPrefs";

    public static void registerAccount(Context context, String username, String password, String birthday) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String uniqueId = UUID.randomUUID().toString();
        editor.putString(username + "_password", password);
        editor.putString(username + "_birthday", birthday);
        editor.putString(username + "_id", uniqueId);
        editor.apply();
    }

    public static boolean loginAccount(Context context, String username, String password) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String savedPassword = sharedPreferences.getString(username + "_password", null);
        return password.equals(savedPassword);
    }

    public static void deleteAccount(Context context, String username) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(username + "_password");
        editor.remove(username + "_birthday");
        editor.remove(username + "_id");
        editor.apply();
    }

    public static Set<String> getAllAccounts(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getAll().keySet();
    }
}
