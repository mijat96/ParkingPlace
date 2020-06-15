package com.rmj.parking_place.database;

import android.content.Context;

import androidx.room.Room;

import java.util.List;

public class NotificationRepository {
    private AppDatabase db;
    private NotificationDao notificationDao;

    public NotificationRepository(Context context) {
        db = Room.databaseBuilder(context, AppDatabase.class, "database-name").build();
        notificationDao = db.notificationDao();
    }

    public void insertNotification(NotificationDb notification) {
        notificationDao.insert(notification);
    }

    public List<NotificationDb> getNotifications() {
        return notificationDao.getAll();
    }

    public void deleteById(long id) {
        notificationDao.deleteById(id);
    }
}
