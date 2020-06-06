package com.rmj.parking_place.database;

import androidx.room.Entity;

@Entity(primaryKeys = {"zoneId", "locationId"})
public class ZoneSouthWestLocationCrossRef {
    public long zoneId;
    public long locationId;

    public ZoneSouthWestLocationCrossRef() {

    }

    public ZoneSouthWestLocationCrossRef(long zoneId, long locationId) {
        this.zoneId = zoneId;
        this.locationId = locationId;
    }
}
