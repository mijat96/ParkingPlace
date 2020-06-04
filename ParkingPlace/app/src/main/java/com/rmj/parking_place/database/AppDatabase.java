package com.rmj.parking_place.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {LocationDb.class, TicketPriceDb.class, ZoneDb.class, ZoneNorthEastLocationCrossRef.class,
                        ZoneSouthWestLocationCrossRef.class, ZoneTicketPriceCrossRef.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ZoneDao zoneDao();
    public abstract LocationDao locationDao();
    public abstract TicketPriceDao ticketPriceDao();
    public abstract ZoneNorthEastLocationCrossRefDao zoneNorthEastLocationCrossRefDao();
    public abstract ZoneSouthWestLocationCrossRefDao zoneSouthWestLocationCrossRefDao();
    public abstract ZoneTicketPriceCrossRefDao zoneTicketPriceCrossRefDao();
}
