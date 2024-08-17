package com.example.coen390androidproject_breathalyzerapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;
/// Helper class to create notifications
public class NotificationHelper {
    private static final String CHANNEL_ID = "SOBER_NOTIFICATION_CHANNEL";
    private static final String CHANNEL_NAME = "Sober Notification";
    private static final String CHANNEL_DESC = "Notifications for when you are sober";
    private static final int NOTIFICATION_ID = 1;
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_IS_FROM_NOTIFICATION = "isFromNotification";

    public static void createNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Log.d("NotificationHelper", "Preparing to create notification channel.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("NotificationHelper", "Creating notification channel.");
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // High importance for visibility
            );
            channel.setDescription(CHANNEL_DESC);
            notificationManager.createNotificationChannel(channel);
        } else {
            Log.d("NotificationHelper", "Skipping notification channel creation as the API level is below 26.");
        }

        // Create an intent to open HomeActivity when the notification is clicked
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("From", "Notification");

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_FROM_NOTIFICATION, true);
        editor.apply();
        Log.d("NotificationHelper", "createNotification: isFromNotification set to true");

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d("NotificationHelper", "Building the notification with title: " + title + " and message: " + message);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        int notificationId = (int) System.currentTimeMillis();
        Log.d("NotificationHelper", "Displaying notification with ID: " + notificationId);
        notificationManager.notify(notificationId, builder.build());
    }
}
