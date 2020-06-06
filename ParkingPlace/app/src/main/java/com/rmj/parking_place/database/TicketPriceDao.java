package com.rmj.parking_place.database;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.rmj.parking_place.model.TicketPrice;

import java.util.List;

@Dao
public interface TicketPriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TicketPriceDb ticketPrice);

}