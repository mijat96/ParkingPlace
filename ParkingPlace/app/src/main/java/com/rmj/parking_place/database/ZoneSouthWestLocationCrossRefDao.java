package com.rmj.parking_place.database;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

@Dao
public interface ZoneSouthWestLocationCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ZoneSouthWestLocationCrossRef zoneSouthWestLocationCrossRef);

}