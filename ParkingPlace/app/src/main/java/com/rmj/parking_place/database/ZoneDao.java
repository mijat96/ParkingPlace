package com.rmj.parking_place.database;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;


import java.util.List;

@Dao
public interface ZoneDao {
    @Transaction
    @Query("SELECT * FROM zone")
    List<ZoneWithLocationsAndTicketPrices> getAll();

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ZoneDb zone);

}