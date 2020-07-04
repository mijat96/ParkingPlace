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

import com.google.gson.Gson;
import com.rmj.parking_place.R;
import com.rmj.parking_place.actvities.MainActivity;
import com.rmj.parking_place.database.NotificationRepository;
import com.rmj.parking_place.model.ParkingPlace;


public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification_id";
    public static String NOTIFICATION_TYPE = "notification_type";
    public static String NOTIFICATION_TEXT = "notification_text";
    public static String NOTIFICATION_TITLE = "notification_title";
    public static String PARKING_PLACE_ID = "parking_place_id";
    private static final String CHANNEL_ID = "PARKING_PLACE_NOTIFICATION_CHANNEL";

    private static NotificationRepository notificationRepository;

    @Override
    public void onReceive(final Context context, Intent intent) {
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        int paringPlaceId = intent.getIntExtra(PARKING_PLACE_ID, 0);
        String notificationType = intent.getStringExtra(NOTIFICATION_TYPE);
        String notificationTitle = intent.getStringExtra(NOTIFICATION_TITLE);
        String notificationText = intent.getStringExtra(NOTIFICATION_TEXT);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                publishNotification(context, notificationId, notificationType, notificationTitle, notificationText, paringPlaceId);

                notificationRepository = new NotificationRepository();
                notificationRepository.deleteById(notificationId);

                return null;
            }
        }.execute();
    }

    private void publishNotification(Context context, int notificationId, String notificationType, String notificationTitle, String notificationText, int parkingPlaceId) {
        int actionIconId;
        String actionString = "";
        boolean reservationOrTaken;

        if(notificationType.equals("reseravation_notification")){
            actionIconId = R.drawable.reservation_home_icon;
            actionString = "Reserve";
            reservationOrTaken = true;
        }else{
            actionIconId = R.drawable.reservation_home_icon;
            actionString = "Take";
            reservationOrTaken = false;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(!reservationOrTaken){
            Intent notificationActionIntent = new Intent(context, TakeAndReserveReceiver.class);
            notificationActionIntent.putExtra(NOTIFICATION_ID, notificationId);
            notificationActionIntent.putExtra(NOTIFICATION_TYPE, notificationType);
            notificationActionIntent.putExtra(PARKING_PLACE_ID, parkingPlaceId);
            PendingIntent notificationActionPendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setCategory(notificationType)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.parking_splash_screen)
                    .setLargeIcon(((BitmapDrawable) context.getResources().getDrawable(R.drawable.parking_splash_screen)).getBitmap())
                    .addAction(actionIconId, actionString, notificationActionPendingIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            PendingIntent activity = PendingIntent.getActivity(context, notificationId, mainActivityIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(activity);

            Notification notification = builder.build();

            notificationManager.notify(notificationId, notification);
        }else{
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
}