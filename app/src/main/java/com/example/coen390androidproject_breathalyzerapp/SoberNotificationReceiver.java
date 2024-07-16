package com.example.coen390androidproject_breathalyzerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SoberNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        NotificationHelper.createNotification(context, title, message);
    }
}
