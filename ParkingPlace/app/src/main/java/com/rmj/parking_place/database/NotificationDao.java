package com.rmj.parking_place.database;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface NotificationDao {
    @Transaction
    @Query("SELECT * FROM notification")
    List<NotificationDb> getAll();


    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(NotificationDb notification);


    @Query("DELETE FROM notification WHERE id = :id")
    void deleteById(long id);

}