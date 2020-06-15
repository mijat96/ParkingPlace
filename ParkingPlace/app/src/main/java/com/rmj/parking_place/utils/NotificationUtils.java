package com.rmj.parking_place.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.os.SystemClock;

import com.rmj.parking_place.App;
import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.database.NotificationDb;
import com.rmj.parking_place.database.NotificationRepository;
import com.rmj.parking_place.receivers.NotificationPublisher;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class NotificationUtils {

    /*
        delay is after how much time(in millis) from current time you want to schedule the notification
    */
    public static void scheduleNotification(long delay, String notificationType, int notificationid, String notificationTitle,
                                            String notificationText) {
        Context context = App.getAppContext();

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationid);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_TYPE, notificationType);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_TITLE, notificationTitle);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_TEXT, notificationText);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationid, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        long futureInMillis = /*SystemClock.elapsedRealtime()*/ System.currentTimeMillis() + delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP /*AlarmManager.ELAPSED_REALTIME_WAKEUP*/, futureInMillis, pendingIntent);
    }

    public static void scheduleNotification(NotificationDb notification) {
        Context context = App.getAppContext();

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, (int) notification.getId());
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_TYPE, notification.getType());
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_TITLE, notification.getTitle());
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_TEXT, notification.getText());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) notification.getId(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP /*AlarmManager.ELAPSED_REALTIME_WAKEUP*/, notification.getDateTime(), pendingIntent);
    }

    public static void cancelNotification(String notificationType, int notificationId) {
        Context context = App.getAppContext();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationId);
        intent.putExtra(NotificationPublisher.NOTIFICATION_TYPE, notificationType);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

}
