package com.rmj.parking_place.database;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ZoneTicketPriceCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ZoneTicketPriceCrossRef zoneTicketPriceCrossRef);

}