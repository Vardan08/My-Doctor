package com.example.mydoctor.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.example.mydoctor.R;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationHelper {

    private static final String CHANNEL_ID = "vaccination_reminder_channel";

    /** Each reminder needs its own id, otherwise a second one silently replaces the first. */
    private static final AtomicInteger NOTIFICATION_ID = new AtomicInteger(1);

    public static void sendNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Vaccination Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(NOTIFICATION_ID.getAndIncrement(), builder.build());
        Log.d("NotificationHelper", "Notification sent: " + title + " - " + message);
    }
}
