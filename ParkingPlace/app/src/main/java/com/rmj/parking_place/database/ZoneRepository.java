package com.rmj.parking_place.database;

import android.content.Context;

import androidx.room.Room;

import com.rmj.parking_place.App;
import com.rmj.parking_place.model.Location;
import com.rmj.parking_place.model.ParkingPlace;
import com.rmj.parking_place.model.TicketPrice;
import com.rmj.parking_place.model.Zone;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class ZoneRepository {
    private AppDatabase db;

    private ZoneDao zoneDao;
    private LocationDao locationDao;
    private TicketPriceDao ticketPriceDao;
    private ZoneNorthEastLocationCrossRefDao zoneNorthEastLocationCrossRefDao;
    private ZoneSouthWestLocationCrossRefDao zoneSouthWestLocationCrossRefDao;
    private ZoneTicketPriceCrossRefDao zoneTicketPriceCrossRefDao;

    public ZoneRepository() {
        // db = Room.databaseBuilder(context, AppDatabase.class, "database-name").build();
        db = App.getDatabase();
        zoneDao = db.zoneDao();
        locationDao = db.locationDao();
        ticketPriceDao = db.ticketPriceDao();
        zoneNorthEastLocationCrossRefDao = db.zoneNorthEastLocationCrossRefDao();
        zoneSouthWestLocationCrossRefDao = db.zoneSouthWestLocationCrossRefDao();
        zoneTicketPriceCrossRefDao = db.zoneTicketPriceCrossRefDao();
    }

    public void insertZones(List<Zone> zones) {
        long zoneId;
        long ticketPriceId;
        long locationId;

        for (Zone zone : zones) {
            zoneId = zoneDao.insert(new ZoneDb(zone));
            locationId = locationDao.insert(new LocationDb(zone.getNorthEast()));
            zoneNorthEastLocationCrossRefDao.insert(new ZoneNorthEastLocationCrossRef(zoneId, locationId));
            locationId = locationDao.insert(new LocationDb(zone.getSouthWest()));
            zoneSouthWestLocationCrossRefDao.insert(new ZoneSouthWestLocationCrossRef(zoneId, locationId));
            for (TicketPrice ticketPrice : zone.getTicketPrices()) {
                ticketPriceId = ticketPriceDao.insert(new TicketPriceDb(ticketPrice));
                zoneTicketPriceCrossRefDao.insert(new ZoneTicketPriceCrossRef(zoneId, ticketPriceId));
            }
        }
    }

    public List<Zone> getZones() {
        List<ZoneWithLocationsAndTicketPrices> list = zoneDao.getAll();
        List<Zone> zones = new ArrayList<Zone>();

        for (ZoneWithLocationsAndTicketPrices zoneWithLocationsAndTicketPrices : list) {
            zones.add(new Zone(zoneWithLocationsAndTicketPrices));
        }

        return zones;
    }

}
