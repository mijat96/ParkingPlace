package com.rmj.parking_place.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rmj.parking_place.database.NotificationDb;
import com.rmj.parking_place.database.NotificationRepository;
import com.rmj.parking_place.utils.NotificationUtils;

import java.util.Date;
import java.util.List;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static NotificationRepository notificationRepository;

    @Override
    public void onReceive(Context context, Intent intent) {
        //showDialog("intent.getAction() == " + intent.getAction(), context);
        Toast.makeText(context, "intent.getAction() == " + intent.getAction(), Toast.LENGTH_LONG).show();
        Log.d("PARKING_PLACE_DEBUG", "intent.getAction() == " + intent.getAction());
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    notificationRepository = new NotificationRepository(context);

                    List<NotificationDb> notifications = notificationRepository.getNotifications();
                    Log.d("PARKING_PLACE_DEBUG", "notifications.size() ==" + notifications.size());
                    for (NotificationDb notification : notifications) {
                        //if(new Date().before(new Date(notification.getDateTime()))){
                            NotificationUtils.scheduleNotification(notification);
                        //}
                    }

                    return null;
                }
            }.execute();
        }
    }
}
