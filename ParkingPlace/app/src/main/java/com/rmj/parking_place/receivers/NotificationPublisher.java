package com.rmj.parking_place.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.os.AsyncTask;

import androidx.core.app.NotificationCompat;

import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.database.NotificationRepository;


public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification_id";
    public static String NOTIFICATION_TYPE = "notification_type";
    public static String NOTIFICATION_TEXT = "notification_text";
    public static String NOTIFICATION_TITLE = "notification_title";
    private static final String CHANNEL_ID = "PARKING_PLACE_NOTIFICATION_CHANNEL";

    private static NotificationRepository notificationRepository;

    @Override
    public void onReceive(final Context context, Intent intent) {
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        String notificationType = intent.getStringExtra(NOTIFICATION_TYPE);
        String notificationTitle = intent.getStringExtra(NOTIFICATION_TITLE);
        String notificationText = intent.getStringExtra(NOTIFICATION_TEXT);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                publishNotification(context, notificationId, notificationType, notificationTitle, notificationText);

                notificationRepository = new NotificationRepository(context);
                notificationRepository.deleteById(notificationId);

                return null;
            }
        }.execute();
    }

    private void publishNotification(Context context, int notificationId, String notificationType, String notificationTitle, String notificationText) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setCategory(notificationType)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.parking_splash_screen)
                .setLargeIcon(((BitmapDrawable) context.getResources().getDrawable(R.drawable.parking_splash_screen)).getBitmap())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent activity = PendingIntent.getActivity(context, notificationId, mainActivityIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(activity);

        Notification notification = builder.build();

        notificationManager.notify(notificationId, notification);
    }
}