package com.rmj.parking_place.database;


import com.rmj.parking_place.App;

import java.util.List;

public class NotificationRepository {
    private AppDatabase db;
    private NotificationDao notificationDao;

    public NotificationRepository() {
        //db = Room.databaseBuilder(context, AppDatabase.class, "parking-place-db").build();
        db = App.getDatabase();
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
