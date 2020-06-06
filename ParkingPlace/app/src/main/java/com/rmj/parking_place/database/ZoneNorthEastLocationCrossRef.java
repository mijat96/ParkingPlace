package com.rmj.parking_place.database;

import androidx.room.Entity;

@Entity(primaryKeys = {"zoneId", "locationId"})
public class ZoneNorthEastLocationCrossRef {
    public long zoneId;
    public long locationId;

    public ZoneNorthEastLocationCrossRef() {

    }

    public ZoneNorthEastLocationCrossRef(long zoneId, long locationId) {
        this.zoneId = zoneId;
        this.locationId = locationId;
    }
}
