package com.example.woodpecker;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

public class NotificationHelper {

    private static final String CHANNEL_ID = "word_of_the_day_channel"; // Unique channel ID
    public static final int NOTIFICATION_ID = 1001; // Unique notification ID

    public static void sendWordOfTheDayNotification(Context context, String word) {
        // Create the NotificationManager
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if we need to create the notification channel
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            // Create notification channel
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Word of the Day",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notification for the daily word to practice.");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            channel.enableVibration(true);

            // Register the channel with the system
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Intent i = new Intent(context, MainActivity.class);
        PendingIntent intentEnNot;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intentEnNot = PendingIntent.getActivity(context,
                    0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }else {
            intentEnNot = PendingIntent.getActivity(context,
                    0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // Create the notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.great_spotted_woodpecker))
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(context.getString(R.string.not_word_of_the_day))
                .setContentText(word)
                .setVibrate(new long[]{0, 1000, 500, 1000})  // Vibrate pattern
                .setContentIntent(intentEnNot)
                .setAutoCancel(true);

        // Send the notification
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        // Check if the notification permission is granted (Android 13+)
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());  // Trigger the notification
    }

    public static void scheduleDailyNotification(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);  // schedule at 9 AM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // calendar.add(Calendar.SECOND, 10); // for testing

        if (alarmManager != null) {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        }
    }

    public static void cancelDailyNotification(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
